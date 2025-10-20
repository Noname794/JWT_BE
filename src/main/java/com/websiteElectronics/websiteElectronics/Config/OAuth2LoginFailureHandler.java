package com.websiteElectronics.websiteElectronics.Config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginFailureHandler.class);
    
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                       HttpServletResponse response,
                                       AuthenticationException exception) throws IOException, ServletException {
        
        logger.error("OAuth2 authentication failed: {}", exception.getMessage(), exception);
        
        String errorMessage = exception.getMessage() != null ? 
                             exception.getMessage() : "OAuth2 authentication failed";
        
        String redirectUrl = String.format(
                "http://localhost:5173/login?error=oauth2_failed&message=%s",
                URLEncoder.encode(errorMessage, StandardCharsets.UTF_8)
        );
        
        response.sendRedirect(redirectUrl);
    }
}
