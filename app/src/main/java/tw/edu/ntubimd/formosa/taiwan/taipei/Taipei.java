package tw.edu.ntubimd.formosa.taiwan.taipei;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.StrictMode;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import tw.edu.ntubimd.formosa.MainActivity;

/**
 * Created by PC on 2016/11/2.
 */

public class Taipei {

    /********************************************************
     * 景點方法
     ********************************************************/
    public static List<HashMap<String, Object>> Attraction(final File dir) { // Att()方法傳回值為List格式

        final List<HashMap<String, Object>> DataReturn = new ArrayList<HashMap<String, Object>>();

        Callable<List<HashMap<String, Object>>> callable = new Callable<List<HashMap<String, Object>>>() {
            public List<HashMap<String, Object>> call() throws Exception {
                // TODO code application logic here
                JSONArray respJSON = new JSONArray(); // 宣告一個JSONArray
                JSONArray attJSON = new JSONArray(); // 宣告一個JSONArray存放respJSON處理後的資料
                String url = "http://140.131.114.161/~x4girls/json/Scenic_spot.json"; // 宣告一個String存放網址
                InputStream inputStream = null; // 宣告一個InputStream
                String[] taipeizipcode = {"100", "103", "104", "105", "106", "108", "110", "111", "112", "114", "115", "116"};
                List<HashMap<String, Object>> Data = new ArrayList<HashMap<String, Object>>();

                try {
                    HttpClient httpclient = new DefaultHttpClient(); // 宣告一個HttpClient
                    HttpResponse httpResponse = httpclient.execute(new HttpGet(url)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
                    inputStream = httpResponse.getEntity().getContent(); // 用InputStream存放取回的內容
                    int resCode = httpResponse.getStatusLine().getStatusCode(); // 宣告一個int去取得網頁執行的回應碼
                    BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
                    String responseString = readAll(rd); // 宣告一個字串放置讀取BufferedReader後的結果
                    JSONObject OneJSON = new JSONObject(responseString); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                    StringBuffer attractionStr = new StringBuffer();
                    if (OneJSON.has("Infos")) { //抓出Infos
                        JSONObject TwoJSON = new JSONObject(OneJSON.get("Infos").toString());
                        if (TwoJSON.has("Info")) { //抓出Info
                            respJSON = new JSONArray(TwoJSON.get("Info").toString());
                            for (int i = 0; i < respJSON.length(); i++) { // 用for迴圈判斷處理respJSON的資料
                                JSONObject tmpJSON = new JSONObject(respJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                                attJSON.put(tmpJSON); // 將JSONObject放回JSONArray方便處理資料(JSONArray才可判斷長度)
                            }
                            if (attJSON.length() > 0) { // 如果JSONArray有資料才做
                                for (int i = 0; i < attJSON.length(); i++) { // 用for迴圈判斷處理attJSON的資料
                                    JSONObject tmpJSON = new JSONObject(attJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                                    HashMap<String, Object> hashMap = new HashMap<String, Object>(); // 宣告一個HashMap放置List資料
                                    String zipcode = tmpJSON.get("Zipcode").toString();
                                    for (int t = 0; t < 12; t++) {
                                        if (taipeizipcode[t].equals(zipcode)) {
                                            if (tmpJSON.has("Picture1")) { // 如果JSONArray裡有pictures的資料
                                                String imageUrl = tmpJSON.get("Picture1").toString(); // Picture1內的url把轉成字串放在imageUrl裡
                                                if (imageUrl == "") {
                                                    hashMap.put("Pictures", "no picture");
                                                    System.out.print(imageUrl);
                                                } else {
//                                        imageUrl.replace('\\','\0');
//                                        System.out.print(imageUrl);
//                                        InputStream inputStreamImage = null; // 用InputStream存放取回的內容
//                                        HttpClient httpclientImage = new DefaultHttpClient(); // 宣告一個HttpClient
//                                        HttpResponse httpResponseImage = httpclientImage.execute(new HttpGet(imageUrl)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟imageUrl，並且執行HttpClient
//                                        HttpEntity entity = httpResponseImage.getEntity(); // 宣告一個HttpEntity取得HttpResponse的實體
//                                        BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(entity); // 宣告一個BufferedHttpEntity取得HttpEntity的實體
//                                        inputStreamImage = bufferedHttpEntity.getContent(); // 用InputStream讀取實體內容
//                                        inputStreamImage.close(); // 關閉InputStream
//                                        hashMap.put("Pictures", decodeBitmapFromInputStream(inputStreamImage, 300, 100)); // 將url裡的圖片放進HashMap裡，並使用decodeBitmapFromInputStream調節圖片大小
                                                    hashMap.put("Pictures", "no picture");
                                                }
                                            }
                                            hashMap.put("Name", tmpJSON.getString("Name")); // 將name的資料放進HashMap裡
                                            hashMap.put("Rank", "5");// 將avgrank的資料放進HashMap裡
                                            Data.add(hashMap); // 把HashMap加到List裡

                                            attractionStr.append(tmpJSON.toString() + ", ");
                                        }
                                        File attractionFile = new File(dir, "attractionFile");
                                        writeToFile(attractionFile, "[ " + attractionStr.toString() + "]");
                                    }
                                }
                            }
                        }
                    }
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (UnsupportedOperationException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                DataReturn.addAll(Data);
                return Data;
            }
        };
        try {
            FutureTask<List<HashMap<String, Object>>> future = new FutureTask<List<HashMap<String, Object>>>(callable);
            new Thread(future).start();
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return DataReturn;
    }
    // endregion

    private static File creatFile(MainActivity mainActivity) {
        File dir = mainActivity.getFilesDir();
        return dir;
    }

    private static void writeToFile(File fout, String data) {
        FileOutputStream osw = null;
        try {
            osw = new FileOutputStream(fout);
            osw.write(data.getBytes());
            osw.flush();
        } catch (Exception e) {
            ;
        } finally {
            try {
                osw.close();
            } catch (Exception e) {
                ;
            }
        }
    }

    /********************************************************
     * 景點風景區方法
     ********************************************************/
    // region 景點風景區方法內容
    public static List<HashMap<String, Object>> AttScenic() { // AttScenic()方法傳回值為List格式
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(
                    new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork() // or
                            // .detectAll()
                            // for
                            // all
                            // detectable
                            // problems
                            .penaltyLog().build());
        }
        List<HashMap<String, Object>> Data = new ArrayList<HashMap<String, Object>>(); // 宣告一個List存放ListView要顯示的資料

        // TODO code application logic here
        JSONArray respJSON = new JSONArray(); // 宣告一個JSONArray
        JSONArray attJSON = new JSONArray(); // 宣告一個JSONArray存放respJSON處理後的資料
        String url = "http://140.131.114.161/~x4girls/json/Scenic_spot.json"; // 宣告一個String存放網址
        InputStream inputStream = null; // 宣告一個InputStream
        String[] taipeizipcode = {"100", "103", "104", "105", "106", "108", "110", "111", "112", "114", "115", "116"};
        String[] type = {"01", "02", "08", "11"};

        try {
            HttpClient httpclient = new DefaultHttpClient(); // 宣告一個HttpClient
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
            inputStream = httpResponse.getEntity().getContent(); // 用InputStream存放取回的內容
            int resCode = httpResponse.getStatusLine().getStatusCode(); // 宣告一個int去取得網頁執行的回應碼
            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
            String responseString = readAll(rd); // 宣告一個字串放置讀取BufferedReader後的結果
            JSONObject OneJSON = new JSONObject(responseString); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
            if (OneJSON.has("Infos")) { //抓出Infos
                JSONObject TwoJSON = new JSONObject(OneJSON.get("Infos").toString());
                if (TwoJSON.has("Info")) { //抓出Info
                    respJSON = new JSONArray(TwoJSON.get("Info").toString());
                    for (int i = 0; i < 20; i++) { // 用for迴圈判斷處理respJSON的資料
                        JSONObject tmpJSON = new JSONObject(respJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                        attJSON.put(tmpJSON); // 將JSONObject放回JSONArray方便處理資料(JSONArray才可判斷長度)
                    }
                    if (attJSON.length() > 0) { // 如果JSONArray有資料才做
                        for (int i = 0; i < attJSON.length(); i++) { // 用for迴圈判斷處理attJSON的資料
                            JSONObject tmpJSON = new JSONObject(attJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                            HashMap<String, Object> hashMap = new HashMap<String, Object>(); // 宣告一個HashMap放置List資料
                            String zipcode = tmpJSON.get("Zipcode").toString();
                            String typeclass1 = tmpJSON.get("Class1").toString();
                            String typeclass2 = tmpJSON.getString("Class2").toString();
                            String typeclass3 = tmpJSON.getString("Class3").toString();
                            for (int t = 0; t < 12; t++) {
                                if (taipeizipcode[t].equals(zipcode)) {
                                    for (int s = 0; s < 4; s++) {
                                        if (type[s].equals(typeclass1) || type[s].equals(typeclass2) || type[s].equals(typeclass3)) {
                                            if (tmpJSON.has("Picture1")) { // 如果JSONArray裡有pictures的資料
                                                String imageUrl = tmpJSON.get("Picture1").toString(); // 把url轉成字串放在imageUrl裡
                                                if (imageUrl == "") {
                                                    hashMap.put("Pictures", "no picture");
                                                    System.out.print(imageUrl);
                                                } else {
//                                                InputStream inputStreamImage = null; // 用InputStream存放取回的內容
//                                                HttpClient httpclientImage = new DefaultHttpClient(); // 宣告一個HttpClient
//                                                HttpResponse httpResponseImage = httpclientImage.execute(new HttpGet(imageUrl)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟imageUrl，並且執行HttpClient
//                                                HttpEntity entity = httpResponseImage.getEntity(); // 宣告一個HttpEntity取得HttpResponse的實體
//                                                BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(entity); // 宣告一個BufferedHttpEntity取得HttpEntity的實體
//                                                inputStreamImage = bufferedHttpEntity.getContent(); // 用InputStream讀取實體內容
//                                                inputStreamImage.close(); // 關閉InputStream
//                                                hashMap.put("Pictures", decodeBitmapFromInputStream(inputStreamImage, 300, 100)); // 將url裡的圖片放進HashMap裡，並使用decodeBitmapFromInputStream調節圖片大小
                                                    hashMap.put("Pictures", "no picture");
                                                }
                                            }
                                            hashMap.put("Name", tmpJSON.getString("Name")); // 將name的資料放進HashMap裡
                                            hashMap.put("Rank", "5");// 將avgrank的資料放進HashMap裡
                                            Data.add(hashMap); // 把HashMap加到List裡
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Data;
    }

    /********************************************************
     * 旅館方法
     ********************************************************/
    // region 旅館方法內容
    @SuppressWarnings("deprecation")
    public static List<HashMap<String, Object>> Hotel() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(
                    new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork() // or
                            // .detectAll()
                            // for
                            // all
                            // detectable
                            // problems
                            .penaltyLog().build());
        }

        // TODO code application logic here
        JSONArray attJSON = new JSONArray(); // 宣告一個JSONArray存放respJSON處理後的資料
        String url = "http://140.131.114.161/~x4girls/json/Hotel.json"; // 宣告一個String存放網址
        InputStream inputStream = null; // 宣告一個InputStream
        String output = new String(); // 存放錯誤時的Not Found資訊
        List<HashMap<String, Object>> Data = new ArrayList<HashMap<String, Object>>(); // 宣告一個List存放ListView要顯示的資料
        try {
            HttpClient httpclient = new DefaultHttpClient(); // 宣告一個HttpClient
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
            inputStream = httpResponse.getEntity().getContent(); // 用InputStream存放取回的內容
            int resCode = httpResponse.getStatusLine().getStatusCode(); // 宣告一個int去取得網頁執行的回應碼
            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
            String responseString = readAll(rd); // 宣告一個字串放置讀取BufferedReader後的結果
            String region = "臺北市";

            JSONObject NooJSON = new JSONObject(responseString); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
            if (NooJSON.has("Infos")) {
                JSONObject returnJSON = new JSONObject(NooJSON.get("Infos").toString());
                if (returnJSON.has("Info")) {
                    JSONArray respJSON = new JSONArray(returnJSON.get("Info").toString());
                    for (int i = 0; i < 20; i++) { // 用for迴圈判斷處理respJSON的資料
                        JSONObject tmpJSON = new JSONObject(respJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                        attJSON.put(tmpJSON); // 將JSONObject放回JSONArray方便處理資料(JSONArray才可判斷長度)
                    }
                    if (attJSON.length() > 0) { // 如果JSONArray有資料才做
                        for (int i = 0; i < attJSON.length(); i++) { // 用for迴圈判斷處理attJSON的資料
                            JSONObject tmpJSON = new JSONObject(attJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                            if (tmpJSON.get("Region").toString().equals(region)) {
                                HashMap<String, Object> hashMap = new HashMap<String, Object>(); // 宣告一個HashMap放置List資料
                                hashMap.put("Pictures", "no piture");
                                hashMap.put("Name", tmpJSON.getString("Name")); // 將name的資料放進HashMap裡
                                hashMap.put("Rank", tmpJSON.getString("Grade"));// 將avgrank的資料放進HashMap裡
                                Data.add(hashMap); // 把HashMap加到List裡
                            }
                        }
                    }
                }
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Data;
    }

    /********************************************************
     * 高級酒店方法
     ********************************************************/
// region 旅館方法內容
    @SuppressWarnings("deprecation")
    public static List<HashMap<String, Object>> AdvancedHotel() {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(
                    new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork() // or
                            // .detectAll()
                            // for
                            // all
                            // detectable
                            // problems
                            .penaltyLog().build());
        }

        // TODO code application logic here
        JSONArray attJSON = new JSONArray(); // 宣告一個JSONArray存放respJSON處理後的資料
        String url = "http://140.131.114.161/~x4girls/json/Hotel.json"; // 宣告一個String存放網址
        InputStream inputStream = null; // 宣告一個InputStream
        String output = new String(); // 存放錯誤時的Not Found資訊
        List<HashMap<String, Object>> Data = new ArrayList<HashMap<String, Object>>(); // 宣告一個List存放ListView要顯示的資料
        try {
            HttpClient httpclient = new DefaultHttpClient(); // 宣告一個HttpClient
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
            inputStream = httpResponse.getEntity().getContent(); // 用InputStream存放取回的內容
            int resCode = httpResponse.getStatusLine().getStatusCode(); // 宣告一個int去取得網頁執行的回應碼
            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
            String responseString = readAll(rd); // 宣告一個字串放置讀取BufferedReader後的結果
            String region = "臺北市";

            JSONObject NooJSON = new JSONObject(responseString); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
            if (NooJSON.has("Infos")) {
                JSONObject returnJSON = new JSONObject(NooJSON.get("Infos").toString());
                if (returnJSON.has("Info")) {
                    JSONArray respJSON = new JSONArray(returnJSON.get("Info").toString());
                    for (int i = 0; i < 20; i++) { // 用for迴圈判斷處理respJSON的資料
                        JSONObject tmpJSON = new JSONObject(respJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                        attJSON.put(tmpJSON); // 將JSONObject放回JSONArray方便處理資料(JSONArray才可判斷長度)
                    }
                    if (attJSON.length() > 0) { // 如果JSONArray有資料才做
                        for (int i = 0; i < attJSON.length(); i++) { // 用for迴圈判斷處理attJSON的資料
                            JSONObject tmpJSON = new JSONObject(attJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                            if (tmpJSON.has("Class")) {
                                String theme = tmpJSON.get("Class").toString();
                                if (theme.equals("1")) {
                                    if (tmpJSON.get("Region").toString().equals(region)) {
                                        HashMap<String, Object> hashMap = new HashMap<String, Object>(); // 宣告一個HashMap放置List資料
                                        hashMap.put("Pictures", "no piture");
                                        hashMap.put("Name", tmpJSON.getString("Name")); // 將name的資料放進HashMap裡
                                        hashMap.put("Rank", tmpJSON.getString("Grade"));// 將avgrank的資料放進HashMap裡
                                        Data.add(hashMap); // 把HashMap加到List裡
                                    }
                                }
                            }
                        }
                    }
                }
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Data;
    }

    /********************************************************
     * 普通飯店方法
     ********************************************************/
// region 旅館方法內容
    @SuppressWarnings("deprecation")
    public static List<HashMap<String, Object>> NormalHotel() {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(
                    new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork() // or
                            // .detectAll()
                            // for
                            // all
                            // detectable
                            // problems
                            .penaltyLog().build());
        }

        // TODO code application logic here
        JSONArray attJSON = new JSONArray(); // 宣告一個JSONArray存放respJSON處理後的資料
        String url = "http://140.131.114.161/~x4girls/json/Hotel.json"; // 宣告一個String存放網址
        InputStream inputStream = null; // 宣告一個InputStream
        String output = new String(); // 存放錯誤時的Not Found資訊
        List<HashMap<String, Object>> Data = new ArrayList<HashMap<String, Object>>(); // 宣告一個List存放ListView要顯示的資料
        try {
            HttpClient httpclient = new DefaultHttpClient(); // 宣告一個HttpClient
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
            inputStream = httpResponse.getEntity().getContent(); // 用InputStream存放取回的內容
            int resCode = httpResponse.getStatusLine().getStatusCode(); // 宣告一個int去取得網頁執行的回應碼
            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
            String responseString = readAll(rd); // 宣告一個字串放置讀取BufferedReader後的結果
            String region = "臺北市";
            JSONObject NooJSON = new JSONObject(responseString); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
            if (NooJSON.has("Infos")) {
                JSONObject returnJSON = new JSONObject(NooJSON.get("Infos").toString());
                if (returnJSON.has("Info")) {
                    JSONArray respJSON = new JSONArray(returnJSON.get("Info").toString());
                    for (int i = 0; i < 20; i++) { // 用for迴圈判斷處理respJSON的資料
                        JSONObject tmpJSON = new JSONObject(respJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                        attJSON.put(tmpJSON); // 將JSONObject放回JSONArray方便處理資料(JSONArray才可判斷長度)
                    }
                    if (attJSON.length() > 0) { // 如果JSONArray有資料才做
                        for (int i = 0; i < attJSON.length(); i++) { // 用for迴圈判斷處理attJSON的資料
                            JSONObject tmpJSON = new JSONObject(attJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                            if (tmpJSON.has("Class")) {
                                String theme = tmpJSON.get("Class").toString();
                                if (theme.equals("2")) {
                                    if (tmpJSON.get("Region").toString().equals(region)) {
                                        HashMap<String, Object> hashMap = new HashMap<String, Object>(); // 宣告一個HashMap放置List資料
                                        hashMap.put("Pictures", "no piture");
                                        hashMap.put("Name", tmpJSON.getString("Name")); // 將name的資料放進HashMap裡
                                        hashMap.put("Rank", tmpJSON.getString("Grade"));// 將avgrank的資料放進HashMap裡
                                        Data.add(hashMap); // 把HashMap加到List裡
                                    }
                                }
                            }
                        }
                    }
                }
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Data;
    }

    /********************************************************
     * 旅社方法
     ********************************************************/
// region 旅館方法內容
    @SuppressWarnings("deprecation")
    public static List<HashMap<String, Object>> Hostel() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(
                    new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork() // or
                            // .detectAll()
                            // for
                            // all
                            // detectable
                            // problems
                            .penaltyLog().build());
        }

        // TODO code application logic here
        JSONArray attJSON = new JSONArray(); // 宣告一個JSONArray存放respJSON處理後的資料
        String url = "http://140.131.114.161/~x4girls/json/Hotel.json"; // 宣告一個String存放網址
        InputStream inputStream = null; // 宣告一個InputStream
        String output = new String(); // 存放錯誤時的Not Found資訊
        List<HashMap<String, Object>> Data = new ArrayList<HashMap<String, Object>>(); // 宣告一個List存放ListView要顯示的資料
        try {
            HttpClient httpclient = new DefaultHttpClient(); // 宣告一個HttpClient
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
            inputStream = httpResponse.getEntity().getContent(); // 用InputStream存放取回的內容
            int resCode = httpResponse.getStatusLine().getStatusCode(); // 宣告一個int去取得網頁執行的回應碼
            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
            String responseString = readAll(rd); // 宣告一個字串放置讀取BufferedReader後的結果
            String region = "臺北市";
            JSONObject NooJSON = new JSONObject(responseString); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
            if (NooJSON.has("Infos")) {
                JSONObject returnJSON = new JSONObject(NooJSON.get("Infos").toString());
                if (returnJSON.has("Info")) {
                    JSONArray respJSON = new JSONArray(returnJSON.get("Info").toString());
                    for (int i = 0; i < 20; i++) { // 用for迴圈判斷處理respJSON的資料
                        JSONObject tmpJSON = new JSONObject(respJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                        attJSON.put(tmpJSON); // 將JSONObject放回JSONArray方便處理資料(JSONArray才可判斷長度)
                    }
                    if (attJSON.length() > 0) { // 如果JSONArray有資料才做
                        for (int i = 0; i < attJSON.length(); i++) { // 用for迴圈判斷處理attJSON的資料
                            JSONObject tmpJSON = new JSONObject(attJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                            if (tmpJSON.has("Class")) {
                                String theme = tmpJSON.get("Class").toString();
                                if (theme.equals("3")) {
                                    if (tmpJSON.get("Region").toString().equals(region)) {
                                        HashMap<String, Object> hashMap = new HashMap<String, Object>(); // 宣告一個HashMap放置List資料
                                        hashMap.put("Pictures", "no piture");
                                        hashMap.put("Name", tmpJSON.getString("Name")); // 將name的資料放進HashMap裡
                                        hashMap.put("Rank", tmpJSON.getString("Grade"));// 將avgrank的資料放進HashMap裡
                                        Data.add(hashMap); // 把HashMap加到List裡
                                    }
                                }
                            }
                        }
                    }
                }
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Data;
    }

    /********************************************************
     * 民宿方法
     ********************************************************/
// region 旅館方法內容
    @SuppressWarnings("deprecation")
    public static List<HashMap<String, Object>> BedAndBreakfastHotel() {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(
                    new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork() // or
                            // .detectAll()
                            // for
                            // all
                            // detectable
                            // problems
                            .penaltyLog().build());
        }

        // TODO code application logic here
        JSONArray attJSON = new JSONArray(); // 宣告一個JSONArray存放respJSON處理後的資料
        String url = "http://140.131.114.161/~x4girls/json/Hotel.json"; // 宣告一個String存放網址
        InputStream inputStream = null; // 宣告一個InputStream
        String output = new String(); // 存放錯誤時的Not Found資訊
        List<HashMap<String, Object>> Data = new ArrayList<HashMap<String, Object>>(); // 宣告一個List存放ListView要顯示的資料
        try {
            HttpClient httpclient = new DefaultHttpClient(); // 宣告一個HttpClient
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
            inputStream = httpResponse.getEntity().getContent(); // 用InputStream存放取回的內容
            int resCode = httpResponse.getStatusLine().getStatusCode(); // 宣告一個int去取得網頁執行的回應碼
            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
            String responseString = readAll(rd); // 宣告一個字串放置讀取BufferedReader後的結果
            String region = "臺北市";

            JSONObject NooJSON = new JSONObject(responseString); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
            if (NooJSON.has("Infos")) {
                JSONObject returnJSON = new JSONObject(NooJSON.get("Infos").toString());
                if (returnJSON.has("Info")) {
                    JSONArray respJSON = new JSONArray(returnJSON.get("Info").toString());
                    for (int i = 0; i < 20; i++) { // 用for迴圈判斷處理respJSON的資料
                        JSONObject tmpJSON = new JSONObject(respJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                        attJSON.put(tmpJSON); // 將JSONObject放回JSONArray方便處理資料(JSONArray才可判斷長度)
                    }
                    if (attJSON.length() > 0) { // 如果JSONArray有資料才做
                        for (int i = 0; i < attJSON.length(); i++) { // 用for迴圈判斷處理attJSON的資料
                            JSONObject tmpJSON = new JSONObject(attJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                            if (tmpJSON.has("Class")) {
                                String theme = tmpJSON.get("Class").toString();
                                if (theme.equals("4")) {
                                    if (tmpJSON.get("Region").toString().equals(region)) {
                                        HashMap<String, Object> hashMap = new HashMap<String, Object>(); // 宣告一個HashMap放置List資料
                                        hashMap.put("Pictures", "no piture");
                                        hashMap.put("Name", tmpJSON.getString("Name")); // 將name的資料放進HashMap裡
                                        hashMap.put("Rank", tmpJSON.getString("Grade"));// 將avgrank的資料放進HashMap裡
                                        Data.add(hashMap); // 把HashMap加到List裡
                                    }
                                }
                            }
                        }
                    }
                }
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Data;
    }

//    static Handler mHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            switch(msg.what){
//                case sussce:
//                    break;
//            }
//        }
//    };

    /********************************************************
     * readAll指令
     ********************************************************/
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {

            sb.append((char) cp);
        }
        return sb.toString();
    }

    /********************************************************
     * 圖片縮放調整大小，不會超出記憶體
     ********************************************************/
    public static Bitmap decodeBitmapFromInputStream(InputStream inputStream, int reqWidth, int reqHeight)
            throws IOException { // 宣告一個傳回值為Bitmap的方法，參數要傳進InputStream, 寬, 高
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        inputStream.reset();
        // inputStream.mark(1024);
        return BitmapFactory.decodeStream(inputStream, null, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) { // 宣告一個傳回值為int的方法，計算圖片大小
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
