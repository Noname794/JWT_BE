package com.websiteElectronics.websiteElectronics.Services.Impl;

import com.websiteElectronics.websiteElectronics.Services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendInvoiceEmail(String to, String subject, String content, String filePath) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);

        if (filePath != null && !filePath.isEmpty()) {
            FileSystemResource file = new FileSystemResource(new File(filePath));
            helper.addAttachment("HoaDon_" + System.currentTimeMillis() + ".txt", file);
        }

        mailSender.send(message);
    }
    
    @Override
    public void sendOtpEmail(String to, String otp) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setTo(to);
        helper.setSubject("Xác thực đăng ký tài khoản");
        
        String content = "<html><body>" +
                "<h2>Xác thực đăng ký tài khoản</h2>" +
                "<p>Mã OTP của bạn là: <strong style='font-size: 24px; color: #007bff;'>" + otp + "</strong></p>" +
                "<p>Mã này sẽ hết hạn sau 5 phút.</p>" +
                "<p>Nếu bạn không yêu cầu đăng ký, vui lòng bỏ qua email này.</p>" +
                "</body></html>";
        
        helper.setText(content, true);
        
        mailSender.send(message);
    }
}
