package com.websiteElectronics.websiteElectronics.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "Orders")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    int id;

    @Column(name = "order_date")
    LocalDateTime orderDate;
    String status;

    @Column(name = "total_amount")
    int totalAmount;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    Customers customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "payment_method_id", nullable = false)
    PaymentMethods paymentMethod;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shipping_method_id", nullable = false)
    ShippingMethods shippingMethod;

}
