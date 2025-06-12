package com.team901.CapstoneDesign.carts.dto;

import com.team901.CapstoneDesign.product.dto.ProductDetailDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OnlineMartDto {
    private int totalItems;
    private double totalPrice;
    private List<ProductDetailDto> products;
}

