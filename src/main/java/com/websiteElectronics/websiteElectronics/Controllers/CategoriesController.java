package com.websiteElectronics.websiteElectronics.Controllers;

import com.websiteElectronics.websiteElectronics.Dtos.CategoriesDto;
import com.websiteElectronics.websiteElectronics.Services.CategoriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

//@CrossOrigin("*")
@RestController
@RequestMapping("/api/categories")
public class CategoriesController {

    private final CategoriesService categoriesService;

    @Autowired
    public CategoriesController(CategoriesService categoriesService) {
        this.categoriesService = categoriesService;
    }

    @PostMapping
    public ResponseEntity<CategoriesDto> createCategory(@RequestBody CategoriesDto categoryDto) {
        return ResponseEntity.ok(categoriesService.createCategory(categoryDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriesDto> updateCategory(@PathVariable int id, @RequestBody CategoriesDto categoryDto) {
        return ResponseEntity.ok(categoriesService.updateCategory(id, categoryDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable int id) {
        categoriesService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriesDto> getCategoryById(@PathVariable int id) {
        return ResponseEntity.ok(categoriesService.getCategoryById(id));
    }

    @GetMapping
    public ResponseEntity<List<CategoriesDto>> getAllCategories() {
        return ResponseEntity.ok(categoriesService.getAllCategories());
    }
}
