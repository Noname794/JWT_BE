package com.websiteElectronics.websiteElectronics.Services;

import com.websiteElectronics.websiteElectronics.Entities.Oauth2Clients;
import com.websiteElectronics.websiteElectronics.Repositories.Oauth2ClientsRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Service
public class JpaRegisteredClientRepository implements RegisteredClientRepository {

    private final Oauth2ClientsRepository oauth2ClientsRepository;

    public JpaRegisteredClientRepository(Oauth2ClientsRepository oauth2ClientsRepository) {
        this.oauth2ClientsRepository = oauth2ClientsRepository;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        Oauth2Clients oauth2Client = toEntity(registeredClient);
        oauth2ClientsRepository.save(oauth2Client);
    }

    @Override
    public RegisteredClient findById(String id) {
        return oauth2ClientsRepository.findById(id)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return oauth2ClientsRepository.findByClientId(clientId)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    private Oauth2Clients toEntity(RegisteredClient registeredClient) {
        return Oauth2Clients.builder()
                .clientId(registeredClient.getClientId())
                .clientSecret(registeredClient.getClientSecret())
                .clientName(registeredClient.getClientName())
                .redirectUris(String.join(",", registeredClient.getRedirectUris()))
                .scopes(String.join(",", registeredClient.getScopes()))
                .authorizationGrantTypes(registeredClient.getAuthorizationGrantTypes().stream()
                        .map(AuthorizationGrantType::getValue)
                        .reduce((a, b) -> a + "," + b)
                        .orElse(""))
                .clientAuthenticationMethods(registeredClient.getClientAuthenticationMethods().stream()
                        .map(ClientAuthenticationMethod::getValue)
                        .reduce((a, b) -> a + "," + b)
                        .orElse(""))
                .accessTokenValiditySeconds((int) registeredClient.getTokenSettings()
                        .getAccessTokenTimeToLive().getSeconds())
                .refreshTokenValiditySeconds((int) registeredClient.getTokenSettings()
                        .getRefreshTokenTimeToLive().getSeconds())
                .build();
    }


    private RegisteredClient toRegisteredClient(Oauth2Clients client) {
        RegisteredClient.Builder builder = RegisteredClient.withId(client.getClientId())
                .clientId(client.getClientId())
                .clientSecret(client.getClientSecret())
                .clientName(client.getClientName());

        if (client.getRedirectUris() != null && !client.getRedirectUris().isEmpty()) {
            Arrays.stream(client.getRedirectUris().split(","))
                    .map(String::trim)
                    .forEach(builder::redirectUri);
        }

        if (client.getScopes() != null && !client.getScopes().isEmpty()) {
            Arrays.stream(client.getScopes().split(","))
                    .map(String::trim)
                    .forEach(builder::scope);
        }

        if (client.getAuthorizationGrantTypes() != null && !client.getAuthorizationGrantTypes().isEmpty()) {
            Arrays.stream(client.getAuthorizationGrantTypes().split(","))
                    .map(String::trim)
                    .forEach(grantType -> builder.authorizationGrantType(new AuthorizationGrantType(grantType)));
        }

        if (client.getClientAuthenticationMethods() != null && !client.getClientAuthenticationMethods().isEmpty()) {
            Arrays.stream(client.getClientAuthenticationMethods().split(","))
                    .map(String::trim)
                    .forEach(method -> builder.clientAuthenticationMethod(new ClientAuthenticationMethod(method)));
        }

        TokenSettings tokenSettings = TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofSeconds(
                        Optional.ofNullable(client.getAccessTokenValiditySeconds()).orElse(3600)))
                .refreshTokenTimeToLive(Duration.ofSeconds(
                        Optional.ofNullable(client.getRefreshTokenValiditySeconds()).orElse(86400)))
                .build();

        builder.tokenSettings(tokenSettings);

        builder.clientSettings(ClientSettings.builder()
                .requireAuthorizationConsent(true)
                .build());

        return builder.build();
    }
}
