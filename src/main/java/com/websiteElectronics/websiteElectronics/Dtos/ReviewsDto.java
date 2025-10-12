package com.websiteElectronics.websiteElectronics.Dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewsDto {
    private int id;
    private int rating;
    private String comment;
    private String reviewDate;
    private int productId;
    private int customerId;
    private String productName;
    private String customerName;

}
