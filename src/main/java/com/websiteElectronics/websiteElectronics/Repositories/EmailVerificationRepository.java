package com.websiteElectronics.websiteElectronics.Repositories;

import com.websiteElectronics.websiteElectronics.Entities.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Integer> {
    
    Optional<EmailVerification> findByEmailAndOtpAndVerifiedFalseAndExpiresAtAfter(
            String email, 
            String otp, 
            LocalDateTime currentTime
    );
    
    Optional<EmailVerification> findFirstByEmailAndVerifiedFalseOrderByCreatedAtDesc(String email);
    
    void deleteByExpiresAtBefore(LocalDateTime currentTime);
    
    void deleteByEmail(String email);
}
