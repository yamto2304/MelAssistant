package com.example.chatapplication.models;

public class ProductModel {
    private String name;
    private String price;
    private String site;


    public ProductModel(String name,String price,String site){
        this.name=name;
        this.price=price;
        this.site=site;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getSite() {
        return site;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setSite(String site) {
        this.site = site;
    }
}
