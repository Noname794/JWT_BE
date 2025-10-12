package com.websiteElectronics.websiteElectronics.Schedulers;

import com.websiteElectronics.websiteElectronics.Entities.Customers;
import com.websiteElectronics.websiteElectronics.Entities.Orders;
import com.websiteElectronics.websiteElectronics.Repositories.CustomersRepository;
import com.websiteElectronics.websiteElectronics.Repositories.OrdersRepository;
import com.websiteElectronics.websiteElectronics.Services.EmailService;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


@Component
public class EmailMarketingScheduler {

    private static final Logger logger = LoggerFactory.getLogger(EmailMarketingScheduler.class);

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 0 23 * * ?")
    public void sendWeeklyPromotionEmails() {
        logger.info(" Starting weekly promotion email campaign...");

        try {
            List<Customers> allCustomers = customersRepository.findAll();

            if (allCustomers.isEmpty()) {
                logger.info("No customers found to send emails");
                return;
            }

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            String subject = " Khuyến mãi cuối tuần - Giảm giá đến 50%!";

            for (Customers customer : allCustomers) {
                try {
                    String emailContent = buildPromotionEmailContent(customer);

                    emailService.sendInvoiceEmail(
                            customer.getEmail(),
                            subject,
                            emailContent,
                            null
                    );

                    successCount.incrementAndGet();
                    logger.debug("Sent promotion email to: {}", customer.getEmail());

                    Thread.sleep(100);

                } catch (MessagingException e) {
                    failCount.incrementAndGet();
                    logger.error("Failed to send email to: {}", customer.getEmail(), e);
                } catch (InterruptedException e) {
                    logger.error("Thread interrupted", e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            logger.info("Weekly promotion campaign completed - Success: {}, Failed: {}",
                    successCount.get(), failCount.get());

        } catch (Exception e) {
            logger.error(" Error during weekly promotion email campaign", e);
        }
    }

    @Scheduled(cron = "0 0 23 * * ?")
    public void sendInactiveCustomerReminder() {
        logger.info("Starting inactive customer reminder campaign...");

        try {
            List<Customers> allCustomers = customersRepository.findAll();
            LocalDateTime sixtyDaysAgo = LocalDateTime.now().minusDays(1);

            AtomicInteger sentCount = new AtomicInteger(0);

            for (Customers customer : allCustomers) {
                try {
                    List<Orders> recentOrders = ordersRepository.findAll().stream()
                            .filter(order -> order.getCustomer().getId() == customer.getId())
                            .filter(order -> order.getOrderDate().isAfter(sixtyDaysAgo))
                            .toList();

                    if (recentOrders.isEmpty()) {
                        String subject = "Chúng tôi nhớ bạn! Quà tặng đặc biệt dành cho bạn";
                        String emailContent = buildInactiveReminderEmailContent(customer);

                        emailService.sendInvoiceEmail(
                                customer.getEmail(),
                                subject,
                                emailContent,
                                null
                        );

                        sentCount.incrementAndGet();
                        logger.debug("Sent reminder email to inactive customer: {}", customer.getEmail());

                        Thread.sleep(100);
                    }

                } catch (Exception e) {
                    logger.error("Failed to send reminder to: {}", customer.getEmail(), e);
                }
            }

            logger.info("Inactive customer reminder completed - Sent: {} emails", sentCount.get());

        } catch (Exception e) {
            logger.error("Error during inactive customer reminder campaign", e);
        }
    }


    @Scheduled(cron = "0 0 23 * * ?")
    public void sendLoyalCustomerThankYou() {
        logger.info(" Starting loyal customer thank you campaign...");

        try {
            List<Customers> allCustomers = customersRepository.findAll();
            AtomicInteger sentCount = new AtomicInteger(0);

            for (Customers customer : allCustomers) {
                try {
                    long orderCount = ordersRepository.findAll().stream()
                            .filter(order -> order.getCustomer().getId() == customer.getId())
                            .count();

                    if (orderCount >= 5) {
                        String subject = " Cảm ơn bạn - Khách hàng thân thiết của chúng tôi!";
                        String emailContent = buildLoyalCustomerEmailContent(customer, (int) orderCount);

                        emailService.sendInvoiceEmail(
                                customer.getEmail(),
                                subject,
                                emailContent,
                                null
                        );

                        sentCount.incrementAndGet();
                        logger.debug("Sent thank you email to loyal customer: {} (Orders: {})",
                                customer.getEmail(), orderCount);

                        Thread.sleep(100);
                    }

                } catch (Exception e) {
                    logger.error("Failed to send thank you to: {}", customer.getEmail(), e);
                }
            }

            logger.info(" Loyal customer thank you completed - Sent: {} emails", sentCount.get());

        } catch (Exception e) {
            logger.error(" Error during loyal customer thank you campaign", e);
        }
    }


    @Async("taskExecutor")
    public void sendEmailAsync(String to, String subject, String content) {
        try {
            emailService.sendInvoiceEmail(to, subject, content, null);
            logger.debug("Async email sent to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send async email to: {}", to, e);
        }
    }

    private String buildPromotionEmailContent(Customers customer) {
        StringBuilder content = new StringBuilder();
        content.append("<!DOCTYPE html>");
        content.append("<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'></head>");
        content.append("<body style='margin:0;padding:0;font-family:Arial,sans-serif;background-color:#f4f4f4;'>");
        content.append("<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f4f4f4;padding:20px 0;'>");
        content.append("<tr><td align='center'>");
        content.append("<table width='600' cellpadding='0' cellspacing='0' style='background-color:#ffffff;border-radius:10px;box-shadow:0 4px 6px rgba(0,0,0,0.1);'>");

        content.append("<tr><td style='background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);padding:40px 30px;border-radius:10px 10px 0 0;text-align:center;'>");
        content.append("<h2 style='color:#ffffff;margin:0;font-size:28px;'>Xin chào ").append(customer.getFirstName()).append(" ").append(customer.getLastName()).append("! </h2>");
        content.append("</td></tr>");

        content.append("<tr><td style='padding:40px 30px;'>");
        content.append("<p style='color:#333;font-size:16px;line-height:1.6;margin:0 0 20px;'>Chúng tôi có tin tuyệt vời dành cho bạn!</p>");

        content.append("<div style='background-color:#fff3cd;border-left:4px solid #ffc107;padding:20px;margin:20px 0;border-radius:5px;'>");
        content.append("<h3 style='color:#856404;margin:0 0 15px;font-size:20px;'> KHUYẾN MÃI CUỐI TUẦN - GIẢM GIÁ ĐẾN 50%</h3>");
        content.append("<ul style='color:#856404;margin:0;padding-left:20px;'>");
        content.append("<li style='margin-bottom:10px;'> Giảm 30% cho tất cả sản phẩm điện tử</li>");
        content.append("<li style='margin-bottom:10px;'> Giảm 50% cho sản phẩm được chọn</li>");
        content.append("<li style='margin-bottom:0;'> Miễn phí vận chuyển cho đơn hàng trên 500.000đ</li>");
        content.append("</ul></div>");

        content.append("<p style='color:#333;font-size:16px;line-height:1.6;margin:20px 0;'><strong> Thời gian:</strong> Từ thứ 6 đến Chủ nhật tuần này</p>");
        content.append("<p style='color:#333;font-size:16px;line-height:1.6;margin:20px 0;'>Đừng bỏ lỡ cơ hội tuyệt vời này!</p>");

        content.append("<div style='text-align:center;margin:30px 0;'>");
        content.append("<a href='#' style='display:inline-block;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);color:#ffffff;text-decoration:none;padding:15px 40px;border-radius:50px;font-weight:bold;font-size:16px;'>MUA SẮM NGAY</a>");
        content.append("</div>");
        content.append("</td></tr>");

        content.append("<tr><td style='background-color:#f8f9fa;padding:30px;border-radius:0 0 10px 10px;text-align:center;'>");
        content.append("<p style='color:#666;font-size:14px;margin:0 0 10px;'>Trân trọng,<br/><strong>Đội ngũ Electronics Store</strong></p>");
        content.append("<p style='color:#999;font-size:12px;margin:10px 0 0;'>Email này được gửi tự động, vui lòng không trả lời.</p>");
        content.append("</td></tr>");

        content.append("</table></td></tr></table>");
        content.append("</body></html>");
        return content.toString();
    }

    private String buildInactiveReminderEmailContent(Customers customer) {
        StringBuilder content = new StringBuilder();
        content.append("<!DOCTYPE html>");
        content.append("<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'></head>");
        content.append("<body style='margin:0;padding:0;font-family:Arial,sans-serif;background-color:#f4f4f4;'>");
        content.append("<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f4f4f4;padding:20px 0;'>");
        content.append("<tr><td align='center'>");
        content.append("<table width='600' cellpadding='0' cellspacing='0' style='background-color:#ffffff;border-radius:10px;box-shadow:0 4px 6px rgba(0,0,0,0.1);'>");

        content.append("<tr><td style='background:linear-gradient(135deg,#4facfe 0%,#00f2fe 100%);padding:40px 30px;border-radius:10px 10px 0 0;text-align:center;'>");
        content.append("<h2 style='color:#ffffff;margin:0;font-size:28px;'>Chúng tôi nhớ bạn, ").append(customer.getFirstName()).append("! </h2>");
        content.append("</td></tr>");

        content.append("<tr><td style='padding:40px 30px;'>");
        content.append("<p style='color:#333;font-size:16px;line-height:1.6;margin:0 0 20px;'>Đã lâu rồi chúng tôi không thấy bạn ghé thăm cửa hàng.</p>");
        content.append("<p style='color:#333;font-size:16px;line-height:1.6;margin:0 0 20px;'>Để chào đón bạn trở lại, chúng tôi có một món quà đặc biệt:</p>");

        content.append("<div style='background:linear-gradient(135deg,#f093fb 0%,#f5576c 100%);padding:30px;margin:20px 0;border-radius:10px;text-align:center;'>");
        content.append("<h3 style='color:#ffffff;margin:0 0 20px;font-size:22px;'> MÃ GIẢM GIÁ 20%<br/>CHO ĐƠN HÀNG TIẾP THEO</h3>");
        content.append("<div style='background-color:#ffffff;padding:15px;border-radius:5px;display:inline-block;'>");
        content.append("<p style='color:#f5576c;font-size:24px;font-weight:bold;margin:0;letter-spacing:2px;'>WELCOME_BACK_2024</p>");
        content.append("</div></div>");

        content.append("<p style='color:#333;font-size:16px;line-height:1.6;margin:20px 0;'>Hãy quay lại và khám phá những sản phẩm mới nhất của chúng tôi!</p>");
        content.append("<p style='color:#333;font-size:16px;line-height:1.6;margin:20px 0;'>Chúng tôi luôn sẵn sàng phục vụ bạn.</p>");

        content.append("<div style='text-align:center;margin:30px 0;'>");
        content.append("<a href='#' style='display:inline-block;background:linear-gradient(135deg,#4facfe 0%,#00f2fe 100%);color:#ffffff;text-decoration:none;padding:15px 40px;border-radius:50px;font-weight:bold;font-size:16px;'>QUAY LẠI MUA SẮM</a>");
        content.append("</div>");
        content.append("</td></tr>");

        content.append("<tr><td style='background-color:#f8f9fa;padding:30px;border-radius:0 0 10px 10px;text-align:center;'>");
        content.append("<p style='color:#666;font-size:14px;margin:0 0 10px;'>Trân trọng,<br/><strong>Đội ngũ Electronics Store</strong></p>");
        content.append("<p style='color:#999;font-size:12px;margin:10px 0 0;'>Email này được gửi tự động, vui lòng không trả lời.</p>");
        content.append("</td></tr>");

        content.append("</table></td></tr></table>");
        content.append("</body></html>");
        return content.toString();
    }

    private String buildLoyalCustomerEmailContent(Customers customer, int orderCount) {
        StringBuilder content = new StringBuilder();
        content.append("<!DOCTYPE html>");
        content.append("<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'></head>");
        content.append("<body style='margin:0;padding:0;font-family:Arial,sans-serif;background-color:#f4f4f4;'>");
        content.append("<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f4f4f4;padding:20px 0;'>");
        content.append("<tr><td align='center'>");
        content.append("<table width='600' cellpadding='0' cellspacing='0' style='background-color:#ffffff;border-radius:10px;box-shadow:0 4px 6px rgba(0,0,0,0.1);'>");

        content.append("<tr><td style='background:linear-gradient(135deg,#f093fb 0%,#f5576c 100%);padding:40px 30px;border-radius:10px 10px 0 0;text-align:center;'>");
        content.append("<h2 style='color:#ffffff;margin:0;font-size:28px;'>Cảm ơn bạn, ").append(customer.getFirstName()).append("! </h2>");
        content.append("</td></tr>");

        content.append("<tr><td style='padding:40px 30px;'>");
        content.append("<p style='color:#333;font-size:16px;line-height:1.6;margin:0 0 20px;'>Bạn là một trong những khách hàng thân thiết nhất của chúng tôi!</p>");

        content.append("<div style='background-color:#fff3e0;border-left:4px solid #ff9800;padding:20px;margin:20px 0;border-radius:5px;text-align:center;'>");
        content.append("<p style='color:#e65100;font-size:18px;margin:0;'>Với <strong style='font-size:32px;display:block;margin:10px 0;'>").append(orderCount).append(" đơn hàng</strong> đã mua, ");
        content.append("bạn đã trở thành một phần quan trọng trong gia đình Electronics Store.</p>");
        content.append("</div>");

        content.append("<h3 style='color:#f5576c;margin:30px 0 20px;font-size:22px;text-align:center;'> ƯU ĐÃI ĐỘC QUYỀN DÀNH CHO BẠN</h3>");

        content.append("<table width='100%' cellpadding='0' cellspacing='0' style='margin:20px 0;'>");
        content.append("<tr>");
        content.append("<td width='50%' style='padding:10px;'>");
        content.append("<div style='background-color:#e8f5e9;padding:20px;border-radius:8px;text-align:center;height:100px;display:table;width:100%;'>");
        content.append("<div style='display:table-cell;vertical-align:middle;'>");
        content.append("<p style='color:#2e7d32;font-size:32px;margin:0;'></p>");
        content.append("<p style='color:#2e7d32;font-size:14px;margin:5px 0 0;font-weight:bold;'>Giảm 25%</p>");
        content.append("<p style='color:#2e7d32;font-size:12px;margin:5px 0 0;'>Tất cả đơn hàng</p>");
        content.append("</div></div></td>");

        content.append("<td width='50%' style='padding:10px;'>");
        content.append("<div style='background-color:#e3f2fd;padding:20px;border-radius:8px;text-align:center;height:100px;display:table;width:100%;'>");
        content.append("<div style='display:table-cell;vertical-align:middle;'>");
        content.append("<p style='color:#1565c0;font-size:32px;margin:0;'></p>");
        content.append("<p style='color:#1565c0;font-size:14px;margin:5px 0 0;font-weight:bold;'>Hỗ trợ VIP</p>");
        content.append("<p style='color:#1565c0;font-size:12px;margin:5px 0 0;'>Ưu tiên hỗ trợ</p>");
        content.append("</div></div></td>");
        content.append("</tr>");

        content.append("<tr>");
        content.append("<td width='50%' style='padding:10px;'>");
        content.append("<div style='background-color:#fce4ec;padding:20px;border-radius:8px;text-align:center;height:100px;display:table;width:100%;'>");
        content.append("<div style='display:table-cell;vertical-align:middle;'>");
        content.append("<p style='color:#c2185b;font-size:32px;margin:0;'></p>");
        content.append("<p style='color:#c2185b;font-size:14px;margin:5px 0 0;font-weight:bold;'>Freeship</p>");
        content.append("<p style='color:#c2185b;font-size:12px;margin:5px 0 0;'>Mọi đơn hàng</p>");
        content.append("</div></div></td>");

        content.append("<td width='50%' style='padding:10px;'>");
        content.append("<div style='background-color:#f3e5f5;padding:20px;border-radius:8px;text-align:center;height:100px;display:table;width:100%;'>");
        content.append("<div style='display:table-cell;vertical-align:middle;'>");
        content.append("<p style='color:#7b1fa2;font-size:32px;margin:0;'>⚡</p>");
        content.append("<p style='color:#7b1fa2;font-size:14px;margin:5px 0 0;font-weight:bold;'>Ưu tiên</p>");
        content.append("<p style='color:#7b1fa2;font-size:12px;margin:5px 0 0;'>Sản phẩm mới</p>");
        content.append("</div></div></td>");
        content.append("</tr></table>");

        content.append("<p style='color:#333;font-size:16px;line-height:1.6;margin:30px 0 20px;text-align:center;font-style:italic;'>Một lần nữa, xin chân thành cảm ơn sự tin tưởng và ủng hộ của bạn!</p>");

        content.append("<div style='text-align:center;margin:30px 0;'>");
        content.append("<a href='#' style='display:inline-block;background:linear-gradient(135deg,#f093fb 0%,#f5576c 100%);color:#ffffff;text-decoration:none;padding:15px 40px;border-radius:50px;font-weight:bold;font-size:16px;'>KHÁM PHÁ ƯU ĐÃI</a>");
        content.append("</div>");
        content.append("</td></tr>");

        content.append("<tr><td style='background-color:#f8f9fa;padding:30px;border-radius:0 0 10px 10px;text-align:center;'>");
        content.append("<p style='color:#666;font-size:14px;margin:0 0 10px;'>Trân trọng,<br/><strong>Đội ngũ Electronics Store</strong></p>");
        content.append("<p style='color:#999;font-size:12px;margin:10px 0 0;'>Email này được gửi tự động, vui lòng không trả lời.</p>");
        content.append("</td></tr>");

        content.append("</table></td></tr></table>");
        content.append("</body></html>");
        return content.toString();
    }
}
