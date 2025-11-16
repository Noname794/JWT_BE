package com.websiteElectronics.websiteElectronics.Entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "orderdetails")
public class OrderDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnoreProperties({"orderDetails", "customer"})
    Orders orderId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonIgnoreProperties({"orderDetails", "reviews", "shoppingCarts"})
    Products productId;
    
    private int quantity;

}
