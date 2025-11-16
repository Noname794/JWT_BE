package com.websiteElectronics.websiteElectronics.Services.Impl;

import com.websiteElectronics.websiteElectronics.Dtos.RegisterRequest;
import com.websiteElectronics.websiteElectronics.Entities.Customers;
import com.websiteElectronics.websiteElectronics.Entities.EmailVerification;
import com.websiteElectronics.websiteElectronics.Repositories.CustomersRepository;
import com.websiteElectronics.websiteElectronics.Repositories.EmailVerificationRepository;
import com.websiteElectronics.websiteElectronics.Services.EmailService;
import com.websiteElectronics.websiteElectronics.Services.EmailVerificationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {
    
    @Autowired
    private EmailVerificationRepository emailVerificationRepository;
    
    @Autowired
    private CustomersRepository customersRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public void sendOtpForRegistration(RegisterRequest registerRequest) throws Exception {

        emailVerificationRepository.deleteByEmail(registerRequest.getEmail());
        
        String otp = generateOtp();
        
        EmailVerification verification = new EmailVerification();
        verification.setEmail(registerRequest.getEmail());
        verification.setOtp(otp);
        verification.setCreatedAt(LocalDateTime.now());
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        verification.setVerified(false);
        
        verification.setFirstName(registerRequest.getFirstName());
        verification.setLastName(registerRequest.getLastName());
        verification.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        
        emailVerificationRepository.save(verification);
        
        emailService.sendOtpEmail(registerRequest.getEmail(), otp);
    }
    
    @Override
    @Transactional
    public boolean verifyOtpAndRegister(String email, String otp) {
        Optional<EmailVerification> verificationOpt = emailVerificationRepository
                .findByEmailAndOtpAndVerifiedFalseAndExpiresAtAfter(email, otp, LocalDateTime.now());
        
        if (verificationOpt.isEmpty()) {
            return false;
        }
        
        EmailVerification verification = verificationOpt.get();
        
        Customers newCustomer = new Customers();
        newCustomer.setFirstName(verification.getFirstName());
        newCustomer.setLastName(verification.getLastName());
        newCustomer.setEmail(verification.getEmail());
        newCustomer.setPassword(verification.getPassword());
        newCustomer.setRole("USER");
        
        customersRepository.save(newCustomer);
        
        verification.setVerified(true);
        emailVerificationRepository.save(verification);
        
        return true;
    }
    
    @Override
    @Transactional
    public void cleanupExpiredOtps() {
        emailVerificationRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
    
    private String generateOtp() {
        Random random = new Random();
        int otp = 1000 + random.nextInt(9000);
        return String.valueOf(otp);
    }
    
    @Override
    @Transactional
    public void sendOtpForPasswordReset(String email) throws Exception {
        Optional<Customers> customerOpt = customersRepository.findByEmail(email);
        if (customerOpt.isEmpty()) {
            throw new RuntimeException("Email not found in system");
        }

        emailVerificationRepository.deleteByEmail(email);

        String otp = generateOtp();
        
        EmailVerification verification = new EmailVerification();
        verification.setEmail(email);
        verification.setOtp(otp);
        verification.setCreatedAt(LocalDateTime.now());
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        verification.setVerified(false);
        
        emailVerificationRepository.save(verification);

        emailService.sendOtpEmail(email, otp);
    }
    
    @Override
    @Transactional
    public boolean verifyResetOtp(String email, String otp) {
        Optional<EmailVerification> verificationOpt = emailVerificationRepository
                .findByEmailAndOtpAndVerifiedFalseAndExpiresAtAfter(email, otp, LocalDateTime.now());
        
        return verificationOpt.isPresent();
    }
    
    @Override
    @Transactional
    public boolean resetPassword(String email, String otp, String newPassword) {
        Optional<EmailVerification> verificationOpt = emailVerificationRepository
                .findByEmailAndOtpAndVerifiedFalseAndExpiresAtAfter(email, otp, LocalDateTime.now());
        
        if (verificationOpt.isEmpty()) {
            return false;
        }

        Optional<Customers> customerOpt = customersRepository.findByEmail(email);
        if (customerOpt.isEmpty()) {
            return false;
        }

        Customers customer = customerOpt.get();
        customer.setPassword(passwordEncoder.encode(newPassword));
        customersRepository.save(customer);

        EmailVerification verification = verificationOpt.get();
        verification.setVerified(true);
        emailVerificationRepository.save(verification);
        
        return true;
    }
}
