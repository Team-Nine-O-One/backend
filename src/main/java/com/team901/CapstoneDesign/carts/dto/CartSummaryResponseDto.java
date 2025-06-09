package com.team901.CapstoneDesign.carts.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class CartSummaryResponseDto {
    private Long cartId;
    private String title;
    private List<CartMartSummaryDto> marts;
    private int totalItems;
    private Double totalPrice;
    private String status;
    private LocalDateTime updatedAt;
    private boolean isCompleted;
}

