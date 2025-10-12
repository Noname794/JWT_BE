package com.websiteElectronics.websiteElectronics.Entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "shippingmethods")
public class ShippingMethods {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipping_method_id")
    int id;
    String name;
    double cost;
    @Column(name = "estimated_delivery_time")
    String deliveryTime;
}
