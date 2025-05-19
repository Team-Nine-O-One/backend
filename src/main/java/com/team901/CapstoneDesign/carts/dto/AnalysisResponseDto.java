package com.team901.CapstoneDesign.carts.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AnalysisResponseDto {
    private Long cartId;
    private String userId;
    private String memo;
    private String status;
    private LocalDateTime createdAt;
}

