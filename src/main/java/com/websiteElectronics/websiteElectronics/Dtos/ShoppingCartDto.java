package com.websiteElectronics.websiteElectronics.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingCartDto {

    private Integer id;
    private Integer customerId;
    private Integer productId;
    private int quantity;
    private Date createdAt;
}
