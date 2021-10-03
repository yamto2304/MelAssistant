package com.example.chatapplication.utils.time;


import android.app.Activity;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


import com.example.chatapplication.R;


import net.time4j.PlainDate;
import net.time4j.calendar.ChineseCalendar;
import net.time4j.calendar.VietnameseCalendar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Calendar;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;



import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Time extends Activity {
    private Context context;
    public Time(Context context){
        this.context =context;
    }
    public String getDay(String sentence,boolean loc) {

        String time = "";
        try {
                if (sentence.toUpperCase().contains("HIỆN TẠI") || sentence.toUpperCase().contains("BÂY GIỜ") || sentence.toUpperCase().contains("HÔM NAY")||sentence.toUpperCase().contains("GIỜ")) {
                    Date date = new Date();

                    if (loc){
                        try {
                            time = new getTimeZone().execute(sentence).get();
                            if (time == ""){
                                time = "Tôi không hiểu ý bạn !";
                            }
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    else if(sentence.contains("âm")){
                        PlainDate gregorian = PlainDate.nowInSystemTime();
                        VietnameseCalendar cc = gregorian.transform(VietnameseCalendar.axis().getChronoType());
                        String time_luar = cc.toString();
                        String year = time_luar.substring(time_luar.indexOf("(")+1,time_luar.indexOf("(")+5);
                        time = "ngày "+cc.getDayOfMonth()+" tháng "+cc.getMonth()+" năm "+namAmLich(Integer.valueOf(year))+" ( "+year+" )";
                    }
                    else if (sentence.toUpperCase().contains("NGÀY") || sentence.toUpperCase().contains("THỨ")) {

                        time = getTime(date).get("dayofweek") + " , " + "ngày " + getTime(date).get("day") + " tháng " +  getTime(date).get("month") + " năm " + getTime(date).get("year");
                    }

                    else {

                        time =  getTime(date).get("hour") + ":" +  getTime(date).get("minutes") + " , " + "ngày " + getTime(date).get("day") + " tháng " +  getTime(date).get("month") + " năm " + getTime(date).get("year");
                    }

                }

                else if (getDayRegex(sentence) != null){
                    Log.e( "getREGEX: ", getDayRegex(sentence));
                    if(sentence.contains("âm")){
                        String date = getDayRegex(sentence);
                        int y = Integer.parseInt(date.substring(date.lastIndexOf("/")+1,date.length()));
                        int m = Integer.parseInt(date.substring(date.indexOf("/")+1,date.lastIndexOf("/")));
                        int d = Integer.parseInt(date.substring(0,date.indexOf("/")));
                        try {
                            PlainDate gregorian =  PlainDate.of(y,m,d);
                            VietnameseCalendar cc = gregorian.transform(VietnameseCalendar.axis().getChronoType());
                            String time_luar = cc.toString();
                            String year = time_luar.substring(time_luar.indexOf("(")+1,time_luar.indexOf("(")+5);
                            time = "ngày "+cc.getDayOfMonth()+" tháng "+cc.getMonth()+" năm "+namAmLich(Integer.valueOf(year))+" ( "+year+" )";
                        }catch (Exception e) {
                            time = "Ngày không hợp lệ !";
                        }

                    }
                    else if (sentence.contains("thứ") ){
                        Date date = new SimpleDateFormat("dd/M/yyyy").parse(getDayRegex(sentence));
                        time = getTime(date).get("dayofweek").toString();
                    }else if (sentence.contains("ngày gì") || sentence.contains("lễ") || sentence.contains("sự kiện") || sentence.contains("ngày nào")){
                        HashMap<String,String> dict = getHoliday();
                        Log.e( "getREGEX: ", "A");
                        for (String key : dict.keySet()){
                            Log.e( "getREGEX: ", dict.get(key).toString());
                            if(dict.get(key).toString().contains(getDayRegex(sentence)) || getDayRegex(sentence).contains(dict.get(key).toString())){
                                Log.e( "getREGEX: ", "B");
                                time = key;
                                break;
                            }
                        }
                        if (time == "") {
                            String result = new getTimeZone().execute(sentence).get();
                            if (result != "") {
                                time = result;
                            } else {
                                time = "GG";
                            }
                        }

                    }
                }else {
                    HashMap<String,String> dict = getHoliday();
                    for(String key : dict.keySet()){

                        String value = dict.get(key).toString();
                        if(sentence.toUpperCase().contains(key) || key.contains(sentence.toUpperCase())){

                            time = value;
                        }
                    }
                    if (time == "") {
                        String result = new getTimeZone().execute(sentence).get();
                        if (result != "") {
                            time = result;
                        } else {
                            time = "GG2";
                        }
                    }
                }

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return time;
    }
    public HashMap getTime(Date date) {
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
        int minutes = calendar.get(Calendar.MINUTE);        // gets hour in 12h format
        int day = calendar.get(Calendar.DATE);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        String dayofweek = "";
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case 1:
                dayofweek = "Chủ Nhật";
                break;
            case 2:
                dayofweek = "Thứ Hai";
                break;
            case 3:
                dayofweek = "Thứ Ba";
                break;
            case 4:
                dayofweek = "Thứ Tư";
                break;
            case 5:
                dayofweek = "Thứ Năm";
                break;
            case 6:
                dayofweek = "Thứ Sáu";
                break;
            case 7:
                dayofweek = "Thứ Bảy";
                break;
        }
        HashMap time = new HashMap();
        time.put("hour",hour);
        time.put("minutes",minutes);
        time.put("day",day);
        time.put("month",month+1);
        time.put("year",year);
        time.put("dayofweek",dayofweek);
        return time;
    }
    public HashMap getHoliday (){
        int[] file = {R.raw.am_lich,R.raw.duong_lich};
        HashMap dict = new HashMap();
        for(Integer id : file){

            try {
                InputStream is = context.getResources().openRawResource(id);
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line = br.readLine();
                while ( line != null) {

                    String value = line.substring(0, line.indexOf(":"));

                    if (id == R.raw.am_lich){
                        value = value + " (Âm lịch)";
                    }
                    Log.e("Line: ",value );
                    String key = line.substring(line.indexOf(" ")+1,line.length()).toUpperCase();
                    dict.put(key,value);
                    line = br.readLine();
                }
                br.close();

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return dict;
    }

    public String getDayRegex(String sentence){
        Pattern p = Pattern.compile("([0-9]{1,2}\\stháng\\s[0-9]{1,2}) | ([0-9]{1,2}\\stháng\\s[0-9]{1,2}\\snăm\\s[0-9]{4})");
        Matcher m = p.matcher(sentence);
        String date = "";
        while(m.find()) {
            date = date + m.group() + " ";
        }
        date  = date.trim();
        if(date != "") {
            System.out.println(date.getClass());
            String day = date.substring(0,date.indexOf(" "));
            String month = "";
            String year = "";
            if (date.contains("năm")){
                month = date.substring(date.indexOf("tháng")+ 6,date.indexOf("năm")-1);
                year  = date.substring(date.lastIndexOf(" ")+1,date.length());

            }else {
                month = date.substring(date.lastIndexOf(" ")+1,date.length());
                Date d = new Date();
                Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
                calendar.setTime(d);
                year = String.valueOf(calendar.get(Calendar.YEAR));
            }

            return day+"/"+month+"/"+year;
        }else {
            return null;
        }

    }

    class getTimeZone extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {

            return crawlGoogle(strings[0]);
        }


        @Override
        protected void onPostExecute(String s) {


        }
    }
    public String crawlGoogle(String sentences){
        String[] className = {"Z0LcW","kpd-ans kno-fb-ctx KBXm4e","ILfuVd","IAznY","wfg6Pb","dDoNo vk_bk","vk_sh vk_bk","vk_c vk_gy vk_sh card-section sL6Rbf"};
        String result = "";
        Document document = null;
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

    public String namAmLich(int year){
        String can = "", chi = "";
        switch (year % 10) {
            case 0:
                can = "Canh";
                break;
            case 1:
                can = "Tân";
                break;
            case 2:
                can = "Nhâm";
                break;
            case 3:
                can = "Quý";
                break;
            case 4:
                can = "Giáp";
                break;
            case 5:
                can = "Ất";
                break;
            case 6:
                can = "Bính";
                break;
            case 7:
                can = "Đinh";
                break;
            case 8:
                can = "Mậu";
                break;
            case 9:
                can = "Kỷ";
                break;
        }

        // Xác định Chi
        switch (year % 12) {
            case 0:
                chi = "Thân";
                break;
            case 1:
                chi = "Dậu";
                break;
            case 2:
                chi = "Tuất";
                break;
            case 3:
                chi = "Hợi";
                break;
            case 4:
                chi = "Tý";
                break;
            case 5:
                chi = "Sửu";
                break;
            case 6:
                chi = "Dần";
                break;
            case 7:
                chi = "Mẹo";
                break;
            case 8:
                chi = "Thìn";
                break;
            case 9:
                chi = "Tỵ";
                break;
            case 10:
                chi = "Ngọ";
                break;
            case 11:
                chi = "Mùi";
                break;
        }
        return can+" "+chi;
    }

}
