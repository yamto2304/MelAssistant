package com.example.chatapplication.utils;

import com.example.chatapplication.models.NewsModel;
import com.example.chatapplication.models.ProductModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Product {
    public List<ProductModel> getProduct(String word){
        List<ProductModel> list=new ArrayList<>();
        Document document = null;
        try {
            document = (Document) Jsoup.connect("https://websosanh.vn/s/"+word+".htm").get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements elements = document.getElementsByClass("item");

        for (Element aElement : elements) {

            String h3 = aElement.select("h3").text();
            String price = aElement.getElementsByClass("price").get(0).text();
            String site = aElement.getElementsByClass("img-merchant-wrap").get(0).select("a").attr("title");
            ProductModel product = new ProductModel(h3,price,site);
            list.add(product);
        }
        return list;
    }
}
