package com.team901.CapstoneDesign.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class OptimizedResultItem {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private OptimizedResult optimizedResult;

    private String memoItemName;
    private String productName;
    private int price;
}
