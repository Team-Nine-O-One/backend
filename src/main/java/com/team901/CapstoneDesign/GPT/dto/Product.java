package com.team901.CapstoneDesign.GPT.dto;


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

    public int getNum() {
        return num;
    }

    public String getTitle() {
        return title;
    }

//    public String getMall() {
//        return mall;
//    }

    public String getPrice() {
        return price;
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