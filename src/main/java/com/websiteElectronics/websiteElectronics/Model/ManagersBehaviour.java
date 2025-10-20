package com.websiteElectronics.websiteElectronics.Model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManagersBehaviour {
    @Id
    private int id;
    private int totalOrders;
    private double totalSales;
    private double totalProfit;
}
