package com.team901.CapstoneDesign.entity;

import com.team901.CapstoneDesign.global.enums.MartType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@Entity
public class Market {
    @Id @GeneratedValue
    private Long id;

    private String name;

//    @Enumerated(EnumType.STRING)
//    private MarketType type; // ONLINE, OFFLINE

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private MartType type;


    private String address;
    private Double latitude;
    private Double longitude;

    @OneToMany(mappedBy = "market", cascade = CascadeType.ALL)
    private List<Products> products = new ArrayList<>();

    @OneToMany(mappedBy = "market", cascade = CascadeType.ALL)
    private List<MarketCart> marketCarts = new ArrayList<>();

    public boolean isOnline() {
        return this.type.name().equals("ONLINE");
    }
}