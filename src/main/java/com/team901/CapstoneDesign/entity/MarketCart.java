package com.team901.CapstoneDesign.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;


@Getter
@Setter
@Entity
public class MarketCart {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Memo memo;

    @ManyToOne
    private Market market;

    private int totalPrice;

    private Double userLat;
    private Double userLng;
    private Double distanceFromUser;

    @OneToMany(mappedBy = "marketCart", cascade = CascadeType.ALL)
    private List<MarketCartItem> cartItems = new ArrayList<>();
}
