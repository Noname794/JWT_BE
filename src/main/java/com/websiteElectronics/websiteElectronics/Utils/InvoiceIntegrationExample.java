package com.websiteElectronics.websiteElectronics.Utils;

import com.websiteElectronics.websiteElectronics.Dtos.InvoicesDto;
import com.websiteElectronics.websiteElectronics.Entities.Orders;
import com.websiteElectronics.websiteElectronics.Repositories.OrdersRepository;
import com.websiteElectronics.websiteElectronics.Services.InvoicesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InvoiceIntegrationExample {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceIntegrationExample.class);

    @Autowired
    private InvoicesService invoicesService;

    @Autowired
    private OrdersRepository ordersRepository;

    public void handlePaymentSuccess(int orderId) {
        try {
            logger.info("Processing payment success for order ID: {}", orderId);

            Optional<Orders> orderOptional = ordersRepository.findById(orderId);
            if (orderOptional.isEmpty()) {
                logger.error("Order not found: {}", orderId);
                return;
            }

            Orders order = orderOptional.get();

            order.setStatus("Paid");
            ordersRepository.save(order);
            logger.info("Order status updated to Paid for order ID: {}", orderId);

            InvoicesDto invoice = invoicesService.generateAndSendInvoice(order, 43200);
            logger.info("Invoice generated and sent successfully. Invoice ID: {}", invoice.getId());

        } catch (Exception e) {
            logger.error("Error processing payment success for order ID: {}", orderId, e);
        }
    }

    public void generateInvoiceWithCustomExpiry(int orderId, int expiryDays) {
        try {
            Optional<Orders> orderOptional = ordersRepository.findById(orderId);
            if (orderOptional.isEmpty()) {
                logger.error("Order not found: {}", orderId);
                return;
            }

            Orders order = orderOptional.get();

            if (!"Paid".equalsIgnoreCase(order.getStatus()) &&
                !"Completed".equalsIgnoreCase(order.getStatus())) {
                logger.warn("Cannot generate invoice for unpaid order: {}", orderId);
                return;
            }

            int expiryMinutes = expiryDays * 24 * 60;

            InvoicesDto invoice = invoicesService.generateAndSendInvoice(order, expiryMinutes);
            logger.info("Invoice generated with {} days expiry. Invoice ID: {}", expiryDays, invoice.getId());

        } catch (Exception e) {
            logger.error("Error generating invoice for order ID: {}", orderId, e);
        }
    }

    public void generateInvoicesForMultipleOrders(int[] orderIds) {
        logger.info("Starting batch invoice generation for {} orders", orderIds.length);

        int successCount = 0;
        int failCount = 0;

        for (int orderId : orderIds) {
            try {
                handlePaymentSuccess(orderId);
                successCount++;
            } catch (Exception e) {
                logger.error("Failed to generate invoice for order ID: {}", orderId, e);
                failCount++;
            }
        }

        logger.info("Batch invoice generation completed. Success: {}, Failed: {}",
                    successCount, failCount);
    }

    public void ensureInvoiceExists(int orderId) {
        try {
            Optional<Orders> orderOptional = ordersRepository.findById(orderId);
            if (orderOptional.isEmpty()) {
                return;
            }

            Orders order = orderOptional.get();

            if (order.getStatus().equals("Paid") || order.getStatus().equals("Completed")) {
                InvoicesDto invoice = invoicesService.generateAndSendInvoice(order, 43200);
                logger.info("Invoice ensured for order ID: {}. Invoice ID: {}",
                           orderId, invoice.getId());
            }

        } catch (Exception e) {
            logger.error("Error ensuring invoice for order ID: {}", orderId, e);
        }
    }
}
