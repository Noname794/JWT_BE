package com.websiteElectronics.websiteElectronics.Dtos;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Oauth2ClientsDto {
    
    private String clientId;
    
    private String clientSecret;
    
    private String clientName;
    
    private String redirectUris;
    
    private String scopes;
    
    private String authorizationGrantTypes;
    
    private String clientAuthenticationMethods;
    
    private Integer accessTokenValiditySeconds;
    
    private Integer refreshTokenValiditySeconds;
    
    private Instant createdAt;
    
    private Instant updatedAt;
}
