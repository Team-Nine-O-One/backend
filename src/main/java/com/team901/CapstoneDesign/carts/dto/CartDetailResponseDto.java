package com.team901.CapstoneDesign.carts.dto;

import com.team901.CapstoneDesign.mart.dto.MartDetailDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CartDetailResponseDto {
    private int onlineCount;
    private int offlineCount;
    private List<MartDetailDto> marts;
}

