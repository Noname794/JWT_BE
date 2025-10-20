package com.websiteElectronics.websiteElectronics.Services;

import com.websiteElectronics.websiteElectronics.Dtos.CategoriesDto;
import java.util.List;

public interface CategoriesService {
    CategoriesDto createCategory(CategoriesDto categoryDto);
    CategoriesDto updateCategory(int id, CategoriesDto categoryDto);
    void deleteCategory(int id);
    CategoriesDto getCategoryById(int id);
    List<CategoriesDto> getAllCategories();
}
