package com.websiteElectronics.websiteElectronics.Schedulers;

import com.websiteElectronics.websiteElectronics.Services.EmailVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EmailVerificationScheduler {
    
    @Autowired
    private EmailVerificationService emailVerificationService;

    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredOtps() {
        emailVerificationService.cleanupExpiredOtps();
    }
}
