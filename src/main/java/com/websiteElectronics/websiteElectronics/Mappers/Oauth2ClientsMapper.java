package com.websiteElectronics.websiteElectronics.Mappers;

import com.websiteElectronics.websiteElectronics.Dtos.Oauth2ClientsDto;
import com.websiteElectronics.websiteElectronics.Entities.Oauth2Clients;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface Oauth2ClientsMapper {

    Oauth2ClientsDto toDto(Oauth2Clients entity);

    Oauth2Clients toEntity(Oauth2ClientsDto dto);
}
