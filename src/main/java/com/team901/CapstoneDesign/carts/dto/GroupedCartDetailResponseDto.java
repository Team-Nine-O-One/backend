package com.team901.CapstoneDesign.carts.dto;


import com.team901.CapstoneDesign.mart.dto.MartDetailDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GroupedCartDetailResponseDto {
    private OnlineMartDto onlineMart;
    //private List<MartDetailDto> offlineMarts;
    RouteGroupedOfflineDto optimal;
    RouteGroupedOfflineDto distance;
    RouteGroupedOfflineDto price;

    private String status;

    private List<String> optimalMartRoute;
    private List<String> distancePriorityMartRoute;
    private List<String> pricePriorityMartRoute;

}

