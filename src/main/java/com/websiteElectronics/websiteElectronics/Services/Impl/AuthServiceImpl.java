package com.websiteElectronics.websiteElectronics.Services.Impl;

import com.websiteElectronics.websiteElectronics.Dtos.AuthResponse;
import com.websiteElectronics.websiteElectronics.Dtos.LoginRequest;
import com.websiteElectronics.websiteElectronics.Dtos.RegisterRequest;
import com.websiteElectronics.websiteElectronics.Entities.Customers;
import com.websiteElectronics.websiteElectronics.Repositories.CustomersRepository;
import com.websiteElectronics.websiteElectronics.Services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final CustomersRepository customersRepository;

    private final PasswordEncoder passwordEncoder;

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
}
