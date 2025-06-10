package com.team901.CapstoneDesign.product.entity;


import com.team901.CapstoneDesign.entity.Market;
import com.team901.CapstoneDesign.mart.entity.Mart;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String category;

    @Column(nullable = true)
    private String volume;

    @Column(nullable = true)
    private Double price;

    @Column(nullable = true)
    private String imageUrl;

    @Column(name = "price_per100g", nullable = true)
    private Double pricePer100g;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mart_id")
    private Market mart;
}


