package tw.edu.ntubimd.formosa.taiwan.taipei;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tw.edu.ntubimd.formosa.R;
import tw.edu.ntubimd.formosa.taiwan.taipei.hotel.HotelClickActivity;
import tw.edu.ntubimd.formosa.taiwan.taipei.hotel.MemberHotelClickActivity;

public class TaipeiActivity extends AppCompatActivity {

    private TextView textview1; // Not Found文字放置
    private ImageButton btnAttraction, btnActivity, btnFood, btnHotel; // 旅遊景點Button
    private ListView listAttraction, listHotel; // ListView
    private Spinner spinnerCounty;    //縣市下拉式選單
    private Spinner spinnerType;    //類型下拉式選單
    private ArrayAdapter<String> listCounty;
    private ArrayAdapter<String> listType;
    private Context cContext;
    private Context tContext;
    List<HashMap<String, Object>> ListData = new ArrayList<HashMap<String, Object>>(); // 宣告一個List放置ListView要放的資料
    private SimpleAdapter listAdapterAttraction, listAdapterHotel; // 宣告一個SimpleAdapter
    private ProgressBar progressBar;
    private View moreView;
    private int lastItem;
    private int count;
    private int totalcount;
    private String county = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taipei);

        //region Toolbar設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("旅遊景點");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //取消AppTitle
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //endregion

        final String[] attcou = {"北部", "基隆市", "台北市", "新北市", "桃園市", "新竹縣", "新竹市", "苗栗縣"};
        final String[] attractiontype = {"全部類別", "風景區", "觀光景點", "主題樂園", "休閒樂活", "觀光工廠"};
        cContext = this.getApplicationContext();
        tContext = this.getApplicationContext();
        spinnerCounty = (Spinner) findViewById(R.id.spinnerCounty);
        spinnerType = (Spinner) findViewById(R.id.spinnerType);
        textview1 = (TextView) findViewById(R.id.textView1);
        btnAttraction = (ImageButton) findViewById(R.id.imageButtonAtt);
        btnActivity = (ImageButton) findViewById(R.id.imageButtonAtc);
        btnFood = (ImageButton) findViewById(R.id.imageButtonFood);
        btnHotel = (ImageButton) findViewById(R.id.imageButtonHotel);
        listAttraction = (ListView) findViewById(R.id.listAttraction);
        listHotel = (ListView) findViewById(R.id.listHotel);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        moreView = getLayoutInflater().inflate(R.layout.list_moreview, null);

        btnAttraction.setImageResource(R.drawable.icon_attraction_click);
        btnAttraction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnAttraction.setImageResource(R.drawable.icon_attraction_click);
                btnActivity.setImageResource(R.drawable.icon_activity);
                btnFood.setImageResource(R.drawable.icon_food);
                btnHotel.setImageResource(R.drawable.icon_hotel);

                progressBar.setVisibility(View.VISIBLE);
                listAttraction.setVisibility(View.VISIBLE);
                listHotel.setAdapter(null);
                listHotel.setVisibility(View.GONE);

                new attractionAsyncTask().execute();
            }
        });

        btnActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnActivity.setImageResource(R.drawable.icon_activity_click);
                btnAttraction.setImageResource(R.drawable.icon_attraction);
                btnFood.setImageResource(R.drawable.icon_food);
                btnHotel.setImageResource(R.drawable.icon_hotel);
            }
        });

        btnFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnFood.setImageResource(R.drawable.icon_food_click);
                btnActivity.setImageResource(R.drawable.icon_activity);
                btnAttraction.setImageResource(R.drawable.icon_attraction);
                btnHotel.setImageResource(R.drawable.icon_hotel);
            }
        });

        btnHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbar_title.setText("住宿指南");
                btnHotel.setImageResource(R.drawable.icon_hotel_click);
                btnActivity.setImageResource(R.drawable.icon_activity);
                btnAttraction.setImageResource(R.drawable.icon_attraction);
                btnFood.setImageResource(R.drawable.icon_food);

                progressBar.setVisibility(View.VISIBLE);
                listHotel.setVisibility(View.VISIBLE);
                listAttraction.setAdapter(null);
                listAttraction.setVisibility(View.GONE);

                new hotelAsyncTask().execute();
            }
        });

        listCounty = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, attcou);
        listType = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, attractiontype);

        spinnerCounty.setAdapter(listCounty);
        spinnerType.setAdapter(listType);

        spinnerCounty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (attcou[position].equals("桃園市")) {
                    progressBar.setVisibility(View.VISIBLE);
                    listAttraction.setAdapter(null);
                    listAttraction.setVisibility(View.GONE);
                    new attractionTaoyuanAsyncTask().execute();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //region UniversalImageLoader初始化
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration //創建默認的ImageLoader配置參數
                .createDefault(this);
        ImageLoader.getInstance().init(configuration); //Initialize ImageLoader with configuration.
        //endregion
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //返回鍵
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        new attractionAsyncTask().execute();

        super.onWindowFocusChanged(hasFocus);

    }

    public class ListClickHandlerAttraction implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
            final File file = new File(getApplication().getFilesDir().getParentFile().getPath() + "/shared_prefs/", "LoginInfo.xml");
            // TODO Auto-generated method stub
            if (county.equals("台北市")) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONArray respJSON = new JSONArray(); // 宣告一個JSONArray
                        String url = "http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=36847f3f-deff-4183-a5bb-800737591de5"; // 宣告一個String存放網址
                        InputStream inputStream = null; // 宣告一個InputStream
                        String data = "";

                        try {
                            HttpClient httpclient = new DefaultHttpClient(); // 宣告一個HttpClient
                            HttpResponse httpResponse = httpclient.execute(new HttpGet(url)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
                            inputStream = httpResponse.getEntity().getContent(); // 用InputStream存放取回的內容
                            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
                            String responseString = readAll(rd); // 宣告一個字串放置讀取BufferedReader後的結果
                            JSONObject OneJSON = new JSONObject(responseString); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                            if (OneJSON.has("result")) { //抓出Infos
                                JSONObject TwoJSON = new JSONObject(OneJSON.get("result").toString());
                                if (TwoJSON.has("results")) { //抓出Info
                                    respJSON = new JSONArray(TwoJSON.get("results").toString());
                                    JSONObject itemJSON = new JSONObject(respJSON.get(position).toString());
                                    data = itemJSON.toString();
                                }
                            }
                            inputStream.close();

                            TextView listText = (TextView) view.findViewById(R.id.textView2);
                            String text = listText.getText().toString();
                            if (file.exists()) {
                                Intent intent = new Intent(TaipeiActivity.this, MemberTaipeiActivityClick.class);
                                intent.putExtra("selected-item", text);
                                intent.putExtra("county", county);
                                intent.putExtra("data", data);
                                startActivity(intent);
                            }else {
                                Intent intent = new Intent(TaipeiActivity.this, TaipeiActivityClick.class);
                                intent.putExtra("selected-item", text);
                                intent.putExtra("county", county);
                                intent.putExtra("data", data);
                                startActivity(intent);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (UnsupportedOperationException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            if (county.equals("桃園市")) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONArray respJSON = new JSONArray(); // 宣告一個JSONArray
                        String url = "http://data.tycg.gov.tw/api/v1/rest/datastore/bd906b29-9006-40ed-8bd7-67597c2577fc?format=json"; // 宣告一個String存放網址
                        InputStream inputStream = null; // 宣告一個InputStream
                        String data = "";

                        try {
                            HttpClient httpclient = new DefaultHttpClient(); // 宣告一個HttpClient
                            HttpResponse httpResponse = httpclient.execute(new HttpGet(url)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
                            inputStream = httpResponse.getEntity().getContent(); // 用InputStream存放取回的內容
                            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
                            String responseString = readAll(rd); // 宣告一個字串放置讀取BufferedReader後的結果
                            JSONObject OneJSON = new JSONObject(responseString); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                            if (OneJSON.has("result")) { //抓出Infos
                                JSONObject TwoJSON = new JSONObject(OneJSON.get("result").toString());
                                if (TwoJSON.has("records")) { //抓出Info
                                    respJSON = new JSONArray(TwoJSON.get("records").toString());
                                    JSONObject itemJSON = new JSONObject(respJSON.get(position).toString());
                                    data = itemJSON.toString();
                                }
                            }
                            inputStream.close();

                            TextView listText = (TextView) view.findViewById(R.id.textView2);
                            String text = listText.getText().toString();
                            if (file.exists()) {
                                Intent intent = new Intent(TaipeiActivity.this, MemberTaipeiActivityClick.class);
                                intent.putExtra("selected-item", text);
                                intent.putExtra("county", county);
                                intent.putExtra("data", data);
                                startActivity(intent);
                            }else {
                                Intent intent = new Intent(TaipeiActivity.this, TaipeiActivityClick.class);
                                intent.putExtra("selected-item", text);
                                intent.putExtra("county", county);
                                intent.putExtra("data", data);
                                startActivity(intent);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (UnsupportedOperationException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

    public class ListClickHandlerHotel implements AdapterView.OnItemClickListener {
        final File file = new File(getApplication().getFilesDir().getParentFile().getPath() + "/shared_prefs/", "LoginInfo.xml");
        @Override
        public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
            // TODO Auto-generated method stub
            if (county.equals("台北市")) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONArray respJSON = new JSONArray(); // 宣告一個JSONArray
                        String url = "http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=6f4e0b9b-8cb1-4b1d-a5c4-febd90f62469"; // 宣告一個String存放網址
                        InputStream inputStream = null; // 宣告一個InputStream
                        String data = "";

                        try {
                            HttpClient httpclient = new DefaultHttpClient(); // 宣告一個HttpClient
                            HttpResponse httpResponse = httpclient.execute(new HttpGet(url)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
                            inputStream = httpResponse.getEntity().getContent(); // 用InputStream存放取回的內容
                            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
                            String responseString = readAll(rd); // 宣告一個字串放置讀取BufferedReader後的結果
                            JSONObject OneJSON = new JSONObject(responseString); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                            if (OneJSON.has("result")) { //抓出Infos
                                JSONObject TwoJSON = new JSONObject(OneJSON.get("result").toString());
                                if (TwoJSON.has("results")) { //抓出Info
                                    respJSON = new JSONArray(TwoJSON.get("results").toString());
                                    JSONObject itemJSON = new JSONObject(respJSON.get(position).toString());
                                    data = itemJSON.toString();
                                }
                            }
                            inputStream.close();

                            TextView listText = (TextView) view.findViewById(R.id.textView2);
                            String text = listText.getText().toString();
                            if (file.exists()){
                                Intent intent = new Intent(TaipeiActivity.this, MemberHotelClickActivity.class);
                                intent.putExtra("selected-item", text);
                                intent.putExtra("county", county);
                                intent.putExtra("data", data);
                                startActivity(intent);
                            }else {
                                Intent intent = new Intent(TaipeiActivity.this, HotelClickActivity.class);
                                intent.putExtra("selected-item", text);
                                intent.putExtra("county", county);
                                intent.putExtra("data", data);
                                startActivity(intent);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (UnsupportedOperationException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

    //region Taipei.Attraction() 傳回台北Attraction資料
    class attractionAsyncTask extends AsyncTask<String, Integer, Integer> {
        @Override
        protected Integer doInBackground(String... param) {
            county = "台北市";
            count = 0;
            totalcount = 0;
            ListData.clear();

            JSONArray respJSON = new JSONArray(); // 宣告一個JSONArray
            JSONArray attJSON = new JSONArray(); // 宣告一個JSONArray存放respJSON處理後的資料
            String url = "http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=36847f3f-deff-4183-a5bb-800737591de5"; // 宣告一個String存放網址
            InputStream inputStream = null; // 宣告一個InputStream

            try {
                HttpClient httpclient = new DefaultHttpClient(); // 宣告一個HttpClient
                HttpResponse httpResponse = httpclient.execute(new HttpGet(url)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
                inputStream = httpResponse.getEntity().getContent(); // 用InputStream存放取回的內容
                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
                String responseString = readAll(rd); // 宣告一個字串放置讀取BufferedReader後的結果
                JSONObject OneJSON = new JSONObject(responseString); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                if (OneJSON.has("result")) { //抓出Infos
                    JSONObject TwoJSON = new JSONObject(OneJSON.get("result").toString());
                    if (TwoJSON.has("results")) { //抓出Info
                        respJSON = new JSONArray(TwoJSON.get("results").toString());
                        totalcount = respJSON.length();
                        for (int i = 0; i < 3; i++) { // 用for迴圈判斷處理respJSON的資料
                            JSONObject tmpJSON = new JSONObject(respJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                            attJSON.put(tmpJSON); // 將JSONObject放回JSONArray方便處理資料(JSONArray才可判斷長度)

                            if (tmpJSON.has("stitle")) {
                                HashMap<String, Object> hashMap = new HashMap<String, Object>(); // 宣告一個HashMap放置List資料
                                if (!tmpJSON.isNull("file")) {
                                    String tmp = tmpJSON.get("file").toString();
                                    if (tmp.length() > 0) {
                                        String pictureUrl = tmp.substring(0, tmp.indexOf("http", 4));
                                        DisplayImageOptions options = new DisplayImageOptions.Builder()
                                                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                                                .bitmapConfig(Bitmap.Config.RGB_565)
                                                .build();
                                        ImageLoader imageLoader = ImageLoader.getInstance();  // Get singleton instance
                                        Bitmap bitmap = imageLoader.loadImageSync(pictureUrl, options);

                                        hashMap.put("Pictures", bitmap);
                                    } else {
                                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_picture);
                                        hashMap.put("Pictures", bitmap);
                                    }
                                } else {
                                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_picture);
                                    hashMap.put("Pictures", bitmap);
                                }

                                hashMap.put("Name", tmpJSON.get("stitle").toString());
                                hashMap.put("Rank", "5");// 將avgrank的資料放進HashMap裡
                                ListData.add(hashMap);
                            }
                        }
                    }
                }
                count = ListData.size();
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
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            listAdapterAttraction = new SimpleAdapter(getApplicationContext(), ListData, R.layout.county_listview, // 宣告一個SimpleAdapter五個參數分別為(List位置,
                    // List資料來源,
                    // ListView介面檔,
                    // List裡資料的名稱,
                    // List裡資料的介面檔中id)
                    new String[]{"Pictures", "Name", "Rank"}, new int[]{R.id.imageView1, R.id.textView2, R.id.ratingBar1});

            listAdapterAttraction.setViewBinder(new SimpleAdapter.ViewBinder() { // 讓SimpleAdapter顯示網路抓取的圖片
                @Override
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if ((view instanceof ImageView) & (data instanceof Bitmap)) {
                        ImageView iv = (ImageView) view;
                        Bitmap bmp = (Bitmap) data;
                        iv.setImageBitmap((bmp));
                        return true;
                    }
                    if (view.getId() == R.id.ratingBar1) {// 讓SimpleAdapter抓取排行資料
                        String stringval = (String) data;
                        float ratingValue = Float.parseFloat(stringval);
                        RatingBar ratingBar = (RatingBar) view;
                        ratingBar.setRating(ratingValue);
                        return true;
                    }
                    return false;
                }
            });

            listAttraction.addFooterView(moreView);
            listAttraction.setAdapter(listAdapterAttraction); // ListView設置Adapter
            listAttraction.setOnItemClickListener(new ListClickHandlerAttraction());
            listAttraction.setOnScrollListener(new AbsListView.OnScrollListener() { // list Scroll事件
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (lastItem == count && scrollState == this.SCROLL_STATE_IDLE) {
                        moreView.setVisibility(view.VISIBLE);

                        new attractionMoreAsyncTask().execute();
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    lastItem = firstVisibleItem + visibleItemCount - 1;  //減1是因為上面加了個addFooterView
                }
            });
            progressBar.setVisibility(View.GONE);
        }
    }
    //endregion

    //region Taipei.Attraction() 傳回台北Attraction更多資料
    class attractionMoreAsyncTask extends AsyncTask<String, Integer, Integer> {
        @Override
        protected Integer doInBackground(String... param) {

            JSONArray respJSON = new JSONArray(); // 宣告一個JSONArray
            JSONArray attJSON = new JSONArray(); // 宣告一個JSONArray存放respJSON處理後的資料
            String url = "http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=36847f3f-deff-4183-a5bb-800737591de5"; // 宣告一個String存放網址
            InputStream inputStream = null; // 宣告一個InputStream

            try {
                HttpClient httpclient = new DefaultHttpClient(); // 宣告一個HttpClient
                HttpResponse httpResponse = httpclient.execute(new HttpGet(url)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
                inputStream = httpResponse.getEntity().getContent(); // 用InputStream存放取回的內容
                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
                String responseString = readAll(rd); // 宣告一個字串放置讀取BufferedReader後的結果
                JSONObject OneJSON = new JSONObject(responseString); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                if (OneJSON.has("result")) { //抓出Infos
                    JSONObject TwoJSON = new JSONObject(OneJSON.get("result").toString());
                    if (TwoJSON.has("results")) { //抓出Info
                        respJSON = new JSONArray(TwoJSON.get("results").toString());
                        if (count + 10 > totalcount) {
                            for (int i = count; i < totalcount; i++) { // 用for迴圈判斷處理respJSON的資料
                                JSONObject tmpJSON = new JSONObject(respJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                                attJSON.put(tmpJSON); // 將JSONObject放回JSONArray方便處理資料(JSONArray才可判斷長度)

                                if (tmpJSON.has("stitle")) {
                                    HashMap<String, Object> hashMap = new HashMap<String, Object>(); // 宣告一個HashMap放置List資料
                                    if (!tmpJSON.isNull("file")) {
                                        String tmp = tmpJSON.get("file").toString();
                                        if (tmp.length() > 0) {
                                            String pictureUrl = tmp.substring(0, tmp.indexOf("http", 4));
                                            DisplayImageOptions options = new DisplayImageOptions.Builder()
                                                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                                                    .bitmapConfig(Bitmap.Config.RGB_565)
                                                    .build();
                                            ImageLoader imageLoader = ImageLoader.getInstance();  // Get singleton instance
                                            Bitmap bitmap = imageLoader.loadImageSync(pictureUrl, options);

                                            hashMap.put("Pictures", bitmap);
                                        } else {
                                            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_picture);
                                            hashMap.put("Pictures", bitmap);
                                        }
                                    } else {
                                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_picture);
                                        hashMap.put("Pictures", bitmap);
                                    }

                                    hashMap.put("Name", tmpJSON.get("stitle").toString());
                                    hashMap.put("Rank", "5");// 將avgrank的資料放進HashMap裡
                                    ListData.add(hashMap);
                                }
                            }
                        } else {
                            for (int i = count; i < count + 10; i++) { // 用for迴圈判斷處理respJSON的資料
                                JSONObject tmpJSON = new JSONObject(respJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                                attJSON.put(tmpJSON); // 將JSONObject放回JSONArray方便處理資料(JSONArray才可判斷長度)

                                if (tmpJSON.has("stitle")) {
                                    HashMap<String, Object> hashMap = new HashMap<String, Object>(); // 宣告一個HashMap放置List資料
                                    if (tmpJSON.has("file")) {
                                        String tmp = tmpJSON.get("file").toString();
                                        String pictureUrl = tmp.substring(0, tmp.indexOf("http", 4));
                                        DisplayImageOptions options = new DisplayImageOptions.Builder()
                                                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                                                .bitmapConfig(Bitmap.Config.RGB_565)
                                                .build();
                                        ImageLoader imageLoader = ImageLoader.getInstance();  // Get singleton instance
                                        Bitmap bitmap = imageLoader.loadImageSync(pictureUrl, options);

                                        hashMap.put("Pictures", bitmap);
                                    } else {
                                        hashMap.put("Pictures", "no picture");
                                    }

                                    hashMap.put("Name", tmpJSON.get("stitle").toString());
                                    hashMap.put("Rank", "5");// 將avgrank的資料放進HashMap裡
                                    ListData.add(hashMap);
                                }
                            }
                        }
                    }
                }
                count = ListData.size();
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
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            listAdapterAttraction.notifyDataSetChanged();
            moreView.setVisibility(View.INVISIBLE);

            if (count == totalcount) {
                Toast.makeText(TaipeiActivity.this, "已經沒有了", Toast.LENGTH_SHORT).show();
                listAttraction.removeFooterView(moreView); //移除底部視圖
            }
        }
    }
    //endregion

    //region Taoyuan.Attraction() 傳回台北Attraction資料
    class attractionTaoyuanAsyncTask extends AsyncTask<String, Integer, Integer> {
        @Override
        protected Integer doInBackground(String... param) {
            county = "桃園市";
            count = 0;
            totalcount = 0;
            ListData.clear();

            JSONArray respJSON = new JSONArray(); // 宣告一個JSONArray
            JSONArray attJSON = new JSONArray(); // 宣告一個JSONArray存放respJSON處理後的資料
            String url = "http://data.tycg.gov.tw/api/v1/rest/datastore/bd906b29-9006-40ed-8bd7-67597c2577fc?format=json"; // 宣告一個String存放網址
            InputStream inputStream = null; // 宣告一個InputStream

            try {
                HttpClient httpclient = new DefaultHttpClient(); // 宣告一個HttpClient
                HttpResponse httpResponse = httpclient.execute(new HttpGet(url)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
                inputStream = httpResponse.getEntity().getContent(); // 用InputStream存放取回的內容
                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
                String responseString = readAll(rd); // 宣告一個字串放置讀取BufferedReader後的結果
                JSONObject OneJSON = new JSONObject(responseString); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                if (OneJSON.has("result")) { //抓出Infos
                    JSONObject TwoJSON = new JSONObject(OneJSON.get("result").toString());
                    if (TwoJSON.has("records")) { //抓出Info
                        respJSON = new JSONArray(TwoJSON.get("records").toString());
                        totalcount = respJSON.length();
                        for (int i = 0; i < 3; i++) { // 用for迴圈判斷處理respJSON的資料
                            JSONObject tmpJSON = new JSONObject(respJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                            attJSON.put(tmpJSON); // 將JSONObject放回JSONArray方便處理資料(JSONArray才可判斷長度)

                            if (tmpJSON.has("Name")) {
                                HashMap<String, Object> hashMap = new HashMap<String, Object>(); // 宣告一個HashMap放置List資料
                                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_picture);
                                hashMap.put("Pictures", bitmap);

                                hashMap.put("Name", tmpJSON.get("Name").toString());
                                hashMap.put("Rank", "5");// 將avgrank的資料放進HashMap裡
                                ListData.add(hashMap);
                            }
                        }
                    }
                }
                count = ListData.size();
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
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            listAdapterAttraction = new SimpleAdapter(getApplicationContext(), ListData, R.layout.county_listview, // 宣告一個SimpleAdapter五個參數分別為(List位置,
                    // List資料來源,
                    // ListView介面檔,
                    // List裡資料的名稱,
                    // List裡資料的介面檔中id)
                    new String[]{"Pictures", "Name", "Rank"}, new int[]{R.id.imageView1, R.id.textView2, R.id.ratingBar1});

            listAdapterAttraction.setViewBinder(new SimpleAdapter.ViewBinder() { // 讓SimpleAdapter顯示網路抓取的圖片
                @Override
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if ((view instanceof ImageView) & (data instanceof Bitmap)) {
                        ImageView iv = (ImageView) view;
                        Bitmap bmp = (Bitmap) data;
                        iv.setImageBitmap((bmp));
                        return true;
                    }
                    if (view.getId() == R.id.ratingBar1) {// 讓SimpleAdapter抓取排行資料
                        String stringval = (String) data;
                        float ratingValue = Float.parseFloat(stringval);
                        RatingBar ratingBar = (RatingBar) view;
                        ratingBar.setRating(ratingValue);
                        return true;
                    }
                    return false;
                }
            });

            listAttraction.setVisibility(View.VISIBLE);
            listAttraction.addFooterView(moreView);
            listAttraction.setAdapter(listAdapterAttraction); // ListView設置Adapter
            listAttraction.setOnItemClickListener(new ListClickHandlerAttraction());
            listAttraction.setOnScrollListener(new AbsListView.OnScrollListener() { // list Scroll事件
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (lastItem == count && scrollState == this.SCROLL_STATE_IDLE) {
                        moreView.setVisibility(view.VISIBLE);

                        new attractionTaoyuanMoreAsyncTask().execute();
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    lastItem = firstVisibleItem + visibleItemCount - 1;  //減1是因為上面加了個addFooterView
                }
            });
            progressBar.setVisibility(View.GONE);
        }
    }
    //endregion

    //region Taipei.Attraction() 傳回台北Attraction更多資料
    class attractionTaoyuanMoreAsyncTask extends AsyncTask<String, Integer, Integer> {
        @Override
        protected Integer doInBackground(String... param) {

            JSONArray respJSON = new JSONArray(); // 宣告一個JSONArray
            JSONArray attJSON = new JSONArray(); // 宣告一個JSONArray存放respJSON處理後的資料
            String url = "http://data.tycg.gov.tw/api/v1/rest/datastore/bd906b29-9006-40ed-8bd7-67597c2577fc?format=json"; // 宣告一個String存放網址
            InputStream inputStream = null; // 宣告一個InputStream

            try {
                HttpClient httpclient = new DefaultHttpClient(); // 宣告一個HttpClient
                HttpResponse httpResponse = httpclient.execute(new HttpGet(url)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
                inputStream = httpResponse.getEntity().getContent(); // 用InputStream存放取回的內容
                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
                String responseString = readAll(rd); // 宣告一個字串放置讀取BufferedReader後的結果
                JSONObject OneJSON = new JSONObject(responseString); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                if (OneJSON.has("result")) { //抓出Infos
                    JSONObject TwoJSON = new JSONObject(OneJSON.get("result").toString());
                    if (TwoJSON.has("records")) { //抓出Info
                        respJSON = new JSONArray(TwoJSON.get("records").toString());
                        if (count + 10 > totalcount) {
                            for (int i = count; i < totalcount; i++) { // 用for迴圈判斷處理respJSON的資料
                                JSONObject tmpJSON = new JSONObject(respJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                                attJSON.put(tmpJSON); // 將JSONObject放回JSONArray方便處理資料(JSONArray才可判斷長度)

                                if (tmpJSON.has("Name")) {
                                    HashMap<String, Object> hashMap = new HashMap<String, Object>(); // 宣告一個HashMap放置List資料

                                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_picture);
                                    hashMap.put("Pictures", bitmap);

                                    hashMap.put("Name", tmpJSON.get("Name").toString());
                                    hashMap.put("Rank", "5");// 將avgrank的資料放進HashMap裡
                                    ListData.add(hashMap);
                                }
                            }
                        } else {
                            for (int i = count; i < count + 10; i++) { // 用for迴圈判斷處理respJSON的資料
                                JSONObject tmpJSON = new JSONObject(respJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                                attJSON.put(tmpJSON); // 將JSONObject放回JSONArray方便處理資料(JSONArray才可判斷長度)

                                if (tmpJSON.has("Name")) {
                                    HashMap<String, Object> hashMap = new HashMap<String, Object>(); // 宣告一個HashMap放置List資料

                                    hashMap.put("Pictures", "no picture");

                                    hashMap.put("Name", tmpJSON.get("stitle").toString());
                                    hashMap.put("Rank", "5");// 將avgrank的資料放進HashMap裡
                                    ListData.add(hashMap);
                                }
                            }
                        }
                    }
                }
                count = ListData.size();
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
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            listAdapterAttraction.notifyDataSetChanged();
            moreView.setVisibility(View.INVISIBLE);

            if (count == totalcount) {
                Toast.makeText(TaipeiActivity.this, "已經沒有了", Toast.LENGTH_SHORT).show();
                listAttraction.removeFooterView(moreView); //移除底部視圖
            }
        }
    }
    //endregion

    //region Taipei.Attraction() 傳回台北Hotel資料
    class hotelAsyncTask extends AsyncTask<String, Integer, Integer> {
        @Override
        protected Integer doInBackground(String... param) {
            county = "台北市";
            count = 0;
            totalcount = 0;
            ListData.clear();

            JSONArray respJSON = new JSONArray(); // 宣告一個JSONArray
            JSONArray attJSON = new JSONArray(); // 宣告一個JSONArray存放respJSON處理後的資料
            String url = "http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=6f4e0b9b-8cb1-4b1d-a5c4-febd90f62469"; // 宣告一個String存放網址
            InputStream inputStream = null; // 宣告一個InputStream

            try {
                HttpClient httpclient = new DefaultHttpClient(); // 宣告一個HttpClient
                HttpResponse httpResponse = httpclient.execute(new HttpGet(url)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
                inputStream = httpResponse.getEntity().getContent(); // 用InputStream存放取回的內容
                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
                String responseString = readAll(rd); // 宣告一個字串放置讀取BufferedReader後的結果
                JSONObject OneJSON = new JSONObject(responseString); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                if (OneJSON.has("result")) { //抓出Infos
                    JSONObject TwoJSON = new JSONObject(OneJSON.get("result").toString());
                    if (TwoJSON.has("results")) { //抓出Info
                        respJSON = new JSONArray(TwoJSON.get("results").toString());
                        totalcount = respJSON.length();
                        for (int i = 0; i < 3; i++) { // 用for迴圈判斷處理respJSON的資料
                            JSONObject tmpJSON = new JSONObject(respJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                            attJSON.put(tmpJSON); // 將JSONObject放回JSONArray方便處理資料(JSONArray才可判斷長度)

                            if (tmpJSON.has("stitle")) {
                                HashMap<String, Object> hashMap = new HashMap<String, Object>(); // 宣告一個HashMap放置List資料
                                if (!tmpJSON.isNull("file")) {
                                    String tmp = tmpJSON.get("file").toString();
                                    if (tmp.length() > 0) {
                                        String pictureUrl = tmp.substring(0, tmp.indexOf("http", 4));
                                        DisplayImageOptions options = new DisplayImageOptions.Builder()
                                                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                                                .bitmapConfig(Bitmap.Config.RGB_565)
                                                .build();
                                        ImageLoader imageLoader = ImageLoader.getInstance();  // Get singleton instance
                                        Bitmap bitmap = imageLoader.loadImageSync(pictureUrl, options);

                                        hashMap.put("Pictures", bitmap);
                                    } else {
                                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_picture);
                                        hashMap.put("Pictures", bitmap);
                                    }
                                } else {
                                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_picture);
                                    hashMap.put("Pictures", bitmap);
                                }

                                hashMap.put("Name", tmpJSON.get("stitle").toString());
                                hashMap.put("Rank", "5");// 將avgrank的資料放進HashMap裡
                                ListData.add(hashMap);
                            }
                        }
                    }
                }
                count = ListData.size();
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
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            listAdapterHotel = new SimpleAdapter(getApplicationContext(), ListData, R.layout.county_listview, // 宣告一個SimpleAdapter五個參數分別為(List位置,
                    // List資料來源,
                    // ListView介面檔,
                    // List裡資料的名稱,
                    // List裡資料的介面檔中id)
                    new String[]{"Pictures", "Name", "Rank"}, new int[]{R.id.imageView1, R.id.textView2, R.id.ratingBar1});

            listAdapterHotel.setViewBinder(new SimpleAdapter.ViewBinder() { // 讓SimpleAdapter顯示網路抓取的圖片
                @Override
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if ((view instanceof ImageView) & (data instanceof Bitmap)) {
                        ImageView iv = (ImageView) view;
                        Bitmap bmp = (Bitmap) data;
                        iv.setImageBitmap((bmp));
                        return true;
                    }
                    if (view.getId() == R.id.ratingBar1) {// 讓SimpleAdapter抓取排行資料
                        String stringval = (String) data;
                        float ratingValue = Float.parseFloat(stringval);
                        RatingBar ratingBar = (RatingBar) view;
                        ratingBar.setRating(ratingValue);
                        return true;
                    }
                    return false;
                }
            });

            listHotel.addFooterView(moreView);
            listHotel.setAdapter(listAdapterHotel); // ListView設置Adapter
            listHotel.setOnItemClickListener(new ListClickHandlerHotel());
            listHotel.setOnScrollListener(new AbsListView.OnScrollListener() { // list Scroll事件
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (lastItem == count && scrollState == this.SCROLL_STATE_IDLE) {
                        moreView.setVisibility(view.VISIBLE);

                        new hotelMoreAsyncTask().execute();
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    lastItem = firstVisibleItem + visibleItemCount - 1;  //減1是因為上面加了個addFooterView
                }
            });
            progressBar.setVisibility(View.GONE);
        }
    }
    //endregion

    //region Taipei.Attraction() 傳回台北Hotel更多資料
    class hotelMoreAsyncTask extends AsyncTask<String, Integer, Integer> {
        @Override
        protected Integer doInBackground(String... param) {

            JSONArray respJSON = new JSONArray(); // 宣告一個JSONArray
            JSONArray attJSON = new JSONArray(); // 宣告一個JSONArray存放respJSON處理後的資料
            String url = "http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=6f4e0b9b-8cb1-4b1d-a5c4-febd90f62469"; // 宣告一個String存放網址
            InputStream inputStream = null; // 宣告一個InputStream

            try {
                HttpClient httpclient = new DefaultHttpClient(); // 宣告一個HttpClient
                HttpResponse httpResponse = httpclient.execute(new HttpGet(url)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
                inputStream = httpResponse.getEntity().getContent(); // 用InputStream存放取回的內容
                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
                String responseString = readAll(rd); // 宣告一個字串放置讀取BufferedReader後的結果
                JSONObject OneJSON = new JSONObject(responseString); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                if (OneJSON.has("result")) { //抓出Infos
                    JSONObject TwoJSON = new JSONObject(OneJSON.get("result").toString());
                    if (TwoJSON.has("results")) { //抓出Info
                        respJSON = new JSONArray(TwoJSON.get("results").toString());
                        if (count + 10 > totalcount) {
                            for (int i = count; i < totalcount; i++) { // 用for迴圈判斷處理respJSON的資料
                                JSONObject tmpJSON = new JSONObject(respJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                                attJSON.put(tmpJSON); // 將JSONObject放回JSONArray方便處理資料(JSONArray才可判斷長度)

                                if (tmpJSON.has("stitle")) {
                                    HashMap<String, Object> hashMap = new HashMap<String, Object>(); // 宣告一個HashMap放置List資料
                                    if (!tmpJSON.isNull("file")) {
                                        String tmp = tmpJSON.get("file").toString();
                                        if (tmp.length() > 0) {
                                            String pictureUrl = tmp.substring(0, tmp.indexOf("http", 4));
                                            DisplayImageOptions options = new DisplayImageOptions.Builder()
                                                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                                                    .bitmapConfig(Bitmap.Config.RGB_565)
                                                    .build();
                                            ImageLoader imageLoader = ImageLoader.getInstance();  // Get singleton instance
                                            Bitmap bitmap = imageLoader.loadImageSync(pictureUrl, options);

                                            hashMap.put("Pictures", bitmap);
                                        } else {
                                            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_picture);
                                            hashMap.put("Pictures", bitmap);
                                        }
                                    } else {
                                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_picture);
                                        hashMap.put("Pictures", bitmap);
                                    }

                                    hashMap.put("Name", tmpJSON.get("stitle").toString());
                                    hashMap.put("Rank", "5");// 將avgrank的資料放進HashMap裡
                                    ListData.add(hashMap);
                                }
                            }
                        } else {
                            for (int i = count; i < count + 10; i++) { // 用for迴圈判斷處理respJSON的資料
                                JSONObject tmpJSON = new JSONObject(respJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                                attJSON.put(tmpJSON); // 將JSONObject放回JSONArray方便處理資料(JSONArray才可判斷長度)

                                if (tmpJSON.has("stitle")) {
                                    HashMap<String, Object> hashMap = new HashMap<String, Object>(); // 宣告一個HashMap放置List資料
                                    if (tmpJSON.has("file")) {
                                        String tmp = tmpJSON.get("file").toString();
                                        String pictureUrl = tmp.substring(0, tmp.indexOf("http", 4));
                                        DisplayImageOptions options = new DisplayImageOptions.Builder()
                                                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                                                .bitmapConfig(Bitmap.Config.RGB_565)
                                                .build();
                                        ImageLoader imageLoader = ImageLoader.getInstance();  // Get singleton instance
                                        Bitmap bitmap = imageLoader.loadImageSync(pictureUrl, options);

                                        hashMap.put("Pictures", bitmap);
                                    } else {
                                        hashMap.put("Pictures", "no picture");
                                    }

                                    hashMap.put("Name", tmpJSON.get("stitle").toString());
                                    hashMap.put("Rank", "5");// 將avgrank的資料放進HashMap裡
                                    ListData.add(hashMap);
                                }
                            }
                        }
                    }
                }
                count = ListData.size();
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
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            listAdapterHotel.notifyDataSetChanged();
            moreView.setVisibility(View.INVISIBLE);

            if (count == totalcount) {
                Toast.makeText(TaipeiActivity.this, "已經沒有了", Toast.LENGTH_SHORT).show();
                listHotel.removeFooterView(moreView); //移除底部視圖
            }
        }
    }
    //endregion

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {

            sb.append((char) cp);
        }
        return sb.toString();
    }

    
}
