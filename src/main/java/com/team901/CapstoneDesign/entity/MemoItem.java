package com.team901.CapstoneDesign.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;


@Getter
@Setter
@Entity
public class MemoItem {
    @Id @GeneratedValue
    private Long id;

    private String name;
    private String quantity;

    @ManyToOne
    private Memo memo;

    @OneToMany(mappedBy = "memoItem", cascade = CascadeType.ALL)
    private List<MemoItemProduct> relatedProducts = new ArrayList<>();

    @OneToMany(mappedBy = "memoItem", cascade = CascadeType.ALL)
    private List<MarketCartItem> marketCartItems = new ArrayList<>();

    @Column(nullable = false)
    private boolean purchasedOnline= false;

    public void setPurchasedOnline(boolean b) {
        this.purchasedOnline = purchasedOnline;
    }
    public boolean isPurchasedOnline() {
        return this.purchasedOnline;
    }
}
