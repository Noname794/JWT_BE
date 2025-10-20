package com.websiteElectronics.websiteElectronics.Services.Impl;

import com.websiteElectronics.websiteElectronics.Collections.Invoices;
import com.websiteElectronics.websiteElectronics.Dtos.InvoicesDto;
import com.websiteElectronics.websiteElectronics.Entities.Orders;
import com.websiteElectronics.websiteElectronics.Mappers.InvoicesMapper;
import com.websiteElectronics.websiteElectronics.Repositories.InvoicesRepository;
import com.websiteElectronics.websiteElectronics.Services.EmailService;
import com.websiteElectronics.websiteElectronics.Services.InvoiceFileService;
import com.websiteElectronics.websiteElectronics.Services.InvoicesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class InvoicesServiceImpl implements InvoicesService {

    private static final Logger logger = LoggerFactory.getLogger(InvoicesServiceImpl.class);

    private final InvoicesRepository invoicesRepository;
    private final InvoiceFileService invoiceFileService;
    private final EmailService emailService;

    @Autowired
    public InvoicesServiceImpl(InvoicesRepository invoicesRepository,
                               InvoiceFileService invoiceFileService,
                               EmailService emailService) {
        this.invoicesRepository = invoicesRepository;
        this.invoiceFileService = invoiceFileService;
        this.emailService = emailService;
    }

    @Override
    public InvoicesDto generateInvoice(Long orderId, Long customerId, String fileUrl, int expireAt) {
        Invoices invoices = new Invoices();
        invoices.setOrderId(orderId);
        invoices.setCustomerId(customerId);
        invoices.setFileUrl(fileUrl);
        invoices.setCreatedAt(LocalDateTime.now());
        invoices.setExpireAt(LocalDateTime.now().plusMinutes(expireAt));

        Invoices savedInvoices = invoicesRepository.save(invoices);
        return InvoicesMapper.mapToDto(savedInvoices);
    }

    @Async("asyncExecutor")
    @Override
    public CompletableFuture<InvoicesDto> generateAndSendInvoiceAsync(Orders order, int expireMinutes) {
        try{
            logger.info("Generating and sending invoice for order {}", order.getId());

            if(invoicesRepository.existsByOrderId((long) order.getId())) {
                Optional<Invoices> existingInvoice = invoicesRepository.findByOrderId((long) order.getId());
                if(existingInvoice.isPresent()) {
                    return CompletableFuture.completedFuture(InvoicesMapper.mapToDto(existingInvoice.get()));
                }
            }

            String filePath = invoiceFileService.createInvoiceFile(order);

            Invoices invoice = new Invoices();
            invoice.setOrderId((long) order.getId());
            invoice.setCustomerId((long) order.getCustomer().getId());
            invoice.setFileUrl(filePath);
            invoice.setCreatedAt(LocalDateTime.now());
            invoice.setExpireAt(LocalDateTime.now().plusMinutes(expireMinutes));

            Invoices savedInvoice = invoicesRepository.save(invoice);

            String customerEmail = order.getCustomer().getEmail();
            String subject = "Hóa đơn đơn hàng:" + order.getId();
            String emailContent = buildHtmlContent(order);

            emailService.sendInvoiceEmail(customerEmail, subject, emailContent, filePath);

            return CompletableFuture.completedFuture(InvoicesMapper.mapToDto(savedInvoice));

        }catch (Exception e) {
            logger.error("Error generating and sending invoice for order {}", order.getId(), e);
            return CompletableFuture.failedFuture(e);}

    }

    @Override
    public InvoicesDto generateAndSendInvoice(Orders order, int expireMinutes) throws Exception {
        try {
            if (invoicesRepository.existsByOrderId((long) order.getId())) {
                Optional<Invoices> existingInvoice = invoicesRepository.findByOrderId((long) order.getId());
                if (existingInvoice.isPresent()) {
                    return InvoicesMapper.mapToDto(existingInvoice.get());
                }
            }

            String filePath = invoiceFileService.createInvoiceFile(order);

            Invoices invoice = new Invoices();
            invoice.setOrderId((long) order.getId());
            invoice.setCustomerId((long) order.getCustomer().getId());
            invoice.setFileUrl(filePath);
            invoice.setCreatedAt(LocalDateTime.now());
            invoice.setExpireAt(LocalDateTime.now().plusMinutes(expireMinutes));

            Invoices savedInvoice = invoicesRepository.save(invoice);

            String customerEmail = order.getCustomer().getEmail();
            String subject = "Hóa đơn đơn hàng:" + order.getId();
            String emailContent = buildHtmlContent(order);

            emailService.sendInvoiceEmail(customerEmail, subject, emailContent, filePath);

            return InvoicesMapper.mapToDto(savedInvoice);

        } catch (Exception e) {
            logger.error("Could not generate and send invoice for order ID: {}", order.getId(), e);
            throw new Exception("Failed to generate and send invoice: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteExpiredInvoices() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Invoices> expiredInvoices = invoicesRepository.findByExpireAtBefore(now);

            logger.info("Found {} expired invoices to delete", expiredInvoices.size());

            for (Invoices invoice : expiredInvoices) {
                if (invoice.getFileUrl() != null && !invoice.getFileUrl().isEmpty()) {
                    try {
                        File file = new File(invoice.getFileUrl());
                        if (file.exists()) {
                            Files.delete(Paths.get(invoice.getFileUrl()));
                            logger.info("Deleted invoice file: {}", invoice.getFileUrl());
                        }
                    } catch (Exception e) {
                        logger.error("Error deleting invoice file: {}", invoice.getFileUrl(), e);
                    }
                }

                invoicesRepository.delete(invoice);
                logger.info("Deleted invoice metadata from MongoDB: {}", invoice.getId());
            }

            logger.info("Completed deletion of {} expired invoices", expiredInvoices.size());

        } catch (Exception e) {
            logger.error("Error during expired invoices cleanup", e);
        }
    }

    String buildHtmlContent(Orders order) {
        StringBuilder content = new StringBuilder();
        content.append("<!DOCTYPE html>");
        content.append("<html lang='vi'>");
        content.append("<head>");
        content.append("<meta charset='UTF-8'>");
        content.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        content.append("<style>");
        content.append("* { margin: 0; padding: 0; box-sizing: border-box; }");
        content.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 40px 20px; }");
        content.append(".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 20px; overflow: hidden; box-shadow: 0 20px 60px rgba(0,0,0,0.3); }");
        content.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 40px 30px; text-align: center; position: relative; }");
        content.append(".header::before { content: ''; position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: url('data:image/svg+xml,<svg width=\"100\" height=\"100\" xmlns=\"http://www.w3.org/2000/svg\"><circle cx=\"10\" cy=\"10\" r=\"2\" fill=\"white\" opacity=\"0.1\"/></svg>'); }");
        content.append(".header h1 { color: white; font-size: 28px; margin-bottom: 10px; position: relative; z-index: 1; text-shadow: 2px 2px 4px rgba(0,0,0,0.2); }");
        content.append(".checkmark { width: 80px; height: 80px; background: white; border-radius: 50%; margin: 0 auto 20px; display: flex; align-items: center; justify-content: center; box-shadow: 0 5px 15px rgba(0,0,0,0.2); position: relative; z-index: 1; animation: scaleIn 0.5s ease-out; }");
        content.append("@keyframes scaleIn { from { transform: scale(0); } to { transform: scale(1); } }");
        content.append(".checkmark svg { width: 45px; height: 45px; }");
        content.append(".content { padding: 40px 30px; }");
        content.append(".greeting { font-size: 24px; color: #2d3748; margin-bottom: 20px; font-weight: 600; }");
        content.append(".message { color: #4a5568; font-size: 16px; line-height: 1.6; margin-bottom: 15px; }");
        content.append(".order-section { background: linear-gradient(135deg, #f6f8fb 0%, #e9ecef 100%); border-radius: 15px; padding: 25px; margin: 30px 0; border-left: 5px solid #667eea; }");
        content.append(".order-title { color: #667eea; font-size: 20px; margin-bottom: 20px; font-weight: 600; display: flex; align-items: center; }");
        content.append(".order-title::before { content: '📦'; margin-right: 10px; font-size: 24px; }");
        content.append(".order-info { background: white; border-radius: 10px; padding: 20px; margin-top: 15px; }");
        content.append(".info-row { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid #e2e8f0; }");
        content.append(".info-row:last-child { border-bottom: none; }");
        content.append(".info-label { color: #718096; font-weight: 500; }");
        content.append(".info-value { color: #2d3748; font-weight: 600; }");
        content.append(".total { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white !important; margin: -20px -20px 20px -20px; padding: 15px 20px; border-radius: 10px 10px 0 0; }");
        content.append(".total .info-label, .total .info-value { color: white; font-size: 18px; }");
        content.append(".status-badge { background: #48bb78; color: white; padding: 5px 15px; border-radius: 20px; font-size: 14px; font-weight: 600; display: inline-block; }");
        content.append(".footer { background: #f7fafc; padding: 30px; text-align: center; color: #718096; border-top: 3px solid #e2e8f0; }");
        content.append(".footer-message { margin-bottom: 15px; font-size: 15px; line-height: 1.6; }");
        content.append(".signature { font-weight: 600; color: #667eea; margin-top: 20px; }");
        content.append(".highlight { background: linear-gradient(120deg, #ffd89b 0%, #19547b 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; font-weight: 700; }");
        content.append("</style>");
        content.append("</head>");
        content.append("<body>");
        content.append("<div class='container'>");

        // Header
        content.append("<div class='header'>");
        content.append("<div class='checkmark'>");
        content.append("<svg viewBox='0 0 24 24' fill='none' stroke='#667eea' stroke-width='3' stroke-linecap='round' stroke-linejoin='round'>");
        content.append("<polyline points='20 6 9 17 4 12'></polyline>");
        content.append("</svg>");
        content.append("</div>");
        content.append("<h1>Đơn Hàng Thành Công!</h1>");
        content.append("</div>");

        // Content
        content.append("<div class='content'>");
        content.append("<div class='greeting'>Xin chào <span class='highlight'>")
                .append(order.getCustomer().getFirstName())
                .append(" ").append(order.getCustomer().getLastName())
                .append("</span>,</div>");

        content.append("<p class='message'>Cảm ơn bạn đã tin tưởng và mua sắm tại cửa hàng của chúng tôi! 🎉</p>");
        content.append("<p class='message'>Đơn hàng của bạn đã được thanh toán và xác nhận thành công. Chúng tôi đang chuẩn bị để giao hàng đến bạn trong thời gian sớm nhất.</p>");

        // Order Section
        content.append("<div class='order-section'>");
        content.append("<div class='order-title'>Thông Tin Đơn Hàng</div>");
        content.append("<div class='order-info'>");

        content.append("<div class='info-row total'>");
        content.append("<span class='info-label'>Tổng Thanh Toán</span>");
        content.append("<span class='info-value'>").append(String.format("%,d VNĐ", order.getTotalAmount())).append("</span>");
        content.append("</div>");

        content.append("<div class='info-row'>");
        content.append("<span class='info-label'>Mã Đơn Hàng</span>");
        content.append("<span class='info-value'>#").append(order.getId()).append("</span>");
        content.append("</div>");

        content.append("<div class='info-row'>");
        content.append("<span class='info-label'>Trạng Thái</span>");
        content.append("<span class='info-value'><span class='status-badge'>").append(order.getStatus()).append("</span></span>");
        content.append("</div>");

        content.append("</div>");
        content.append("</div>");

        content.append("<p class='message' style='margin-top: 25px;'>📄 Hóa đơn chi tiết đã được đính kèm trong email này để bạn tiện theo dõi.</p>");
        content.append("</div>");

        // Footer
        content.append("<div class='footer'>");
        content.append("<div class='footer-message'>Nếu bạn có bất kỳ câu hỏi nào, đừng ngần ngại liên hệ với chúng tôi.<br>Chúng tôi luôn sẵn sàng hỗ trợ bạn! 💬</div>");
        content.append("<div class='signature'>Trân trọng,<br/>Đội Ngũ Hỗ Trợ Khách Hàng</div>");
        content.append("</div>");

        content.append("</div>");
        content.append("</body>");
        content.append("</html>");

        return content.toString();
    }

    @Override
    public Optional<InvoicesDto> getInvoiceByOrderId(Long orderId) {
        try {
            Optional<Invoices> invoice = invoicesRepository.findByOrderId(orderId);
            return invoice.map(InvoicesMapper::mapToDto);
        } catch (Exception e) {
            logger.error("Could not find invoice for order ID: {}", orderId, e);
            return Optional.empty();
        }
    }

    @Override
    public List<InvoicesDto> getInvoicesByCustomerId(Long customerId) {
        try {
            List<Invoices> invoices = invoicesRepository.findByCustomerId(customerId);
            return invoices.parallelStream()
                    .map(InvoicesMapper::mapToDto)
                    .toList();
        } catch (Exception e) {
            logger.error("Could not get invoices for customer ID: {}", customerId, e);
            return List.of();
        }
    }

    @Override
    public boolean hasInvoice(Long orderId) {
        try {
            return invoicesRepository.existsByOrderId(orderId);
        } catch (Exception e) {
            logger.error("Could not find invoice for order ID: {}", orderId, e);
            return false;
        }
    }
}
