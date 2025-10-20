package com.websiteElectronics.websiteElectronics.Config;

import com.websiteElectronics.websiteElectronics.Entities.Customers;
import com.websiteElectronics.websiteElectronics.Repositories.CustomersRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final CustomersRepository customersRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomAuthenticationProvider(
            CustomersRepository customersRepository,
            @Lazy PasswordEncoder passwordEncoder) {
        this.customersRepository = customersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final String email = authentication.getName();
        final String password = authentication.getCredentials().toString();

        Optional<Customers> customers = customersRepository.findByEmail(email);

        if(customers.isEmpty()){
            throw new BadCredentialsException("Email not found");
        }

        Customers customers1 = customers.get();

        if(!passwordEncoder.matches(password, customers1.getPassword())){
            throw new BadCredentialsException("Password not matched");
        }

        return authenticateAndGetToken(customers1);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    private UsernamePasswordAuthenticationToken authenticateAndGetToken(Customers customers) {
        final List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        String role = customers.getRole();
        if(role == null || role.isEmpty()){
            role = "USER";
        }

        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));

        final UserDetails principal = new User(
            customers.getEmail(),
            customers.getPassword(),
            grantedAuthorities
        );

        return new UsernamePasswordAuthenticationToken(principal, customers.getPassword(), grantedAuthorities);

    }
}
