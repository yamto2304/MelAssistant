package com.example.chatapplication.utils;

import android.util.Log;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;

public class SearchInfor {

    String result = "";
    public String getResult (String url, String qa_word, String summary, String sentence, String predict){
        if(url.equals("null")){
            if (crawlGoogle(sentence) != ""){
                return crawlGoogle(sentence);
            }else {
                return "GG";
            }
        }
        else{
            HashMap<String, String> dictionary = new HashMap<>();

            try {
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        //.userAgent("Chrome")
                        .timeout(0)
                        .get();


                System.out.println("Title : " + doc.title());
                // Truy vấn các phần tử a mà href chứa /document/
                Elements table = doc.getElementsByClass("infobox");
                Iterator<Element> iterator = table.iterator();
                while (iterator.hasNext()) {
                    Element table_element = iterator.next();
                    table_element.html(table_element.html().replaceAll("&nbsp;", " ").replaceAll("\\xa0", " ").replaceAll("\\ufeff", "").replaceAll("•", ""));
                    table_element.select(":containsOwn(&00000000000000)").remove();
                    table_element.select(":containsOwn(&-1-1-1-1-1-1-1-1-1-1-1-1-1-1-1-1)").remove();
                    for (int i = 1; i < 10; i++) {
                        String str = "[" + i + "]";
                        table_element.html(table_element.html().replace(str, " "));

                    }
                    Elements tr_elements = table_element.getElementsByTag("tr");
                    Iterator<Element> iter_tr = tr_elements.iterator();

                    while (iter_tr.hasNext()) {
                        Element tag_tr = iter_tr.next();
                        Elements list_th = tag_tr.getElementsByTag("th");
                        Elements list_td = tag_tr.getElementsByTag("td");
                        if ((list_th.size() > 0) && (list_td.size() > 0)) {
                            dictionary.put(list_th.text().toUpperCase().trim(), list_td.get(0).text());
                        } else if ((list_th.size() == 0) && (list_td.size() == 3)) {
                            dictionary.put(list_td.get(1).text().toUpperCase().trim(), list_td.get(2).text());
                        } else if ((list_th.size() == 0) && (list_td.size() == 2)) {
                            dictionary.put(list_td.get(0).text().toUpperCase().trim(), list_td.get(1).text());
                        } else if ((list_th.size() == 0) && (list_td.size() == 1) && (!list_td.get(0).text().equals("")) && (list_td.get(0).parent().previousElementSibling() != null)) {

                            if (list_td.get(0).getElementsByTag("b").text().equals("")) {

                                dictionary.put(list_td.get(0).parent().previousElementSibling().text().toUpperCase().trim(), list_td.get(0).parent().text());
                            } else {
                                if (list_td.get(0).getElementsByTag("b").get(0).nextElementSibling() == null) {
                                    if (list_td.get(0).getElementsByTag("b").get(0).parent().nextElementSibling() != null) {

                                        dictionary.put(list_td.get(0).getElementsByTag("b").text().toUpperCase().trim(), list_td.get(0).getElementsByTag("b").get(0).parent().parent().text());
                                    }

                                } else {
                                    if (list_td.get(0).getElementsByTag("b").get(0).nextElementSibling().tagName() == "br") {
                                        if(list_td.get(0).getElementsByTag("b").get(0).nextElementSibling().nextElementSibling() != null ){
                                            dictionary.put(list_td.get(0).getElementsByTag("b").text(), list_td.get(0).getElementsByTag("b").get(0).nextElementSibling().nextElementSibling().text());
                                        }                                    }else {
                                        dictionary.put(list_td.get(0).getElementsByTag("b").text().toUpperCase().trim(), list_td.get(0).getElementsByTag("b").get(0).nextElementSibling().text());
                                    }                            }


                            }

                        }
                    }
                }
                if(!predict.equals("location") && (dictionary.get("NGHỀ NGHIỆP") == null)){
                    if(summary.indexOf("là") > 0){
                        dictionary.put("NGHỀ NGHIỆP",summary.substring(summary.indexOf("là")+2,summary.indexOf(".",summary.indexOf("là")+2)));

                    }
                }

                dictionary.remove("");
                if (dictionary.size() > 0){

                }
                for (String name : dictionary.keySet()) {

                    String key = name.toString();
                    String value = dictionary.get(name).toString();
                    System.out.println("KEY : "+key+" --- Value : "+value);

                }
                Log.e("URL: ", url);
                qa_word = qa_word.toUpperCase().trim();
                Log.e( "Qa_word: ", qa_word);
                if (qa_word.contains("SINH") && !sentence.contains("ở") && !sentence.contains("đâu")) {
                    if (dictionary.get("NGÀY SINH") != null) {

                        result = dictionary.get("NGÀY SINH").toString();
                    }else {
                        qa_word = "SINH";
                    }
                }
                if (sentence.contains("làm gì") || qa_word.contains("NGHỀ")) {
                   qa_word = "NGHỀ NGHIỆP";
                }


                if (qa_word.contains("QUÊ") || qa_word.contains("QUÊ QUÁN") || sentence.contains(" ở ") || sentence.contains("nằm")) {
                    Log.e( "Quê: ", "sSSS" );

                    if (dictionary.get("NƠI SINH") != null) {
                        Log.e( "Quê: ", "nơi sinh" );
                        result = dictionary.get("NƠI SINH").toString();
                    } else if (dictionary.get("SINH") != null) {
                        Log.e( "Quê: ", "sinh" );

                        result = dictionary.get("SINH").toString();

                    }else if (dictionary.get("ĐỊA CHỈ") != null) {
                        result = dictionary.get("ĐỊA CHỈ").toString();


                    } else if (dictionary.get("VỊ TRÍ") != null) {
                        result = dictionary.get("VỊ TRÍ").toString();
                    }
                    else if (dictionary.get("ĐỊA ĐIỂM") != null) {
                        result = dictionary.get("ĐỊA ĐIỂM").toString();
                    }

                } else if (qa_word.contains("DIỆN TÍCH") || qa_word.contains("RỘNG")) {
                    Log.e("getResult: ", "Diện tích");
                    if (getValue(dictionary,"TỔNG CỘNG").contains("km")) {
                        qa_word = "TỔNG CỘNG";
                    } else if (dictionary.get("DIỆN TÍCH") != null) {
                        result = dictionary.get("DIỆN TÍCH").toString();
                    } else {
                        result = "";
                    }


                }  else if (qa_word.contains("TÊN KHAI SINH")) {
                    qa_word = "TÊN ĐẦY ĐỦ";
                }

                else if (sentence.toUpperCase().contains("MẬT ĐỘ")) {
                    if (!getValue(dictionary,"MẬT ĐỘ").equals("")) {
                        qa_word = "MẬT ĐỘ";
                    }else if(!getValue(dictionary,"MẬT SỐ").equals("")){
                        qa_word = "MẬT SỐ";
                    }
                }

                else if (qa_word.contains("DÂN SỐ") || sentence.contains("bao nhiêu người") || qa_word.contains("SỐ NGƯỜI") ) {
                    Log.e("Dân số: ", "xxx");
                    if (getValue(dictionary,"TỔNG CỘNG").contains("người")) {
                        Log.e("Dân số: ", "tong cong");
                        qa_word = "TỔNG CỘNG";
                    }
                    else  if (!getValue(dictionary,"ƯỚC TÍNH").equals("")) {
                        qa_word = "ƯỚC TÍNH";
                    }  else  if (!getValue(dictionary,"ƯỚC LƯỢNG").equals("")) {
                        qa_word = "ƯỚC LƯỢNG";
                    }else if (!getValue(dictionary,"DÂN SỐ").equals("")) {
                        qa_word = "DÂN SỐ";
                    }
                    else {
                        qa_word = "";
                    }

                }  else if (qa_word.contains("ĐIỀU HÀNH")) {
                    qa_word = "CEO";

                } else if (qa_word.contains("CÂU LẠC BỘ") | qa_word.contains("ĐỘI BÓNG")) {
                    qa_word = "CLB";

                }else if (qa_word.contains("NGHỀ")) {
                    qa_word = "NGHỀ NGHIỆP";

                }
                else if (qa_word.contains("QUỐC CA")) {
                    qa_word = "QUỐC CA:";

                } else if (qa_word.contains("VỢ")) {
                    qa_word = "VỢ";

                }
                else if (qa_word.contains("CON")) {
                    qa_word = "CON";

                }
                else if (qa_word.contains("CHỒNG")) {
                    qa_word = "CHỒNG";

                }
                else if(qa_word.contains("GDP") || qa_word.contains("TỔNG SẢN PHẨM NỘI ĐỊA")){
                    if (!getValue(dictionary,"TỔNG SỐ").equals("")) {
                        result = getValue(dictionary,"TỔNG SỐ");
                    }else {
                        result = getValue(dictionary,"Tổng số");
                    }
                }

                else if (qa_word.contains("TUỔI")) {
                    if (dictionary.get("MẤT") != null) {
                        String val = dictionary.get("MẤT").toString();
                        if (val.contains("tuổi")){
                            result = val.substring(val.lastIndexOf("(") + 1, val.lastIndexOf(")"));

                        }

                    } else if (dictionary.get("NGÀY SINH") != null) {
                        String val = dictionary.get("NGÀY SINH").toString();
                        if (val.contains("tuổi")){
                            result = val.substring(val.indexOf("(") + 1, val.indexOf(")"));

                        }

                    } else if (dictionary.get("THÀNH LẬP") != null) {
                        String val = dictionary.get("THÀNH LẬP");
                        if (val.contains("tuổi")){
                            result = val.substring(val.indexOf(";") + 1, val.length());

                        }

                    }else if (dictionary.get("SINH") != null) {
                        String val = dictionary.get("SINH").toString();
                        if (val.contains("tuổi")){
                            result = val.substring(val.indexOf("(") + 1, val.indexOf(")"));

                        }

                    }
                }
                if (result == ""){
                    for (String name : dictionary.keySet()) {

                        String key = name.toString();
                        String value = dictionary.get(name).toString();

                        if (qa_word.equals(key.trim())) {

                            result =value;
                        }
                    }
                    if (result == ""){
                        for (String name : dictionary.keySet()) {

                            String key = name.toString();
                            String value = dictionary.get(name).toString();
                            if(!qa_word.equals("")){
                                if ((qa_word.contains(key)) || (key.contains(qa_word))) {
                                    Log.e( "getResult: ", "AAA1");
                                    result = value;
                                }
                            }

                        }
                    }

                }

                if ( (sentence.contains("thông tin")) || (sentence.contains("tiểu sử")) || (sentence.contains("thân thế")) || (sentence.contains("xuất thân")) || (qa_word.equals(""))) {
                    Log.e( "Summary: ", "ss");
                    result = summary;
                }

                if (result == "") {
                    if (crawlGoogle(sentence) != ""){
                        result = crawlGoogle(sentence);
                    }else {
                        result = "GG";
                    }

                }
                if (result == ""){
                    result ="không tìm thấy thông tin!";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

    }


    public static String getValue(HashMap<String, String> dictionary, String qa_word){
        String resul = "";
        for (String name : dictionary.keySet()) {

            String key = name.toString();
            String value = dictionary.get(name).toString();

            if (qa_word.equals(key)) {
                Log.e( "getValue: ","Hello" );
                resul =value;

            } else if ((qa_word.contains(key)) || (key.contains(qa_word))) {
                Log.e( "getValue: ","Hello2" );
                resul = value;

            }
        }
        return resul;

    }




    public String crawlGoogle(String sentences){
        String[] className = {"Z0LcW","kpd-ans kno-fb-ctx KBXm4e","ILfuVd","IAznY","wfg6Pb","dDoNo vk_bk","vk_sh vk_bk"};
        String result = "";
        Document  document = null;
        Log.e( "crawlGoogle: ","s" );
        try {
            document = (Document) Jsoup.connect("https://www.google.com/search?q="+sentences).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36").get();
            for (String name : className) {

                if(!document.getElementsByClass(name).toString().equals("")){

                    Elements elements = document.getElementsByClass(name);

                    Iterator<Element> iterator = elements.iterator();
                    while(iterator.hasNext())  {
                        Element table_element = iterator.next();

                        result = result+ table_element.text()+". ";
                        Log.e( "crawlGoogle: ",result );
                    }
                    break;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return result.trim();

    }



}

