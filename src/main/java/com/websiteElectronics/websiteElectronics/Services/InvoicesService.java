package com.websiteElectronics.websiteElectronics.Services;

import com.websiteElectronics.websiteElectronics.Dtos.InvoicesDto;
import com.websiteElectronics.websiteElectronics.Entities.Orders;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


public interface InvoicesService {

    InvoicesDto generateInvoice(Long orderId, Long customerId, String fileUrl, int expireAt);

    CompletableFuture<InvoicesDto> generateAndSendInvoiceAsync(Orders order, int expireMinutes);

    InvoicesDto generateAndSendInvoice(Orders order, int expireMinutes) throws Exception;

    void deleteExpiredInvoices();

    Optional<InvoicesDto> getInvoiceByOrderId(Long orderId);

    List<InvoicesDto> getInvoicesByCustomerId(Long customerId);

    boolean hasInvoice(Long orderId);
}
