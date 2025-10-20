package com.websiteElectronics.websiteElectronics.Dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoriesDto {
    private Integer id;
    private String name;
    private String description;
    private Integer parentId;
}
