package com.team901.CapstoneDesign.mart.entity;

import com.team901.CapstoneDesign.global.enums.MartType;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "mart")
public class Mart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long martId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MartType type;

    @Column(nullable = true)
    private String location;

    @Column(nullable = true)
    private Double latitude;

    @Column(nullable = true)
    private Double longitude;

    @Column(nullable = true)
    private Long deliveryFee;

    @Column(nullable = true)
    private String logoUrl;

    @Column(nullable = true)
    private String homepageUrl;
}
