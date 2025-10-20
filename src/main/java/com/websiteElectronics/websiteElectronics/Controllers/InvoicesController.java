package com.websiteElectronics.websiteElectronics.Controllers;

import com.websiteElectronics.websiteElectronics.Dtos.InvoicesDto;
import com.websiteElectronics.websiteElectronics.Entities.Orders;
import com.websiteElectronics.websiteElectronics.Repositories.OrdersRepository;
import com.websiteElectronics.websiteElectronics.Services.InvoicesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/invoices")
//@CrossOrigin(origins = "*")
public class InvoicesController {

    @Autowired
    private InvoicesService invoicesService;

    @Autowired
    private OrdersRepository ordersRepository;

    @PostMapping("/generate/{orderId}")
    public ResponseEntity<?> generateInvoiceForOrder(
            @PathVariable int orderId,
            @RequestParam(defaultValue = "43200") int expireMinutes) {

        try {
            Optional<Orders> orderOptional = ordersRepository.findById(orderId);
            if (orderOptional.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Order not found with ID: " + orderId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Orders order = orderOptional.get();

            if (!"Paid".equalsIgnoreCase(order.getStatus()) &&
                !"Completed".equalsIgnoreCase(order.getStatus())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Order must be paid before generating invoice. Current status: " + order.getStatus());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            InvoicesDto invoice = invoicesService.generateAndSendInvoice(order, expireMinutes);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Invoice generated and sent successfully");
            response.put("invoice", invoice);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to generate invoice: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/cleanup-expired")
    public ResponseEntity<?> cleanupExpiredInvoices() {
        try {
            invoicesService.deleteExpiredInvoices();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Expired invoices cleanup completed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to cleanup expired invoices: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getInvoiceByOrderId(@PathVariable Long orderId) {
        try {
            Optional<InvoicesDto> invoice = invoicesService.getInvoiceByOrderId(orderId);
            if (invoice.isPresent()) {
                return ResponseEntity.ok(invoice.get());
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invoice not found for order ID: " + orderId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get invoice: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getInvoicesByCustomerId(@PathVariable Long customerId) {
        try {
            List<InvoicesDto> invoices = invoicesService.getInvoicesByCustomerId(customerId);
            Map<String, Object> response = new HashMap<>();
            response.put("count", invoices.size());
            response.put("invoices", invoices);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get invoices: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/check/{orderId}")
    public ResponseEntity<?> checkInvoiceExists(@PathVariable Long orderId) {
        try {
            boolean exists = invoicesService.hasInvoice(orderId);
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", orderId);
            response.put("hasInvoice", exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to check invoice: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
