package com.websiteElectronics.websiteElectronics.Controllers;

import com.websiteElectronics.websiteElectronics.Controllers.ExportToExcel.CustomersExport;
import com.websiteElectronics.websiteElectronics.Dtos.CustomersDto;
import com.websiteElectronics.websiteElectronics.Services.CustomersService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
//
//@CrossOrigin("*")
@RestController
@RequestMapping("/api/customers")
public class CustomersController {

    private final CustomersService customersService;

    @Autowired
    public CustomersController(CustomersService customersService) {
        this.customersService = customersService;
    }

    @PostMapping
    public ResponseEntity<CustomersDto> createCustomer(@RequestBody CustomersDto customerDto) {
        return ResponseEntity.ok(customersService.createCustomer(customerDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomersDto> updateCustomer(@PathVariable int id, @RequestBody CustomersDto customerDto, Principal principal) {

        return ResponseEntity.ok(customersService.updateCustomer(id, customerDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable int id) {
        customersService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomersDto> getCustomerById(@PathVariable int id, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(customersService.getCustomerById(id));
    }

    @GetMapping
    public ResponseEntity<List<CustomersDto>> getAllCustomers() {
        return ResponseEntity.ok(customersService.getAllCustomers());
    }

    @GetMapping("/export")
    public void exportCustomers(HttpServletResponse response) throws Exception {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=customers.csv");

        List<CustomersDto> customers = customersService.getAllCustomers();
        CustomersExport.exportToCsv(customers, response.getOutputStream());
    }
}
