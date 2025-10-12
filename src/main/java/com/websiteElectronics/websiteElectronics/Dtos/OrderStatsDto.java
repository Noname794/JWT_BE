package com.websiteElectronics.websiteElectronics.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatsDto {
    private Long totalOrders;
    private Double totalAmountSpent;

}
