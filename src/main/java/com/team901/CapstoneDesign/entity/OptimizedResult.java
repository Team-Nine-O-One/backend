package com.team901.CapstoneDesign.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
@Entity
public class OptimizedResult {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Memo memo;

    @OneToMany(mappedBy = "optimizedResult", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OptimizedResultItem> items = new ArrayList<>();

    private String marketName;
    private int totalPrice;
    private Double distance;

    private LocalDateTime createdAt = LocalDateTime.now();
}