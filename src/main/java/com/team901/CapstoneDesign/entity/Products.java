package com.team901.CapstoneDesign.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@Entity
public class Products {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int price;
    private String volumeInfo;
    private String category;

    @ManyToOne
    private Market market;

    private Double pricePer100g;
}
