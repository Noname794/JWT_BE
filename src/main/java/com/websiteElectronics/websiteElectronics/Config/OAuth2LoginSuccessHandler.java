package com.websiteElectronics.websiteElectronics.Config;

import com.websiteElectronics.websiteElectronics.Entities.Customers;
import com.websiteElectronics.websiteElectronics.Repositories.CustomersRepository;
import com.websiteElectronics.websiteElectronics.Services.Impl.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);
    
    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub");
        String provider = getProvider(request);
        
        logger.info("OAuth2 login success - Provider: {}, Email: {}", provider, email);

        Customers customers = findOrCreateCustomer(email, name, provider, providerId);

        UserDetails userDetails = User.builder()
                .username(customers.getEmail())
                .password("")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + customers.getRole().toUpperCase())))
                .build();

        String token = jwtService.generateToken(userDetails);

        String redirectUrl = String.format(
                "http://localhost:5173/oauth2/redirect?token=%s&email=%s&customerId=%d",
                token,
                customers.getEmail(),
                customers.getId()
        );

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);

    }

    private String getProvider(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.contains("google")) {
            return "GOOGLE";
        } else if (uri.contains("facebook")) {
            return "FACEBOOK";
        } else {
            return "UNKNOWN";
        }
    }

    private Customers findOrCreateCustomer(String email, String name, String provider, String providerId) {
        Optional<Customers> existingCustomer = customersRepository.findByEmail(email);

        if(existingCustomer.isPresent()) {
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
        logger.info("Created new customer from OAuth2 - Provider: {}, Email: {}", provider, email);
        
        return savedCustomer;
    }

}
