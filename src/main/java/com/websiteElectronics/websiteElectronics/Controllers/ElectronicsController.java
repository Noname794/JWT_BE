package com.websiteElectronics.websiteElectronics.Controllers;

import com.websiteElectronics.websiteElectronics.Controllers.ExportToExcel.ProductsExport;
import com.websiteElectronics.websiteElectronics.Dtos.ProductsDto;
import com.websiteElectronics.websiteElectronics.Services.ElectronicsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

//@CrossOrigin("*")
@RestController
@RequestMapping("/api/electronics")
public class ElectronicsController {
    private final ElectronicsService electronicsService;

    @Autowired
    public ElectronicsController(ElectronicsService electronicsService) {
        this.electronicsService = electronicsService;
    }

    @GetMapping
    public ResponseEntity<List<ProductsDto>> getAllElectronics() {
        return ResponseEntity.ok(electronicsService.getAllElectronics());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductsDto> getElectronicsById(@PathVariable Integer id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(electronicsService.getElectronicsById(id));
    }

    @PostMapping
    public ResponseEntity<ProductsDto> createElectronics(@RequestBody ProductsDto electronicsDto) {
        return ResponseEntity.ok(electronicsService.createElectronics(electronicsDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductsDto> updateElectronics(@PathVariable int id, @RequestBody ProductsDto electronicsDto) {
        return ResponseEntity.ok(electronicsService.updateElectronics(id, electronicsDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteElectronics(@PathVariable int id) {
        electronicsService.deleteElectronics(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export/excel")
    public void exportProducts(HttpServletResponse response) throws Exception {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=electronics.csv");

        List<ProductsDto> electronics = electronicsService.getAllElectronics();
        ProductsExport.exportToCsv(electronics, response.getOutputStream());
    }
}
