package com.team901.CapstoneDesign.GPT.dto;

import lombok.Getter;

@Getter
public class Product {
    private int num;
    private String title;
    //private String mall;
    private String price;

    public Product(int num, String title, String price) {
        this.num = num;
        this.title = title;
        //this.mall = mall;
        this.price = price;
    }


    @Override
    public String toString() {
        return "Product{" +
                "num= '" + num + '\'' +
                "title='" + title + '\'' +
                ", price='" + price + '\'' +
                '}';
    }

}