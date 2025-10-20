package com.websiteElectronics.websiteElectronics.Mappers;

import com.websiteElectronics.websiteElectronics.Dtos.CategoriesDto;
import com.websiteElectronics.websiteElectronics.Entities.Categories;

public class CategoriesMapper {
    public static CategoriesDto toDto(Categories category) {
        return new CategoriesDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getParentId()
        );
    }

    public static Categories toEntity(CategoriesDto dto) {
        if (dto == null) return null;
        return new Categories(
                dto.getId(),
                dto.getName(),
                dto.getDescription(),
                dto.getParentId()
        );
    }
}
