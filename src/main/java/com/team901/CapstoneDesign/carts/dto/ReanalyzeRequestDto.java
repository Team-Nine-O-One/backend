package com.team901.CapstoneDesign.carts.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReanalyzeRequestDto {
    private double priceWeight;
    private double distanceWeight;

}