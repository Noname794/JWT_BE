package com.websiteElectronics.websiteElectronics.Services;

import com.websiteElectronics.websiteElectronics.Dtos.RegisterRequest;
import com.websiteElectronics.websiteElectronics.Dtos.LoginRequest;
import com.websiteElectronics.websiteElectronics.Dtos.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
