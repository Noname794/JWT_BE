package com.websiteElectronics.websiteElectronics.Services.Impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.websiteElectronics.websiteElectronics.Dtos.*;
import com.websiteElectronics.websiteElectronics.Entities.Customers;
import com.websiteElectronics.websiteElectronics.Repositories.CustomersRepository;
import com.websiteElectronics.websiteElectronics.Services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final CustomersRepository customersRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Autowired
    public AuthServiceImpl(CustomersRepository customersRepository, PasswordEncoder passwordEncoder) {
        this.customersRepository = customersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        Customers customer = new Customers();
        customer.setEmail(request.getEmail());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customersRepository.save(customer);
        return new AuthResponse("Registration successful", null);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Customers customer = customersRepository.findByEmail(request.getEmail())
                .orElse(null);
        if (customer == null) {
            return new AuthResponse("Invalid email", null);
        }
        String dbPassword = customer.getPassword();
        String rawPassword = request.getPassword();

        if (!dbPassword.startsWith("$2a$") && !dbPassword.startsWith("$2b$") && !dbPassword.startsWith("$2y$")) {

            if (dbPassword.equals(rawPassword)) {
                String encoded = passwordEncoder.encode(rawPassword);
                customer.setPassword(encoded);
                customersRepository.save(customer);
                dbPassword = encoded;
            } else {
                return new AuthResponse("Invalid password", null);
            }
        }
        if (!passwordEncoder.matches(rawPassword, dbPassword)) {
            return new AuthResponse("Invalid email or password", null);
        }
        return new AuthResponse("Login successful", null, customer.getId());
    }

    @Override
    public LoginResponse verifyGg(String token) {
        try{
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(clientId))
                    .build();
            GoogleIdToken idToken = verifier.verify(token);

            if (idToken == null) {
                throw new RuntimeException("Invalid token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String providerId = payload.getSubject();
            String provider = "GOOGLE";
            
            logger.info("Google token verified - Email: {}", email);
            
            Customers customer = findOrCreateCustomer(email, name, provider, providerId);
            UserDetails userDetails = User.builder()
                    .username(customer.getEmail())
                    .password("")
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + customer.getRole().toUpperCase())))
                    .build();
            
            String jwtToken = jwtService.generateToken(userDetails);
            
            return LoginResponse.builder()
                    .message("Google login successful")
                    .email(customer.getEmail())
                    .customerId(customer.getId())
                    .roles(List.of("ROLE_" + customer.getRole().toUpperCase()))
                    .accessToken(jwtToken)
                    .tokenType("Bearer")
                    .build();
        } catch (Exception e) {
            logger.error("Error verifying Google token", e);
            throw new RuntimeException("Failed to verify Google token: " + e.getMessage());
        }
    }

    @Override
    public LoginResponse refreshToken(String token) {
        try {
            String email = jwtService.extractUsername(token);
            Customers customer = customersRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            UserDetails userDetails = User.builder()
                    .username(customer.getEmail())
                    .password("")
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + customer.getRole().toUpperCase())))
                    .build();

            if (jwtService.validateToken(token, userDetails)) {
                String newToken = jwtService.generateToken(userDetails);

                return LoginResponse.builder()
                        .message("Token refreshed")
                        .email(customer.getEmail())
                        .customerId(customer.getId())
                        .roles(List.of("ROLE_" + customer.getRole().toUpperCase()))
                        .accessToken(newToken)
                        .tokenType("Bearer")
                        .build();
            } else {
                throw new RuntimeException("Invalid token");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to refresh token: " + e.getMessage());
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            String email = jwtService.extractUsername(token);
            Customers customer = customersRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            UserDetails userDetails = User.builder()
                    .username(customer.getEmail())
                    .password("")
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + customer.getRole().toUpperCase())))
                    .build();

            return jwtService.validateToken(token, userDetails);
        } catch (Exception e) {
            return false;
        }
    }

    private Customers findOrCreateCustomer(String email, String name, String provider, String providerId) {
        Optional<Customers> existingCustomer = customersRepository.findByEmail(email);

        if (existingCustomer.isPresent()) {
            Customers customer = existingCustomer.get();

            if (customer.getOauthProvider() == null || customer.getOauthProvider().equals("LOCAL")) {
                customer.setOauthProvider(provider);
                customer.setOauthProviderId(providerId);
                customersRepository.save(customer);
                logger.info("Updated existing customer with OAuth provider: {}", provider);
            }
            
            return customer;
        }

        Customers newCustomer = new Customers();
        newCustomer.setEmail(email);

        String[] nameParts = name != null ? name.split(" ", 2) : new String[]{"", ""};
        newCustomer.setFirstName(nameParts.length > 0 ? nameParts[0] : "");
        newCustomer.setLastName(nameParts.length > 1 ? nameParts[1] : "");

        newCustomer.setPassword("");
        newCustomer.setRole("USER");
        newCustomer.setOauthProvider(provider);
        newCustomer.setOauthProviderId(providerId);

        Customers savedCustomer = customersRepository.save(newCustomer);
        logger.info("Created new customer from OAuth - Provider: {}, Email: {}", provider, email);

        return savedCustomer;
    }
}
