package com.websiteElectronics.websiteElectronics.Services;

import jakarta.mail.MessagingException;


public interface EmailService {
    void sendInvoiceEmail(String to, String subject, String content, String filePath) throws MessagingException;
}
