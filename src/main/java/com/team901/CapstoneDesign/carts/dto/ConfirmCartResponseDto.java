package com.team901.CapstoneDesign.carts.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ConfirmCartResponseDto {
    private Long cartId;
    private String status;
    private LocalDateTime confirmedAt;
}

