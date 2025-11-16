package com.websiteElectronics.websiteElectronics.Services;

import com.websiteElectronics.websiteElectronics.Dtos.LoginResponse;
import com.websiteElectronics.websiteElectronics.Dtos.RegisterRequest;
import com.websiteElectronics.websiteElectronics.Dtos.LoginRequest;
import com.websiteElectronics.websiteElectronics.Dtos.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    LoginResponse verifyGg(String token);
    LoginResponse refreshToken(String token);
    boolean validateToken(String token);
}
