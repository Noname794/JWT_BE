package com.websiteElectronics.websiteElectronics.Mappers;

import com.websiteElectronics.websiteElectronics.Dtos.ProductsDto;
import com.websiteElectronics.websiteElectronics.Entities.Products;

public class ElectronicsMapper {

    public static ProductsDto mapToDto(Products electronics) {
        return new ProductsDto(
                electronics.getId(),
                electronics.getName(),
                electronics.getDescription(),
                electronics.getPrice(),
                electronics.getQuantity(),
                electronics.getImageUrl(),
                electronics.getCategory(),
                electronics.getSupplier()
        );
    }

    public static Products mapToEntity(ProductsDto electronicsDto) {
        return new Products(
                electronicsDto.getId(),
                electronicsDto.getName(),
                electronicsDto.getDescription(),
                electronicsDto.getPrice(),
                electronicsDto.getQuantity(),
                electronicsDto.getImageUrl(),
                electronicsDto.getCategory(),
                electronicsDto.getSupplier()
        );
    }

}
