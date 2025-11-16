package com.websiteElectronics.websiteElectronics.Services;

import com.websiteElectronics.websiteElectronics.Dtos.Oauth2ClientsDto;
import com.websiteElectronics.websiteElectronics.Entities.Oauth2Clients;
import com.websiteElectronics.websiteElectronics.Mappers.Oauth2ClientsMapper;
import com.websiteElectronics.websiteElectronics.Repositories.Oauth2ClientsRepository;
import com.websiteElectronics.websiteElectronics.Services.Impl.Oauth2ClientsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class Oauth2ClientsServiceImplTest {

    @Mock
    private Oauth2ClientsRepository oauth2ClientsRepository;

    @Mock
    private Oauth2ClientsMapper oauth2ClientsMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private Oauth2ClientsServiceImpl oauth2ClientsService;

    private Oauth2Clients testClient;
    private Oauth2ClientsDto testClientDto;

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

        testClientDto = new Oauth2ClientsDto();
        testClientDto.setClientId("test-client");
        testClientDto.setClientSecret("plain-secret");
        testClientDto.setClientName("Test Client Application");
        testClientDto.setRedirectUris("http://localhost:3000/callback");
        testClientDto.setScopes("read,write,openid");
        testClientDto.setAuthorizationGrantTypes("authorization_code,refresh_token");
        testClientDto.setClientAuthenticationMethods("client_secret_basic,client_secret_post");
        testClientDto.setAccessTokenValiditySeconds(3600);
        testClientDto.setRefreshTokenValiditySeconds(86400);
    }

    @Test
    void testGetAllClients_Success() {

        List<Oauth2Clients> clients = Arrays.asList(testClient);
        when(oauth2ClientsRepository.findAll()).thenReturn(clients);
        when(oauth2ClientsMapper.toDto(any(Oauth2Clients.class))).thenReturn(testClientDto);

        List<Oauth2ClientsDto> result = oauth2ClientsService.getAllClients();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test-client", result.get(0).getClientId());
        verify(oauth2ClientsRepository, times(1)).findAll();
        verify(oauth2ClientsMapper, times(1)).toDto(any(Oauth2Clients.class));
    }

    @Test
    void testGetClientByClientId_Success() {

        when(oauth2ClientsRepository.findByClientId("test-client"))
                .thenReturn(Optional.of(testClient));
        when(oauth2ClientsMapper.toDto(testClient)).thenReturn(testClientDto);

        Oauth2ClientsDto result = oauth2ClientsService.getClientByClientId("test-client");

        assertNotNull(result);
        assertEquals("test-client", result.getClientId());
        assertEquals("Test Client Application", result.getClientName());
        verify(oauth2ClientsRepository, times(1)).findByClientId("test-client");
        verify(oauth2ClientsMapper, times(1)).toDto(testClient);
    }

    @Test
    void testGetClientByClientId_NotFound() {

        when(oauth2ClientsRepository.findByClientId("non-existent"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            oauth2ClientsService.getClientByClientId("non-existent");
        });

        assertTrue(exception.getMessage().contains("OAuth2 Client not found"));
        verify(oauth2ClientsRepository, times(1)).findByClientId("non-existent");
    }

    @Test
    void testCreateClient_Success() {

        when(oauth2ClientsRepository.findByClientId("test-client"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain-secret"))
                .thenReturn("{bcrypt}$2a$10$encodedSecret");
        when(oauth2ClientsMapper.toEntity(any(Oauth2ClientsDto.class)))
                .thenReturn(testClient);
        when(oauth2ClientsRepository.save(any(Oauth2Clients.class)))
                .thenReturn(testClient);
        when(oauth2ClientsMapper.toDto(testClient))
                .thenReturn(testClientDto);

        Oauth2ClientsDto result = oauth2ClientsService.createClient(testClientDto);

        assertNotNull(result);
        assertEquals("test-client", result.getClientId());
        verify(oauth2ClientsRepository, times(1)).findByClientId("test-client");
        verify(passwordEncoder, times(1)).encode("plain-secret");
        verify(oauth2ClientsRepository, times(1)).save(any(Oauth2Clients.class));
    }

    @Test
    void testCreateClient_AlreadyExists() {

        when(oauth2ClientsRepository.findByClientId("test-client"))
                .thenReturn(Optional.of(testClient));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            oauth2ClientsService.createClient(testClientDto);
        });

        assertTrue(exception.getMessage().contains("Client ID already exists"));
        verify(oauth2ClientsRepository, times(1)).findByClientId("test-client");
        verify(oauth2ClientsRepository, never()).save(any(Oauth2Clients.class));
    }

    @Test
    void testUpdateClient_Success() {

        testClientDto.setClientName("Updated Client Name");
        testClientDto.setClientSecret("new-secret");

        when(oauth2ClientsRepository.findByClientId("test-client"))
                .thenReturn(Optional.of(testClient));
        when(passwordEncoder.encode("new-secret"))
                .thenReturn("{bcrypt}$2a$10$newEncodedSecret");
        when(oauth2ClientsRepository.save(any(Oauth2Clients.class)))
                .thenReturn(testClient);
        when(oauth2ClientsMapper.toDto(testClient))
                .thenReturn(testClientDto);

        Oauth2ClientsDto result = oauth2ClientsService.updateClient("test-client", testClientDto);
        assertNotNull(result);
        assertEquals("test-client", result.getClientId());
        verify(oauth2ClientsRepository, times(1)).findByClientId("test-client");
        verify(passwordEncoder, times(1)).encode("new-secret");
        verify(oauth2ClientsRepository, times(1)).save(any(Oauth2Clients.class));
    }

    @Test
    void testUpdateClient_NotFound() {

        when(oauth2ClientsRepository.findByClientId("non-existent"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            oauth2ClientsService.updateClient("non-existent", testClientDto);
        });

        assertTrue(exception.getMessage().contains("OAuth2 Client not found"));
        verify(oauth2ClientsRepository, times(1)).findByClientId("non-existent");
        verify(oauth2ClientsRepository, never()).save(any(Oauth2Clients.class));
    }

    @Test
    void testUpdateClient_WithoutNewSecret() {

        testClientDto.setClientSecret(null);

        when(oauth2ClientsRepository.findByClientId("test-client"))
                .thenReturn(Optional.of(testClient));
        when(oauth2ClientsRepository.save(any(Oauth2Clients.class)))
                .thenReturn(testClient);
        when(oauth2ClientsMapper.toDto(testClient))
                .thenReturn(testClientDto);

        Oauth2ClientsDto result = oauth2ClientsService.updateClient("test-client", testClientDto);

        assertNotNull(result);
        verify(passwordEncoder, never()).encode(anyString());
        verify(oauth2ClientsRepository, times(1)).save(any(Oauth2Clients.class));
    }

    @Test
    void testDeleteClient_Success() {

        when(oauth2ClientsRepository.findByClientId("test-client"))
                .thenReturn(Optional.of(testClient));
        doNothing().when(oauth2ClientsRepository).delete(testClient);

        oauth2ClientsService.deleteClient("test-client");

        verify(oauth2ClientsRepository, times(1)).findByClientId("test-client");
        verify(oauth2ClientsRepository, times(1)).delete(testClient);
    }

    @Test
    void testDeleteClient_NotFound() {

        when(oauth2ClientsRepository.findByClientId("non-existent"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            oauth2ClientsService.deleteClient("non-existent");
        });

        assertTrue(exception.getMessage().contains("OAuth2 Client not found"));
        verify(oauth2ClientsRepository, times(1)).findByClientId("non-existent");
        verify(oauth2ClientsRepository, never()).delete(any(Oauth2Clients.class));
    }
}
