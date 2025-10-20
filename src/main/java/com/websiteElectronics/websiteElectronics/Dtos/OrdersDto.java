package com.websiteElectronics.websiteElectronics.Dtos;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.websiteElectronics.websiteElectronics.Entities.Customers;
import com.websiteElectronics.websiteElectronics.Entities.PaymentMethods;
import com.websiteElectronics.websiteElectronics.Entities.ShippingMethods;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdersDto {
    @CsvBindByPosition(position = 0)
    @CsvBindByName(column = "Order Id")
    private int id;

    @CsvBindByPosition(position = 1)
    @CsvBindByName(column = "Order Date")
    private LocalDateTime orderDate;

    @CsvBindByPosition(position = 2)
    @CsvBindByName(column = "Status")
    private String status;

    @CsvBindByPosition(position = 3)
    @CsvBindByName(column = "Total Amount")
    private int totalAmount;

    @CsvBindByPosition(position = 4)
    @CsvBindByName(column = "Customer Id")
    private int customerId;

    @CsvBindByPosition(position = 5)
    @CsvBindByName(column = "Payment Method Id")
    private int paymentMethodId;

    @CsvBindByPosition(position = 6)
    @CsvBindByName(column = "Shipping Method Id")
    private int shippingMethodId;

}
