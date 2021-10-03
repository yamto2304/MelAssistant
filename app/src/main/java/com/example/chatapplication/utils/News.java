package com.example.chatapplication.utils;

import android.os.AsyncTask;

import com.example.chatapplication.models.NewsModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class News {

    public List<NewsModel> getNews(String word){
        List<NewsModel> list=new ArrayList<>();
        if(!word.equals("")){
            Document document = null;
            try {
                document = (Document) Jsoup.connect("https://baomoi.com/tim-kiem/"+word+".epi").get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Elements elements = document.getElementsByClass("story__heading");
            for (Element aElement : elements) {
                String href = aElement.select("a").attr("href");
                String title = aElement.text();
                NewsModel newsModel = new NewsModel(title,href);
                list.add(newsModel);

            }
            return list;
        }else {
            Document document = null;
            try {
                document = (Document) Jsoup.connect("https://baomoi.com/").get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Elements elements = document.getElementsByClass("story__heading");
            for (Element aElement : elements) {
                String href = aElement.select("a").attr("href");
                String title = aElement.text();
                NewsModel newsModel = new NewsModel(title,href);
                list.add(newsModel);
            }
            return list;
        }
    }

}
