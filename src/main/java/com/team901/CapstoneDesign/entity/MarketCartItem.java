package com.team901.CapstoneDesign.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@Entity
public class MarketCartItem {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private MarketCart marketCart;

    @ManyToOne
    private MemoItem memoItem;

    @ManyToOne
    private Products products;

    private int price;
}