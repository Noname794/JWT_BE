package com.websiteElectronics.websiteElectronics.Services.Impl;

import com.websiteElectronics.websiteElectronics.Entities.Oauth2Clients;
import com.websiteElectronics.websiteElectronics.Repositories.Oauth2ClientsRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class Oauth2ClientInitializer {

    @Autowired
    private Oauth2ClientsRepository oauth2ClientsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initializeDefaultClient() {
        if (oauth2ClientsRepository.findByClientId("react-client").isEmpty()) {
            Oauth2Clients reactClient = Oauth2Clients.builder()
                    .clientId("react-client")
                    .clientSecret(passwordEncoder.encode("react-secret"))
                    .clientName("React Web Application")
                    .redirectUris("http://localhost:3000/oauth2/callback")
                    .scopes("read,write,openid")
                    .authorizationGrantTypes("authorization_code,refresh_token")
                    .clientAuthenticationMethods("client_secret_basic,client_secret_post")
                    .accessTokenValiditySeconds(3600)
                    .refreshTokenValiditySeconds(2592000)
                    .build();

            oauth2ClientsRepository.save(reactClient);
            System.out.println(" OAuth2 Client 'react-client' initialized:");
            System.out.println("   Client ID: react-client");
            System.out.println("   Client Secret: react-secret");
            System.out.println("   Redirect URI: http://localhost:3000/oauth2/callback");
            System.out.println("   Scopes: read, write, openid");
        } else {
            System.out.println("ℹ  OAuth2 Client 'react-client' already exists");
        }
    }
}
