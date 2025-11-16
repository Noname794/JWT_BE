package com.websiteElectronics.websiteElectronics.Controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/oauth2")
public class OAuth2TestController {

    @GetMapping("/test-token")
    public Map<String, Object> testToken(@AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (jwt != null) {
            response.put("subject", jwt.getSubject());
            response.put("claims", jwt.getClaims());
            response.put("issuedAt", jwt.getIssuedAt());
            response.put("expiresAt", jwt.getExpiresAt());
        }
        
        if (authentication != null) {
            response.put("authenticated", authentication.isAuthenticated());
            response.put("principal", authentication.getName());
            response.put("authorities", authentication.getAuthorities());
        }
        
        return response;
    }

    @GetMapping("/userinfo")
    public Map<String, Object> userInfo(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> userInfo = new HashMap<>();
        
        if (jwt != null) {
            userInfo.put("sub", jwt.getSubject());
            userInfo.put("email", jwt.getClaim("email"));
            userInfo.put("name", jwt.getClaim("name"));
            userInfo.put("roles", jwt.getClaim("roles"));
        }
        
        return userInfo;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "OAuth2 Authorization Server is running");
        return response;
    }
}
