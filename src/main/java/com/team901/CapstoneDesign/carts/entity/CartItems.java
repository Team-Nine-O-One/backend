package com.team901.CapstoneDesign.carts.entity;

import com.team901.CapstoneDesign.mart.entity.MartProductPrice;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "cart_items")
public class CartItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemsId;

    @ManyToOne
    @JoinColumn(name = "mart_product_price_id")
    private MartProductPrice martProductPrice;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @Column(nullable = false)
    private Double quantity;
}
