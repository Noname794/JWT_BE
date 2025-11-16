package com.websiteElectronics.websiteElectronics.Services;

import com.websiteElectronics.websiteElectronics.Entities.Oauth2Clients;
import com.websiteElectronics.websiteElectronics.Repositories.Oauth2ClientsRepository;
import com.websiteElectronics.websiteElectronics.Services.Impl.Oauth2ClientInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class Oauth2ClientInitializerTest {

    @Mock
    private Oauth2ClientsRepository oauth2ClientsRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private Oauth2ClientInitializer oauth2ClientInitializer;

    @BeforeEach
    void setUp() {
        reset(oauth2ClientsRepository, passwordEncoder);
    }

    @Test
    void testInitializeDefaultClient_WhenClientNotExists() {

        when(oauth2ClientsRepository.findByClientId("react-client"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("react-secret"))
                .thenReturn("{bcrypt}$2a$10$encodedSecret");
        when(oauth2ClientsRepository.save(any(Oauth2Clients.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        oauth2ClientInitializer.initializeDefaultClient();

        ArgumentCaptor<Oauth2Clients> clientCaptor = ArgumentCaptor.forClass(Oauth2Clients.class);
        verify(oauth2ClientsRepository, times(1)).findByClientId("react-client");
        verify(passwordEncoder, times(1)).encode("react-secret");
        verify(oauth2ClientsRepository, times(1)).save(clientCaptor.capture());

        Oauth2Clients savedClient = clientCaptor.getValue();
        assertNotNull(savedClient);
        assertEquals("react-client", savedClient.getClientId());
        assertEquals("{bcrypt}$2a$10$encodedSecret", savedClient.getClientSecret());
        assertEquals("React Web Application", savedClient.getClientName());
        assertEquals("http://localhost:3000/oauth2/callback", savedClient.getRedirectUris());
        assertEquals("read,write,openid", savedClient.getScopes());
        assertEquals("authorization_code,refresh_token", savedClient.getAuthorizationGrantTypes());
        assertEquals("client_secret_basic,client_secret_post", savedClient.getClientAuthenticationMethods());
        assertEquals(3600, savedClient.getAccessTokenValiditySeconds());
        assertEquals(2592000, savedClient.getRefreshTokenValiditySeconds());
    }

    @Test
    void testInitializeDefaultClient_WhenClientAlreadyExists() {

        Oauth2Clients existingClient = new Oauth2Clients();
        existingClient.setClientId("react-client");
        existingClient.setClientName("Existing Client");

        when(oauth2ClientsRepository.findByClientId("react-client"))
                .thenReturn(Optional.of(existingClient));

        oauth2ClientInitializer.initializeDefaultClient();

        verify(oauth2ClientsRepository, times(1)).findByClientId("react-client");
        verify(passwordEncoder, never()).encode(anyString());
        verify(oauth2ClientsRepository, never()).save(any(Oauth2Clients.class));
    }

    @Test
    void testInitializeDefaultClient_PasswordEncoding() {

        when(oauth2ClientsRepository.findByClientId("react-client"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("react-secret"))
                .thenReturn("{bcrypt}$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
        when(oauth2ClientsRepository.save(any(Oauth2Clients.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        oauth2ClientInitializer.initializeDefaultClient();

        ArgumentCaptor<Oauth2Clients> clientCaptor = ArgumentCaptor.forClass(Oauth2Clients.class);
        verify(oauth2ClientsRepository).save(clientCaptor.capture());

        Oauth2Clients savedClient = clientCaptor.getValue();
        assertTrue(savedClient.getClientSecret().startsWith("{bcrypt}"));
        assertNotEquals("react-secret", savedClient.getClientSecret()); // Secret should be encoded
    }

    @Test
    void testInitializeDefaultClient_CorrectScopes() {

        when(oauth2ClientsRepository.findByClientId("react-client"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString()))
                .thenReturn("{bcrypt}$2a$10$encodedSecret");
        when(oauth2ClientsRepository.save(any(Oauth2Clients.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        oauth2ClientInitializer.initializeDefaultClient();

        ArgumentCaptor<Oauth2Clients> clientCaptor = ArgumentCaptor.forClass(Oauth2Clients.class);
        verify(oauth2ClientsRepository).save(clientCaptor.capture());

        Oauth2Clients savedClient = clientCaptor.getValue();
        String scopes = savedClient.getScopes();
        assertTrue(scopes.contains("read"));
        assertTrue(scopes.contains("write"));
        assertTrue(scopes.contains("openid"));
    }

    @Test
    void testInitializeDefaultClient_CorrectGrantTypes() {

        when(oauth2ClientsRepository.findByClientId("react-client"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString()))
                .thenReturn("{bcrypt}$2a$10$encodedSecret");
        when(oauth2ClientsRepository.save(any(Oauth2Clients.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        oauth2ClientInitializer.initializeDefaultClient();

        ArgumentCaptor<Oauth2Clients> clientCaptor = ArgumentCaptor.forClass(Oauth2Clients.class);
        verify(oauth2ClientsRepository).save(clientCaptor.capture());

        Oauth2Clients savedClient = clientCaptor.getValue();
        String grantTypes = savedClient.getAuthorizationGrantTypes();
        assertTrue(grantTypes.contains("authorization_code"));
        assertTrue(grantTypes.contains("refresh_token"));
    }

    @Test
    void testInitializeDefaultClient_CorrectAuthMethods() {

        when(oauth2ClientsRepository.findByClientId("react-client"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString()))
                .thenReturn("{bcrypt}$2a$10$encodedSecret");
        when(oauth2ClientsRepository.save(any(Oauth2Clients.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        oauth2ClientInitializer.initializeDefaultClient();

        ArgumentCaptor<Oauth2Clients> clientCaptor = ArgumentCaptor.forClass(Oauth2Clients.class);
        verify(oauth2ClientsRepository).save(clientCaptor.capture());

        Oauth2Clients savedClient = clientCaptor.getValue();
        String authMethods = savedClient.getClientAuthenticationMethods();
        assertTrue(authMethods.contains("client_secret_basic"));
        assertTrue(authMethods.contains("client_secret_post"));
    }

    @Test
    void testInitializeDefaultClient_TokenValidity() {

        when(oauth2ClientsRepository.findByClientId("react-client"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString()))
                .thenReturn("{bcrypt}$2a$10$encodedSecret");
        when(oauth2ClientsRepository.save(any(Oauth2Clients.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        oauth2ClientInitializer.initializeDefaultClient();

        ArgumentCaptor<Oauth2Clients> clientCaptor = ArgumentCaptor.forClass(Oauth2Clients.class);
        verify(oauth2ClientsRepository).save(clientCaptor.capture());

        Oauth2Clients savedClient = clientCaptor.getValue();
        assertEquals(3600, savedClient.getAccessTokenValiditySeconds()); // 1 hour
        assertEquals(2592000, savedClient.getRefreshTokenValiditySeconds()); // 30 days
    }

    @Test
    void testInitializeDefaultClient_RedirectUri() {

        when(oauth2ClientsRepository.findByClientId("react-client"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString()))
                .thenReturn("{bcrypt}$2a$10$encodedSecret");
        when(oauth2ClientsRepository.save(any(Oauth2Clients.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        oauth2ClientInitializer.initializeDefaultClient();

        ArgumentCaptor<Oauth2Clients> clientCaptor = ArgumentCaptor.forClass(Oauth2Clients.class);
        verify(oauth2ClientsRepository).save(clientCaptor.capture());

        Oauth2Clients savedClient = clientCaptor.getValue();
        assertEquals("http://localhost:3000/oauth2/callback", savedClient.getRedirectUris());
    }
}
