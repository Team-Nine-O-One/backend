package com.team901.CapstoneDesign.carts.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartDetailGroupedResponseDto {
    private OnlineMartDto onlineMart;
    private OfflineMartGroupDto offlineMarts;
    private String status;

}
