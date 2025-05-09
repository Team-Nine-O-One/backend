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
@Table(name = "analysis")
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long analysisId;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Double priceWeight;

    @Column(nullable = false)
    private Double distanceWeight;

    @Column(nullable = true)
    private Boolean isConfirmed;

    @Column(nullable = true)
    private Double userLatitude;

    @Column(nullable = true)
    private Double userLongitude;
}
