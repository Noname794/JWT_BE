package com.websiteElectronics.websiteElectronics.Services.Impl;

import com.websiteElectronics.websiteElectronics.Dtos.Oauth2ClientsDto;
import com.websiteElectronics.websiteElectronics.Entities.Oauth2Clients;
import com.websiteElectronics.websiteElectronics.Mappers.Oauth2ClientsMapper;
import com.websiteElectronics.websiteElectronics.Repositories.Oauth2ClientsRepository;
import com.websiteElectronics.websiteElectronics.Services.Oauth2ClientsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class Oauth2ClientsServiceImpl implements Oauth2ClientsService {

    @Autowired
    private Oauth2ClientsRepository oauth2ClientsRepository;

    @Autowired
    private Oauth2ClientsMapper oauth2ClientsMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<Oauth2ClientsDto> getAllClients() {
        return oauth2ClientsRepository.findAll().stream()
                .map(oauth2ClientsMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Oauth2ClientsDto getClientByClientId(String clientId) {
        Oauth2Clients client = oauth2ClientsRepository.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("OAuth2 Client not found with id: " + clientId));
        return oauth2ClientsMapper.toDto(client);
    }

    @Override
    public Oauth2ClientsDto createClient(Oauth2ClientsDto clientDto) {
        if (oauth2ClientsRepository.findByClientId(clientDto.getClientId()).isPresent()) {
            throw new RuntimeException("Client ID already exists: " + clientDto.getClientId());
        }

        if (clientDto.getClientSecret() != null && !clientDto.getClientSecret().isEmpty()) {
            clientDto.setClientSecret(passwordEncoder.encode(clientDto.getClientSecret()));
        }

        Oauth2Clients client = oauth2ClientsMapper.toEntity(clientDto);
        Oauth2Clients savedClient = oauth2ClientsRepository.save(client);
        return oauth2ClientsMapper.toDto(savedClient);
    }

    @Override
    public Oauth2ClientsDto updateClient(String clientId, Oauth2ClientsDto clientDto) {
        Oauth2Clients existingClient = oauth2ClientsRepository.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("OAuth2 Client not found with id: " + clientId));

        existingClient.setClientName(clientDto.getClientName());
        existingClient.setRedirectUris(clientDto.getRedirectUris());
        existingClient.setScopes(clientDto.getScopes());
        existingClient.setAuthorizationGrantTypes(clientDto.getAuthorizationGrantTypes());
        existingClient.setClientAuthenticationMethods(clientDto.getClientAuthenticationMethods());
        existingClient.setAccessTokenValiditySeconds(clientDto.getAccessTokenValiditySeconds());
        existingClient.setRefreshTokenValiditySeconds(clientDto.getRefreshTokenValiditySeconds());

        if (clientDto.getClientSecret() != null && !clientDto.getClientSecret().isEmpty()) {
            existingClient.setClientSecret(passwordEncoder.encode(clientDto.getClientSecret()));
        }

        Oauth2Clients updatedClient = oauth2ClientsRepository.save(existingClient);
        return oauth2ClientsMapper.toDto(updatedClient);
    }

    @Override
    public void deleteClient(String clientId) {
        Oauth2Clients client = oauth2ClientsRepository.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("OAuth2 Client not found with id: " + clientId));
        oauth2ClientsRepository.delete(client);
    }
}
