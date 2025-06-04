package com.team901.CapstoneDesign.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@Entity
public class MemoItemProduct {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private MemoItem memoItem;

    @ManyToOne
    private Products products;


    @Column(name = "is_cheapest", nullable = false)
    private boolean isCheapest = false;

    @Column(name = "is_recommended", nullable = false)
    private boolean recommended = false;
}
