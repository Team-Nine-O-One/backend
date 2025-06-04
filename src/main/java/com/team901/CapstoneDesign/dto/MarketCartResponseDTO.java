package com.team901.CapstoneDesign.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MarketCartResponseDTO {
    public String marketName;
    public int totalPrice;
    public Double distanceFromUser;
    public List<CartItemDTO> items;

    @Getter
    @Setter
    public static class CartItemDTO {
        public String memoItemName;
        public String productName;
        public int price;
    }
}