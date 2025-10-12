package com.websiteElectronics.websiteElectronics.Services;

import com.websiteElectronics.websiteElectronics.Dtos.ProductsDto;

import java.util.List;

public interface ElectronicsService {
    List<ProductsDto> getAllElectronics();
    ProductsDto getElectronicsById(int id);
    ProductsDto createElectronics(ProductsDto electronicsDto);
    ProductsDto updateElectronics(int id, ProductsDto electronicsDto);
    void deleteElectronics(int id);
}
