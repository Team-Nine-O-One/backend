package com.team901.CapstoneDesign.mart.entity;

import com.team901.CapstoneDesign.global.enums.UnitType;
import com.team901.CapstoneDesign.product.entity.Product;
import lombok.Getter;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "mart_product_price")
public class MartProductPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long martProductPriceId;

    @ManyToOne
    @JoinColumn(name = "mart_id")
    private Mart mart;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Double weight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnitType unit;

    @Column(nullable = true)
    private Double pricePer100g;
}