package com.websiteElectronics.websiteElectronics.Services;

import com.websiteElectronics.websiteElectronics.Entities.Oauth2Clients;
import com.websiteElectronics.websiteElectronics.Repositories.Oauth2ClientsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JpaRegisteredClientRepositoryTest {

    @Mock
    private Oauth2ClientsRepository oauth2ClientsRepository;

    @InjectMocks
    private JpaRegisteredClientRepository jpaRegisteredClientRepository;

    private Oauth2Clients testClient;

    @BeforeEach
    void setUp() {
        testClient = new Oauth2Clients();
        testClient.setClientId("test-client");
        testClient.setClientSecret("{bcrypt}$2a$10$encodedSecret");
        testClient.setClientName("Test Client Application");
        testClient.setRedirectUris("http://localhost:3000/callback");
        testClient.setScopes("read,write,openid");
        testClient.setAuthorizationGrantTypes("authorization_code,refresh_token");
        testClient.setClientAuthenticationMethods("client_secret_basic,client_secret_post");
        testClient.setAccessTokenValiditySeconds(3600);
        testClient.setRefreshTokenValiditySeconds(86400);
        testClient.setCreatedAt(Instant.now());
        testClient.setUpdatedAt(Instant.now());
    }

    @Test
    void testFindByClientId_Success() {
        when(oauth2ClientsRepository.findByClientId("test-client"))
                .thenReturn(Optional.of(testClient));

        RegisteredClient result = jpaRegisteredClientRepository.findByClientId("test-client");

        assertNotNull(result);
        assertEquals("test-client", result.getClientId());
        assertEquals("Test Client Application", result.getClientName());
        assertEquals("{bcrypt}$2a$10$encodedSecret", result.getClientSecret());
        assertTrue(result.getRedirectUris().contains("http://localhost:3000/callback"));
        assertTrue(result.getScopes().contains("read"));
        assertTrue(result.getScopes().contains("write"));
        assertTrue(result.getScopes().contains("openid"));
        
        verify(oauth2ClientsRepository, times(1)).findByClientId("test-client");
    }

    @Test
    void testFindByClientId_NotFound() {
        when(oauth2ClientsRepository.findByClientId("non-existent"))
                .thenReturn(Optional.empty());

        RegisteredClient result = jpaRegisteredClientRepository.findByClientId("non-existent");

        assertNull(result);
        verify(oauth2ClientsRepository, times(1)).findByClientId("non-existent");
    }

    @Test
    void testFindById_Success() {
        when(oauth2ClientsRepository.findById("test-client"))
                .thenReturn(Optional.of(testClient));

        RegisteredClient result = jpaRegisteredClientRepository.findById("test-client");

        assertNotNull(result);
        assertEquals("test-client", result.getClientId());
        verify(oauth2ClientsRepository, times(1)).findById("test-client");
    }

    @Test
    void testFindById_NotFound() {
        when(oauth2ClientsRepository.findById("non-existent"))
                .thenReturn(Optional.empty());

        RegisteredClient result = jpaRegisteredClientRepository.findById("non-existent");

        assertNull(result);
        verify(oauth2ClientsRepository, times(1)).findById("non-existent");
    }

    @Test
    void testSave_Success() {
        RegisteredClient registeredClient = RegisteredClient.withId("new-client")
                .clientId("new-client")
                .clientSecret("{bcrypt}$2a$10$newSecret")
                .clientName("New Client")
                .redirectUri("http://localhost:3000/callback")
                .scope("read")
                .scope("write")
                .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientAuthenticationMethod(org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .build();

        when(oauth2ClientsRepository.save(any(Oauth2Clients.class)))
                .thenReturn(testClient);

        jpaRegisteredClientRepository.save(registeredClient);

        verify(oauth2ClientsRepository, times(1)).save(any(Oauth2Clients.class));
    }

    @Test
    void testConversion_MultipleRedirectUris() {

        testClient.setRedirectUris("http://localhost:3000/callback,http://localhost:3001/callback");
        when(oauth2ClientsRepository.findByClientId("test-client"))
                .thenReturn(Optional.of(testClient));

        RegisteredClient result = jpaRegisteredClientRepository.findByClientId("test-client");

        assertNotNull(result);
        assertEquals(2, result.getRedirectUris().size());
        assertTrue(result.getRedirectUris().contains("http://localhost:3000/callback"));
        assertTrue(result.getRedirectUris().contains("http://localhost:3001/callback"));
    }

    @Test
    void testConversion_MultipleScopes() {

        testClient.setScopes("read,write,openid,profile,email");
        when(oauth2ClientsRepository.findByClientId("test-client"))
                .thenReturn(Optional.of(testClient));

        RegisteredClient result = jpaRegisteredClientRepository.findByClientId("test-client");

        assertNotNull(result);
        assertEquals(5, result.getScopes().size());
        assertTrue(result.getScopes().contains("read"));
        assertTrue(result.getScopes().contains("write"));
        assertTrue(result.getScopes().contains("openid"));
        assertTrue(result.getScopes().contains("profile"));
        assertTrue(result.getScopes().contains("email"));
    }

    @Test
    void testConversion_TokenSettings() {

        testClient.setAccessTokenValiditySeconds(7200); // 2 hours
        testClient.setRefreshTokenValiditySeconds(172800); // 2 days
        when(oauth2ClientsRepository.findByClientId("test-client"))
                .thenReturn(Optional.of(testClient));

        RegisteredClient result = jpaRegisteredClientRepository.findByClientId("test-client");

        assertNotNull(result);
        assertNotNull(result.getTokenSettings());
        assertEquals(7200, result.getTokenSettings().getAccessTokenTimeToLive().getSeconds());
        assertEquals(172800, result.getTokenSettings().getRefreshTokenTimeToLive().getSeconds());
    }

    @Test
    void testConversion_ClientSettings() {

        when(oauth2ClientsRepository.findByClientId("test-client"))
                .thenReturn(Optional.of(testClient));

        RegisteredClient result = jpaRegisteredClientRepository.findByClientId("test-client");

        assertNotNull(result);
        assertNotNull(result.getClientSettings());
        assertTrue(result.getClientSettings().isRequireAuthorizationConsent());
    }

    @Test
    void testConversion_EmptyFields() {

        testClient.setRedirectUris("");
        testClient.setScopes("");
        testClient.setAuthorizationGrantTypes("");
        testClient.setClientAuthenticationMethods("");
        when(oauth2ClientsRepository.findByClientId("test-client"))
                .thenReturn(Optional.of(testClient));

        RegisteredClient result = jpaRegisteredClientRepository.findByClientId("test-client");

        assertNotNull(result);
        assertTrue(result.getRedirectUris().isEmpty());
        assertTrue(result.getScopes().isEmpty());
        assertTrue(result.getAuthorizationGrantTypes().isEmpty());
        assertTrue(result.getClientAuthenticationMethods().isEmpty());
    }

    @Test
    void testConversion_NullFields() {
        testClient.setRedirectUris(null);
        testClient.setScopes(null);
        testClient.setAuthorizationGrantTypes(null);
        testClient.setClientAuthenticationMethods(null);
        testClient.setAccessTokenValiditySeconds(null);
        testClient.setRefreshTokenValiditySeconds(null);
        when(oauth2ClientsRepository.findByClientId("test-client"))
                .thenReturn(Optional.of(testClient));

        RegisteredClient result = jpaRegisteredClientRepository.findByClientId("test-client");

        assertNotNull(result);
        assertTrue(result.getRedirectUris().isEmpty());
        assertTrue(result.getScopes().isEmpty());

        assertNotNull(result.getTokenSettings());
    }
}
