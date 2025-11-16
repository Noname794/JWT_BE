package com.websiteElectronics.websiteElectronics.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.websiteElectronics.websiteElectronics.Dtos.Oauth2ClientsDto;
import com.websiteElectronics.websiteElectronics.Services.Oauth2ClientsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(Oauth2ClientsController.class)
public class Oauth2ClientsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Oauth2ClientsService oauth2ClientsService;

    @Autowired
    private ObjectMapper objectMapper;

    private Oauth2ClientsDto testClientDto;

    @BeforeEach
    void setUp() {
        testClientDto = new Oauth2ClientsDto();
        testClientDto.setClientId("test-client");
        testClientDto.setClientSecret("test-secret");
        testClientDto.setClientName("Test Client Application");
        testClientDto.setRedirectUris("http://localhost:3000/callback");
        testClientDto.setScopes("read,write,openid");
        testClientDto.setAuthorizationGrantTypes("authorization_code,refresh_token");
        testClientDto.setClientAuthenticationMethods("client_secret_basic,client_secret_post");
        testClientDto.setAccessTokenValiditySeconds(3600);
        testClientDto.setRefreshTokenValiditySeconds(86400);
    }

    @Test
    void testGetAllClients_Success() throws Exception {
        List<Oauth2ClientsDto> clients = Arrays.asList(testClientDto);
        Mockito.when(oauth2ClientsService.getAllClients()).thenReturn(clients);

        mockMvc.perform(get("/api/oauth2-clients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientId").value("test-client"))
                .andExpect(jsonPath("$[0].clientName").value("Test Client Application"));
    }

    @Test
    void testGetClientByClientId_Success() throws Exception {
        Mockito.when(oauth2ClientsService.getClientByClientId("test-client"))
                .thenReturn(testClientDto);

        mockMvc.perform(get("/api/oauth2-clients/test-client")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value("test-client"))
                .andExpect(jsonPath("$.clientName").value("Test Client Application"))
                .andExpect(jsonPath("$.scopes").value("read,write,openid"));
    }

    @Test
    void testGetClientByClientId_NotFound() throws Exception {

        Mockito.when(oauth2ClientsService.getClientByClientId("non-existent"))
                .thenThrow(new RuntimeException("OAuth2 Client not found"));

        mockMvc.perform(get("/api/oauth2-clients/non-existent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateClient_Success() throws Exception {
        Mockito.when(oauth2ClientsService.createClient(any(Oauth2ClientsDto.class)))
                .thenReturn(testClientDto);

        mockMvc.perform(post("/api/oauth2-clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testClientDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value("test-client"))
                .andExpect(jsonPath("$.clientName").value("Test Client Application"));
    }

    @Test
    void testCreateClient_AlreadyExists() throws Exception {
        Mockito.when(oauth2ClientsService.createClient(any(Oauth2ClientsDto.class)))
                .thenThrow(new RuntimeException("Client ID already exists: test-client"));

        mockMvc.perform(post("/api/oauth2-clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testClientDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Client ID already exists: test-client"));
    }

    @Test
    void testUpdateClient_Success() throws Exception {

        testClientDto.setClientName("Updated Client Name");
        Mockito.when(oauth2ClientsService.updateClient(eq("test-client"), any(Oauth2ClientsDto.class)))
                .thenReturn(testClientDto);

        mockMvc.perform(put("/api/oauth2-clients/test-client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testClientDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value("test-client"))
                .andExpect(jsonPath("$.clientName").value("Updated Client Name"));
    }

    @Test
    void testUpdateClient_NotFound() throws Exception {
        Mockito.when(oauth2ClientsService.updateClient(eq("non-existent"), any(Oauth2ClientsDto.class)))
                .thenThrow(new RuntimeException("OAuth2 Client not found with id: non-existent"));

        // Act & Assert
        mockMvc.perform(put("/api/oauth2-clients/non-existent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testClientDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteClient_Success() throws Exception {
        Mockito.doNothing().when(oauth2ClientsService).deleteClient("test-client");

        mockMvc.perform(delete("/api/oauth2-clients/test-client")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Client deleted successfully"));
    }

    @Test
    void testDeleteClient_NotFound() throws Exception {
        Mockito.doThrow(new RuntimeException("OAuth2 Client not found with id: non-existent"))
                .when(oauth2ClientsService).deleteClient("non-existent");

        mockMvc.perform(delete("/api/oauth2-clients/non-existent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
