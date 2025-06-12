package com.team901.CapstoneDesign.carts.dto;

import com.team901.CapstoneDesign.mart.dto.MartDetailDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RouteGroupedOfflineDto {
    String routeName;
    List<MartDetailDto> marts;
}
