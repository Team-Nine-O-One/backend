package com.team901.CapstoneDesign.carts.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CartMartSummaryDto {
    private String martName;
    private List<String> productNames; // 물 외 1건, 양파 외 2건
    private Double totalPrice;
}
