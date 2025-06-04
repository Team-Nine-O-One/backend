package com.team901.CapstoneDesign.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

import java.util.Date;


@Getter
@Setter
@Entity
public class Memo {
    @Id
    @GeneratedValue
    private Long id;

    private String rawText;
    private Date createdAt;

    @OneToMany(mappedBy = "memo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemoItem> memoItems = new ArrayList<>();

    @OneToMany(mappedBy = "memo", cascade = CascadeType.ALL)
    private List<MarketCart> marketCarts = new ArrayList<>();
}
