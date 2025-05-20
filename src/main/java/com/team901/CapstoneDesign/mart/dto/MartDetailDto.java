package com.team901.CapstoneDesign.mart.dto;

import com.team901.CapstoneDesign.product.dto.ProductDetailDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MartDetailDto {
    private String martName;
    private Double distance;
    private String estimatedTime;
    private Integer totalItems;
    private Double totalPrice;
    private List<ProductDetailDto> products;
}