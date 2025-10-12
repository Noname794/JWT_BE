package com.websiteElectronics.websiteElectronics.Controllers;

import com.websiteElectronics.websiteElectronics.Controllers.ExportToExcel.OrdersDetailExport;
import com.websiteElectronics.websiteElectronics.Dtos.OrderDetailsDto;
import com.websiteElectronics.websiteElectronics.Entities.Products;
import com.websiteElectronics.websiteElectronics.Services.OrderDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@RestController
//@CrossOrigin("*")
@RequestMapping("/api/orderDetails")
public class OrderDetailsController {
    private final OrderDetailsService orderDetailsService;

    @Autowired
    public OrderDetailsController(OrderDetailsService orderDetailsService) {
        this.orderDetailsService = orderDetailsService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDetailsDto>> getAllOrderDetails() {
        List<OrderDetailsDto> lstOrderDetails = orderDetailsService.lstOrderDetails();
        return ResponseEntity.ok(lstOrderDetails);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailsDto> getOrderDetailsById(@PathVariable Integer id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(orderDetailsService.getOrderDetailsById(id));
    }

    @GetMapping("/getProduct/{customerId}")
    public ResponseEntity<List<Integer>> getProductIdsByCustomerId(@PathVariable Integer customerId) {
        if (customerId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(orderDetailsService.getProductIdsByCustomerId(customerId));
    }

    @GetMapping("/getProducts/{customerId}")
    public ResponseEntity<List<Products>> getProductsByCustomerId(@PathVariable Integer customerId) {
        if (customerId == null) {
            return ResponseEntity.badRequest().build();
        }
        List<Products> products = orderDetailsService.getElectronicsByCustomerId(customerId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("export/excel")
    public void exportOrderDetails(HttpServletResponse response) throws Exception {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=orderDetails.csv");

        List<OrderDetailsDto> orderDetails = orderDetailsService.lstOrderDetails();
        OrdersDetailExport.exportToCsv(orderDetails, response.getOutputStream());
    }
}
