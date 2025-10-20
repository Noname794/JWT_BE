package com.websiteElectronics.websiteElectronics.Dtos;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.websiteElectronics.websiteElectronics.Entities.Categories;
import com.websiteElectronics.websiteElectronics.Entities.Suppliers;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductsDto {

    @CsvBindByPosition(position = 0)
    @CsvBindByName(column = "Product Id")
    private int id;

    @CsvBindByPosition(position = 1)
    @CsvBindByName(column = "Name")
    @NotNull(message = "Name is required")
    @Size(min = 5, message = "Name must be at least 5 characters")
    private String name;

    @CsvBindByPosition(position = 2)
    @CsvBindByName(column = "Description")
    @NotNull(message = "Description is required")
    private String description;

    @CsvBindByPosition(position = 3)
    @CsvBindByName(column = "Price")
    @NotNull(message = "Price is required")
    private int price;

    @CsvBindByPosition(position = 4)
    @CsvBindByName(column = "Quantity")
    @NotNull(message = "Quantity is required")
    private int quantity;

    @CsvBindByPosition(position = 5)
    @CsvBindByName(column = "Image URL")
    @NotNull(message = "ImageUrl is required")
    private String imageUrl;

    @CsvBindByPosition(position = 6)
    @CsvBindByName(column = "Category Id")
    @NotNull(message = "Category is required")
    private Categories category;

    @CsvBindByPosition(position = 7)
    @CsvBindByName(column = "Supplier Id")
    @NotNull(message = "Supplier is required")
    private Suppliers supplier;

}
