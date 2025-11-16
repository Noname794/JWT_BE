package com.websiteElectronics.websiteElectronics.Services;

import com.websiteElectronics.websiteElectronics.Dtos.RegisterRequest;

public interface EmailVerificationService {
    
    void sendOtpForRegistration(RegisterRequest registerRequest) throws Exception;
    
    boolean verifyOtpAndRegister(String email, String otp);
    
    void cleanupExpiredOtps();

    void sendOtpForPasswordReset(String email) throws Exception;
    
    boolean verifyResetOtp(String email, String otp);
    
    boolean resetPassword(String email, String otp, String newPassword);
}
