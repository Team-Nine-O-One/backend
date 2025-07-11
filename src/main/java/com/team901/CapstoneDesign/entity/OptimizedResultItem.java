package com.team901.CapstoneDesign.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class OptimizedResultItem {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "optimized_result_id")
    private OptimizedResult optimizedResult;

    private String memoItemName;
    private String productName;
    private int price;


    public void setMemoItem(MemoItem item) {
    }

    public void setMarket(Market market) {
    }

    public void setProducts(Products product) {
    }

    public void setOnline(boolean b) {
    }
}
