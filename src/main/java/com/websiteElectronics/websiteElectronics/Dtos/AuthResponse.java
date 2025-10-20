package com.websiteElectronics.websiteElectronics.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String message;
    private String email;
    private List<String> roles;
    private String token;
    private Integer customerId;

    public AuthResponse(String message, String token) {
        this.message = message;
        this.token = token;
    }
    
    public AuthResponse(String message, String token, Integer customerId) {
        this.message = message;
        this.token = token;
        this.customerId = customerId;
    }

}
