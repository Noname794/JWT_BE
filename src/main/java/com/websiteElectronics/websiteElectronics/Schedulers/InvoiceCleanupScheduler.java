package com.websiteElectronics.websiteElectronics.Schedulers;

import com.websiteElectronics.websiteElectronics.Services.InvoicesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InvoiceCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceCleanupScheduler.class);

    @Autowired
    private InvoicesService invoicesService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredInvoices() {
        logger.info("Starting scheduled cleanup of expired invoices...");
        try {
            invoicesService.deleteExpiredInvoices();
            logger.info("Scheduled cleanup of expired invoices completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled cleanup of expired invoices", e);
        }
    }

    public void cleanupExpiredInvoicesFrequent() {
        logger.info("Starting frequent cleanup of expired invoices...");
        try {
            invoicesService.deleteExpiredInvoices();
            logger.info("Frequent cleanup of expired invoices completed successfully");
        } catch (Exception e) {
            logger.error("Error during frequent cleanup of expired invoices", e);
        }
    }
}
