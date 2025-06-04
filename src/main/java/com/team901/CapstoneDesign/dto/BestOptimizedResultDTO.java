package com.team901.CapstoneDesign.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BestOptimizedResultDTO {
    public String marketName;
    public Long memoId;
    public double totalPrice;
    public Double distance;
    public List<ItemDTO> items;

    @Getter
    @Setter
    public static class ItemDTO {
        public String memoItemName;
        public String productName;
        public int price;
    }
}