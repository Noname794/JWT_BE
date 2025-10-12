package com.websiteElectronics.websiteElectronics.Dtos;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.websiteElectronics.websiteElectronics.Entities.Products;
import com.websiteElectronics.websiteElectronics.Entities.Orders;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderDetailsDto {

    @CsvBindByPosition(position = 0)
    @CsvBindByName(column = "Order Details Id")
    private Integer id;

    @CsvBindByPosition(position = 1)
    @CsvBindByName(column = "Order ID")
    @NotNull(message = "Order ID is required")
    private Orders orderId;

    @CsvBindByPosition(position = 2)
    @CsvBindByName(column = "Product ID")
    @NotNull(message = "Product Id is required")
    private Products productId;

    @CsvBindByPosition(position = 3)
    @CsvBindByName(column = "Quantity")
    @NotNull(message = "Quantity is required")
    @Size(min = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
