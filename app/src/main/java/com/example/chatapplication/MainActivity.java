package com.example.chatapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
//import android.support.annotation.Nullable;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.models.ChatAppMsgDTO;
import com.example.chatapplication.models.NewsModel;
import com.example.chatapplication.models.ProductModel;
import com.example.chatapplication.utils.News;
import com.example.chatapplication.utils.Product;
import com.example.chatapplication.utils.SearchInfor;
import com.example.chatapplication.utils.time.Time;


import net.time4j.android.ApplicationStarter;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private List<ChatAppMsgDTO> msgDtoList;
    // Set RecyclerView layout manager.
    private RecyclerView msgRecyclerView;
    private ChatAppMsgAdapter chatAppMsgAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ApplicationStarter.initialize(this, true);
        setContentView(R.layout.activity_main);

        setTitle("Virual Assistant");

        // Get RecyclerView object.
        msgRecyclerView = (RecyclerView) findViewById(R.id.chat_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        msgRecyclerView.setLayoutManager(linearLayoutManager);

        // Create the initial data list.
        msgDtoList = new ArrayList<ChatAppMsgDTO>();

        ChatAppMsgDTO msgDto = new ChatAppMsgDTO(ChatAppMsgDTO.MSG_TYPE_RECEIVED, "Hello ! Can I help U ?");
        msgDtoList.add(msgDto);

        // Create the data adapter with above data list.
        chatAppMsgAdapter = new ChatAppMsgAdapter(msgDtoList);

        // Set data adapter to RecyclerView.
        msgRecyclerView.setAdapter(chatAppMsgAdapter);


    }

    public void getSpeechInput(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String msgContent = result.get(0);
                    if (!TextUtils.isEmpty(msgContent)) {
                        receivedText(ChatAppMsgDTO.MSG_TYPE_SENT,msgContent);


                    }
                    new Geturl().execute("https://sv-api1.herokuapp.com/predict?str=" + result.get(0));

                }
                break;
        }
    }

    class Geturl extends AsyncTask<String, String, String> {

        OkHttpClient client = new OkHttpClient();
        ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        protected void onPreExecute() {
            dialog.setMessage("Loading...");
            //show dialog
            dialog.show();
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... strings) {

            Request.Builder builder = new Request.Builder();
            builder.url(strings[0]);
            Request request = builder.build();

            try {
                Response response = client.newCall(request).execute();

                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject obj = new JSONObject(s);

                String predict = obj.get("predict").toString();
                String str = obj.get("str").toString();
                String url_wiki = null,qa_word = null,summary = null,word_ner = null;


                switch (predict) {
                    case "entity":
                        JSONArray list = new JSONArray(obj.get("list").toString());
                        url_wiki = obj.get("url_wiki").toString();

                        qa_word = obj.get("qa_word").toString();
                        summary = obj.get("summary").toString();
                        String predict2 = obj.get("predict_2").toString();
                        new GetSearch(dialog).execute(url_wiki, qa_word, summary, str,predict2, String.valueOf(list));
                        break;
                    case "time":
                        Time time = new Time(MainActivity.this);
                        boolean loc = Boolean.parseBoolean(obj.get("loc").toString());
                        String list_time = obj.get("list").toString();
                        String getTime = time.getDay(str,loc);
                        if(getTime.equals("GG")){
                            new GetSearchGoogle(dialog).execute("https://www.googleapis.com/customsearch/v1?key=AIzaSyCXpYP7A-wcw90T9ck0nVgc6bDRh03DgG0&cx=009578444420158495262%3Ae6hwskq1wuq&q="+str+"&siteSearch=youtube.com&siteSearchFilter=e",str,"other",list_time);
                        }
                        else if(getTime.equals("GG2")){
                            new GetSearchGoogle(dialog).execute("https://www.googleapis.com/customsearch/v1?key=AIzaSyCXpYP7A-wcw90T9ck0nVgc6bDRh03DgG0&cx=009578444420158495262%3Ae6hwskq1wuq&q="+str+"&siteSearch=youtube.com&siteSearchFilter=e",str,"time_2",list_time);
                        }else {
                            dialog.dismiss();
                            receivedText(ChatAppMsgDTO.MSG_TYPE_RECEIVED,getTime);

                        }
                        break;
                    case "location":

                        word_ner = obj.get("word_ner").toString();
                        dialog.dismiss();
                        openMap(word_ner);
                        break;
                    case "news":
                        word_ner = obj.get("word_ner").toString();
                        new GetNews(dialog).execute(word_ner);
                        break;
                    case "product":
                        word_ner = obj.get("word_ner").toString();
                        new GetProducts(dialog).execute(word_ner);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    class GetSearch extends AsyncTask<String, String, List<String>> {
        ProgressDialog dialog ;
        GetSearch(ProgressDialog pdialog){
            dialog = pdialog;
        }

        @Override
        protected List<String> doInBackground(String... strings) {

            SearchInfor searchInfor = new SearchInfor();
            List<String> list = new ArrayList<>();
            list.add(searchInfor.getResult(strings[0], strings[1], strings[2], strings[3],strings[4]));
            list.add(strings[3]);
            list.add(strings[4]);
            list.add(strings[5]);
            return list;
        }


        @Override
        protected void onPostExecute(List<String> list) {
            if(list.get(0).equals("GG")){
                if(list.get(2).equals("other")){
                    dialog.dismiss();
                    receivedText(ChatAppMsgDTO.MSG_TYPE_RECEIVED,"Không tìm thấy thông tin!");
                }else {
                    new GetSearchGoogle(dialog).execute("https://www.googleapis.com/customsearch/v1?key=AIzaSyCXpYP7A-wcw90T9ck0nVgc6bDRh03DgG0&cx=009578444420158495262%3Ae6hwskq1wuq&q="+list.get(1)+"&siteSearch=youtube.com&siteSearchFilter=e",list.get(1),list.get(2),list.get(3));

                }

            }
            else{
                dialog.dismiss();
                receivedText(ChatAppMsgDTO.MSG_TYPE_RECEIVED,list.get(0));
            }


        }

    }


    class GetProducts extends AsyncTask<String, String, List<ProductModel>> {
        ProgressDialog dialog;
        public GetProducts(ProgressDialog pdialog){
            dialog = pdialog;
        }
        @Override
        protected List<ProductModel> doInBackground(String... strings) {
            Product product = new Product();
            return product.getProduct(strings[0]);

        }


        @Override
        protected void onPostExecute(List<ProductModel> s) {

            for(ProductModel product : s){
                String str  = product.getName()+"\n "+"Price : "+product.getPrice()+"\n "+"Nơi bán : "+product.getSite();
                dialog.dismiss();
                receivedText(ChatAppMsgDTO.MSG_TYPE_RECEIVED,str);
            }


        }

    }


    class GetNews extends AsyncTask<String, String, List<NewsModel>> {
        ProgressDialog dialog;
        public GetNews(ProgressDialog pdialog){
            dialog = pdialog;
        }
        @Override
        protected List<NewsModel> doInBackground(String... strings) {
            News news = new News();
            return news.getNews(strings[0]);

        }


        @Override
        protected void onPostExecute(List<NewsModel> s) {
            dialog.dismiss();
            for(NewsModel n : s){
                String str  = "<a href=\"https://baomoi.com"+n.getUrl()+"\">"+n.getTitle()+"</a>";
                Log.e("News: ",str );

                receivedText(ChatAppMsgDTO.MSG_TYPE_RECEIVED_NEWS,str);
            }


        }

    }

    public void openMap(String query){
        String uri = "https://www.google.com/maps/search/?api=1&query="+query;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
        receivedText(ChatAppMsgDTO.MSG_TYPE_RECEIVED,"Đã bật Map");
    }
    public void receivedText(String type,String text){
        ChatAppMsgDTO msgResult = new ChatAppMsgDTO(type, text);
        msgDtoList.add(msgResult);
        int newMsgPosition = msgDtoList.size() - 1;

        // Notify recycler view insert one new data.
        chatAppMsgAdapter.notifyItemInserted(newMsgPosition);

        // Scroll RecyclerView to the last message.
        msgRecyclerView.scrollToPosition(newMsgPosition);
    }


    class GetSearchGoogle extends AsyncTask<String, Void, List<String>>{
        ProgressDialog dialog ;
        GetSearchGoogle(ProgressDialog pdialog){
            dialog = pdialog;
        }
        OkHttpClient client = new OkHttpClient();
        @Override
        protected List<String> doInBackground(String... strings) {

            Request.Builder builder = new Request.Builder();
            builder.url(strings[0]);
            Request request = builder.build();

            try {
                Response response = client.newCall(request).execute();
                List<String> list = Arrays.asList(response.body().string(),strings[1],strings[2],strings[3]);
                return list;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(List<String> result) {
            try {
                JSONObject obj = new JSONObject(result.get(0));
                JSONArray items = obj.getJSONArray("items");
                String predict = result.get(2);

                String l  =result.get(3).replace("[","").replace("]","").replace("\"","");
                List<String> list = Arrays.asList(l.split("\\s*,\\s*"));

                String sentences = "";
                String t ="";
                for (int i = 0;i <items.length();i++){
                    JSONObject item_obj = (JSONObject) items.get(i);

                        String str = item_obj.get("snippet").toString();
                        String title = item_obj.get("title").toString().replace("...","");
                        sentences = sentences + title + " ";
                        String snippet = str.replace("\n","");

                        String[] strs = snippet.split("\\.\\.\\. ");
                        for(String s : strs){
                            s = s.replace("...","");

                            int x = 0 ;
                            for(String word : list){
                                if(s.toUpperCase().contains(word.toUpperCase())){
//
//                                    sentences = sentences + s + " ";
//                                    break;
                                    x++;
                                }
                            }

                                if((100*(x+1)/(list.size()+1)) >= 50){
                                sentences = sentences + s + " ";
                                    Log.e("Sentences: ", sentences);

                            }


                        }


                }
//                for(String s : list){
//                    sentences = sentences.replaceAll("(?i)"+s.replace("_"," "),"");
//                }
                Log.e( "Sentences: ", sentences);
//                new getInfor().execute("https://sv-api1.herokuapp.com/ner?str="+sentences+"&predict="+predict,l);

                new getInfor(dialog).execute("https://sv-api1.herokuapp.com/foo",sentences,predict,l);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }



    class getInfor extends AsyncTask<String, String, List<String>> {
        ProgressDialog dialog ;
        getInfor(ProgressDialog pdialog){
            dialog = pdialog;
        }
        OkHttpClient client = new OkHttpClient();

        @Override
        protected List<String> doInBackground(String... strings) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("str",strings[1]);
                jsonObject.put("predict",strings[2]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, jsonObject.toString());
            Request.Builder builder = new Request.Builder();

            builder.url(strings[0]).post(body);
            Request request = builder.build();

            try {
                Response response = client.newCall(request).execute();
                List<String> list = Arrays.asList(response.body().string(),strings[3],strings[2]);
                return list;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(List<String> l) {
            Log.e("Post: ", l.get(0));
            try {
                JSONObject obj = new JSONObject(l.get(0));
                List<String> list = Arrays.asList(l.get(1).split("\\s*,\\s*"));
                String predict  = l.get(2);


                String str = obj.get("str").toString();
                Log.e( "List: ", list.toString());
                for(String s : list){
                    str = str.replaceAll("(?i)"+s.replace(" ","_"),"");
                }
                Log.e( "respone: ", str);
                if(str != ""){
                    Map<String, Integer> wordMap = countWords(str);
                    Iterator<String> iter = wordMap.keySet().iterator();
                    while (iter.hasNext()){

                        String key = iter.next();

                        if (key.equals(".") || key.equals("(") || key.equals(")") || key.equals(",") || key.equals("\"") || key.equals(":") || key.equals("“") | key.equals("|") | key.equals("”") | key.equals("-") || key.equals("'")||key.equals(",") || key.equals("_") ||key.equals("__")){

                            iter.remove();
                        }

                    }


                    List<String> listKey = new LinkedList<String>(Arrays.asList(wordMap.keySet().toString().split("\\s*,\\s*")));
                    int n = listKey.size();

                    for (String key : wordMap.keySet()) {
                        System.out.println("1Key : "+key +" -- 1Value : "+wordMap.get(key));
                    }
                    for (int i = 0 ; i < n;i++){
                        for ( int j = 0 ;j<i;j++){
                            if ( (wordMap.get(listKey.get(i)) !=null) & (wordMap.get(listKey.get(j)) !=null)){
                                if(isNESimilar(listKey.get(i), listKey.get(j),predict)){
                                    Log.e("Key I: ", listKey.get(i)+"--Value I :"+wordMap.get(listKey.get(i)) +"--Key J :"+listKey.get(j)+"--- Value J: "+wordMap.get(listKey.get(j)));

                                    String key1= "",key2="";
                                    if(wordMap.get(listKey.get(i)) == wordMap.get(listKey.get(j))){
                                        key1 = (listKey.get(i).length() >= listKey.get(j).length()) ? listKey.get(i):listKey.get(j);
                                        key2 = (listKey.get(i).length() < listKey.get(j).length()) ? listKey.get(i):listKey.get(j);
                                    }else{
                                        key1 = (wordMap.get(listKey.get(i)) > wordMap.get(listKey.get(j))) ? listKey.get(i):listKey.get(j);
                                        key2 = (wordMap.get(listKey.get(i)) < wordMap.get(listKey.get(j))) ? listKey.get(i):listKey.get(j);
                                    }

                                    Log.e("Value: ", key1+"--"+key2);
                                    listKey.remove(key2);

                                    Log.e( "Remove: ", listKey.toString());
                                    Iterator<String> iter1 = wordMap.keySet().iterator();
                                    while (iter1.hasNext()){
                                        String key = iter1.next();
                                        Log.e("Key Inter: ", key);
                                        if(key.equals(key2)){
                                            Log.e("Key2: ", key2);
                                            int value = wordMap.get(key1)+wordMap.get(key2);
                                            Log.e( "AAAA: ", String.valueOf(value));
                                            wordMap.put(key1, value);
                                            iter1.remove();
                                            break;
                                        }

                                    }
                                    n--;
                                    i--;
                                }
                            }

                        }

                    }

                    for (String key : wordMap.keySet()) {  // Itrate through hashmap
                        System.out.println("Key : "+key +" -- Value : "+wordMap.get(key));
                    }
                    int maxValueInMap=(Collections.max(wordMap.values()));  // This will return max value in the Hashmap
                    String res = "";
                    dialog.dismiss();
                    if(maxValueInMap > 2){
                    for (Map.Entry<String, Integer> entry : wordMap.entrySet()) {  // Itrate through hashmap
                        if (entry.getValue()==maxValueInMap) {

                                Log.e( "Max: ",entry.getKey() );
                                receivedText(ChatAppMsgDTO.MSG_TYPE_RECEIVED,entry.getKey().replace("_"," "));
                                res = res +entry.getKey() +"_";
                            }


                        }
                    }else{
                        receivedText(ChatAppMsgDTO.MSG_TYPE_RECEIVED,"Không tìm thấy !");
                    }


                }else {
                    receivedText(ChatAppMsgDTO.MSG_TYPE_RECEIVED,"Không tìm thấy !");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }



    public static Map<String, Integer> countWords(String input) {
        // khởi tạo wordMap
        Map<String, Integer> wordMap = new TreeMap<String, Integer>();
        if (input == null) {
            return wordMap;
        }
        int size = input.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (input.charAt(i) != SPACE && input.charAt(i) != TAB
                    && input.charAt(i) != BREAK_LINE) {
                // build một từ
                sb.append(input.charAt(i));
            } else {
                // thêm từ vào wordMap
                addWord(wordMap, sb);
                sb = new StringBuilder();
            }
        }
        // thêm từ cuối cùng tìm được vào wordMap
        addWord(wordMap, sb);
        return wordMap;
    }

    public static void addWord(Map<String, Integer> wordMap, StringBuilder sb) {
        String word = sb.toString();
        if (word.length() == 0) {
            return;
        }
        if (wordMap.containsKey(word)) {
            int count = wordMap.get(word) + 1;
            wordMap.put(word, count);
        } else {
            wordMap.put(word, 1);
        }
    }


    public static final char SPACE = ' ';
    public static final char TAB = '\t';
    public static final char BREAK_LINE = '\n';

    public boolean isNESimilar(String ne1,String ne2,String predict){
        String word1 = ne1.toLowerCase();
        String word2 = ne2.toLowerCase();
//        if(predict.equals("number")){
//            if ( word1.contains(word2) || word2.contains(word1) ){
//                return true;
//            }
//        }
         if (word1.toUpperCase().contains(word2.toUpperCase()) || word2.toUpperCase().equals(word1.toUpperCase()) ){
            return true;
        }
        else {
            String[] ne1Words = word1.split("");
            String[] ne2Words = word2.split("");
            int maxNWords = (ne1Words.length >= ne2Words.length) ? ne1Words.length : ne2Words.length;
            int countSame = countSame(ne1Words,ne2Words);
            if(100*(countSame/maxNWords) > 40){
                return true;
            }
        }
        return false;
    }

    public int countSame(String[] arr1,String[] arr2){
        int count = 0;
        for (int i = 0 ; i <arr1.length;i++){
            for(int j = 0;j<arr2.length;j++){
                if(arr1[i].equals(arr2[j])){
                    count++;
                    arr2[j]="";
                    break;
                }
            }
        }
        return count;
    }
}
