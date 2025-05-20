package com.team901.CapstoneDesign.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductDetailDto {
    private String product;
    private Double price;
    private Double pricePer100g;
}
