package com.websiteElectronics.websiteElectronics.Entities;

import jakarta.persistence.*;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class OrderDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    Orders orderId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    Products productId;
    private int quantity;

}
