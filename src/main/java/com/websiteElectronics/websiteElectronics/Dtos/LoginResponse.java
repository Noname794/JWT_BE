package com.websiteElectronics.websiteElectronics.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String message;
    private String email;
    private Integer customerId;
    private List<String> roles;
    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
}
