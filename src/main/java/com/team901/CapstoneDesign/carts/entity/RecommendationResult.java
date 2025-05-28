package com.team901.CapstoneDesign.carts.entity;

import com.team901.CapstoneDesign.mart.entity.Mart;
import com.team901.CapstoneDesign.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "recommendation_result")
public class RecommendationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recommendationId;

    @ManyToOne
    @JoinColumn(name = "analysis_id")
    private Analysis analysis;

    @ManyToOne
    @JoinColumn(name = "mart_id")
    private Mart mart;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private Double totalPrice;

    @Column(nullable = true)
    private Double pricePer100g;

    @Column(nullable = true)
    private Double distance;

    @Column(nullable = true)
    private Double deliveryFee;

    @Column(nullable = true)
    private Double score; // 가격 * a + 거리 * b 계산 결과

}