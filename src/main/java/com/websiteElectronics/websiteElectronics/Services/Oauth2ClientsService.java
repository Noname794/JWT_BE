package com.websiteElectronics.websiteElectronics.Services;

import com.websiteElectronics.websiteElectronics.Dtos.Oauth2ClientsDto;

import java.util.List;

public interface Oauth2ClientsService {

    List<Oauth2ClientsDto> getAllClients();

    Oauth2ClientsDto getClientByClientId(String clientId);

    Oauth2ClientsDto createClient(Oauth2ClientsDto clientDto);

    Oauth2ClientsDto updateClient(String clientId, Oauth2ClientsDto clientDto);
    
    void deleteClient(String clientId);
}
