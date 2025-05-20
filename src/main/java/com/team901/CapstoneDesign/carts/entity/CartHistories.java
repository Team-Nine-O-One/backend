package com.team901.CapstoneDesign.carts.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "cart_histories")
public class CartHistories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @ManyToOne
    @JoinColumn(name = "recommendation_id")
    private RecommendationResult recommendationResult;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private Double totalPrice;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private String marketSummary;

    @Column(nullable = true)
    private String marketImages;
}