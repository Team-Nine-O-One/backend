package com.team901.CapstoneDesign.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemoRequestDTO {
    public String userId;
    public String rawText;
    public Double userLat;
    public Double userLng;
}