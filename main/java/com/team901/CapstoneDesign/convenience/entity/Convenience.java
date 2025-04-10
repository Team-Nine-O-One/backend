package com.team901.CapstoneDesign.convenience.entity;

import jakarta.persistence.*;
import com.team901.CapstoneDesign.mart.entity.Mart;

@Entity
@Table(name = "convenience")
public class Convenience {
    @Id
    private Integer convenienceId;

    private String name;

    private Float distance;

    @ManyToOne
    @JoinColumn(name = "mart_id")
    private Mart mart;
}
