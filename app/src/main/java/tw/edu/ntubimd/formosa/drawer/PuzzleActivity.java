package tw.edu.ntubimd.formosa.drawer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import tw.edu.ntubimd.formosa.MemberMainActivity;
import tw.edu.ntubimd.formosa.R;

public class PuzzleActivity extends AppCompatActivity {

    private boolean getService = false;  //是否已開啟定位服務
    private ListView puzzleList;
    private RelativeLayout puzzleQuestionLayout;
    private LinearLayout drawer_view;
    private ImageView tw1ImageView, tw2ImageView, tw3ImageView, tw4ImageView, tw5ImageView, tw6ImageView, tw7ImageView,
            tw8ImageView, tw9ImageView, tw10ImageView, tw11ImageView, tw12ImageView, tw13ImageView, tw14ImageView, tw15ImageView,
            tw16ImageView, tw17ImageView, tw18ImageView, tw19ImageView, tw20ImageView, tw21ImageView, tw22ImageView;
    private ImageView puzzleQuestionImage, getLeftTop, getLelfButtom, getRightTop, getRightButtom;
    private Button buttonYes, getButton, nextButton;

    final String[] drawer_menu = new String[]{"基隆市", "台北市", "新北市", "桃園市", "新竹縣", "新竹市", "苗栗縣", "台中市", "彰化縣", "南投縣", "雲林縣",
            "嘉義縣", "嘉義市", "台南市", "高雄市", "屏東縣", "宜蘭縣", "花蓮縣", "台東縣", "澎湖縣", "金門縣", "連江縣"};

    private int countyPuzzleCount;
    private String idString, county, level;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);

        File file = new File(getApplication().getFilesDir().getParentFile().getPath() + "/shared_prefs/", "LoginInfo.xml");
        if (file.exists()) {
            ReadValue();
        }

        puzzleList = (ListView) findViewById(R.id.drawerList);

        puzzleQuestionLayout = (RelativeLayout) findViewById(R.id.puzzleQuestionLayout);
        drawer_view = (LinearLayout) findViewById(R.id.drawer_view);
        buttonYes = (Button) findViewById(R.id.buttonYes);
        getButton = (Button) findViewById(R.id.getButton);
        nextButton = (Button) findViewById(R.id.nextButton);

        tw1ImageView = (ImageView) findViewById(R.id.tw1ImageView);
        tw2ImageView = (ImageView) findViewById(R.id.tw2ImageView);
        tw3ImageView = (ImageView) findViewById(R.id.tw3ImageView);
        tw4ImageView = (ImageView) findViewById(R.id.tw4ImageView);
        tw5ImageView = (ImageView) findViewById(R.id.tw5ImageView);
        tw6ImageView = (ImageView) findViewById(R.id.tw6ImageView);
        tw7ImageView = (ImageView) findViewById(R.id.tw7ImageView);
        tw8ImageView = (ImageView) findViewById(R.id.tw8ImageView);
        tw9ImageView = (ImageView) findViewById(R.id.tw9ImageView);
        tw10ImageView = (ImageView) findViewById(R.id.tw10ImageView);
        tw11ImageView = (ImageView) findViewById(R.id.tw11ImageView);
        tw12ImageView = (ImageView) findViewById(R.id.tw12ImageView);
        tw13ImageView = (ImageView) findViewById(R.id.tw13ImageView);
        tw14ImageView = (ImageView) findViewById(R.id.tw14ImageView);
        tw15ImageView = (ImageView) findViewById(R.id.tw15ImageView);
        tw16ImageView = (ImageView) findViewById(R.id.tw16ImageView);
        tw17ImageView = (ImageView) findViewById(R.id.tw17ImageView);
        tw18ImageView = (ImageView) findViewById(R.id.tw18ImageView);
        tw19ImageView = (ImageView) findViewById(R.id.tw19ImageView);
        tw20ImageView = (ImageView) findViewById(R.id.tw20ImageView);
        tw21ImageView = (ImageView) findViewById(R.id.tw21ImageView);
        tw22ImageView = (ImageView) findViewById(R.id.tw22ImageView);

        puzzleQuestionImage = (ImageView) findViewById(R.id.puzzleQuestionImage);
        getLeftTop = (ImageView) findViewById(R.id.getLeftTop);
        getLelfButtom = (ImageView) findViewById(R.id.getLelfButtom);
        getRightTop = (ImageView) findViewById(R.id.getRightTop);
        getRightButtom = (ImageView) findViewById(R.id.getRightButtom);

        //region Toolbar設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("我的拼圖");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //取消AppTitle
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //endregion

        //region 設定Drawer
        ListView drawerList = (ListView) findViewById(R.id.drawerList);
        ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        final int drawer_icon = R.drawable.puzzle_drawer_icon;
        for (int i = 0; i < drawer_menu.length; i++) {
            HashMap<String, Object> item = new HashMap<String, Object>();
            item.put("Icon", drawer_icon);
            item.put("Menu", drawer_menu[i]);
            list.add(item);
        }
        SimpleAdapter ListAdapter = new SimpleAdapter(this, list, R.layout.drawerlistview_puzzle, // 宣告一個SimpleAdapter五個參數分別為(List位置,
                // List資料來源,
                // ListView介面檔,
                // List裡資料的名稱,
                // List裡資料的介面檔中id)
                new String[]{"Icon", "Menu"}, new int[]{R.id.imageViewIcon, R.id.textViewName});
        drawerList.setAdapter(ListAdapter);
        drawerList.setDividerHeight(0);
        drawerList.setOnItemClickListener(new DrawerItemClickListener());
        //endregion

        //region 綁定Toolbar與Drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer); // 實作 drawer toggle 並放入 toolbar
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.syncState(); //將返回建置換成三條槓
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        //endregion

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                puzzleQuestionLayout.setVisibility(View.INVISIBLE);
                puzzleQuestionImage.setImageDrawable(null); //清圖片
                getLeftTop.setImageDrawable(null);
                getLelfButtom.setImageDrawable(null);
                getRightTop.setImageDrawable(null);
                getRightButtom.setImageDrawable(null);
                mDrawerLayout.openDrawer(drawer_view);
            }
        });

        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationManager status = (LocationManager) (PuzzleActivity.this.getSystemService(Context.LOCATION_SERVICE));
                if (status.isProviderEnabled(LocationManager.GPS_PROVIDER) || status.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    //如果GPS或網路定位開啟，呼叫locationServiceInitial()更新位置
                    getService = true;    //確認開啟定位服務
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    getPuzzleByCounty(county, level, countyPuzzleCount);
                                }
                            });
                        }
                    }).start();
                } else {
                    Toast.makeText(PuzzleActivity.this, "請開啟定位服務", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));    //開啟設定頁面
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (countyPuzzleCount <= 4) {
                        puzzleQuestionImage.setImageDrawable(getAssetImage(PuzzleActivity.this, "puzzleQuestion" + county + "2"));
                    } else if (countyPuzzleCount <= 8) {
                        puzzleQuestionImage.setImageDrawable(getAssetImage(PuzzleActivity.this, "puzzleQuestion" + county + "3"));
                    } else if (countyPuzzleCount <= 12) {
                        puzzleQuestionImage.setImageDrawable(getAssetImage(PuzzleActivity.this, "puzzleQuestion" + county + "4"));
                    } else if (countyPuzzleCount <= 16) {
                        puzzleQuestionImage.setImageDrawable(getAssetImage(PuzzleActivity.this, "puzzleQuestion" + county + "5"));
                    } else if (countyPuzzleCount <= 20) {
                        puzzleQuestionImage.setImageDrawable(getAssetImage(PuzzleActivity.this, "puzzleQuestion" + county + "6"));
                    }
                    getLeftTop.setImageDrawable(null);
                    getLelfButtom.setImageDrawable(null);
                    getRightTop.setImageDrawable(null);
                    getRightButtom.setImageDrawable(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                nextButton.setVisibility(View.INVISIBLE);
            }
        });
        allPuzzle();
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            county = drawer_menu[position];

            new Thread(new Runnable() {
                @Override
                public void run() {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String url = "http://140.131.114.161:8080/Formosa/rest/puzzle/getPuzzleByUserAndCounty";

                            try {
                                HttpClient httpclient = new DefaultHttpClient();
                                HttpPost httpRequst = new HttpPost(url);

                                JSONObject parameter = new JSONObject();

                                parameter.accumulate("userID", idString);
                                parameter.accumulate("county", county);

                                String json = parameter.toString();

                                StringEntity se = new StringEntity(json, "UTF-8");
                                httpRequst.setEntity(se);
                                httpRequst.addHeader("Content-Type", "application/json");

                                HttpResponse responsePOST = httpclient.execute(httpRequst);

                                HttpEntity resEntity = responsePOST.getEntity();
                                String result = EntityUtils.toString(resEntity, "UTF-8");

                                JSONObject tmpJson = new JSONObject(result);
                                String tmp = tmpJson.get("statuscode").toString();

                                if (tmp.equals("0")) {

                                    JSONArray puzzleJSON = new JSONArray(tmpJson.get("Puzzles").toString());
                                    JSONArray puzzlesJSON = new JSONArray();

                                    int count = puzzleJSON.length();
                                    for (int i = 0; i < puzzleJSON.length(); i++) {
                                        JSONObject onePuzzleJSON = new JSONObject(puzzleJSON.get(i).toString());

                                        puzzlesJSON.put(onePuzzleJSON);
                                    }

                                    countyPuzzleCount = count;

                                    if (count < 4) {
                                        puzzleQuestionImage.setImageDrawable(getAssetImage(PuzzleActivity.this, "puzzleQuestion" + county + "1"));
                                        level = "1";
                                        getImageGet(county, level, puzzlesJSON);
                                    } else if (count < 8) {
                                        puzzleQuestionImage.setImageDrawable(getAssetImage(PuzzleActivity.this, "puzzleQuestion" + county + "2"));
                                        level = "2";
                                        getImageGet(county, level, puzzlesJSON);
                                    } else if (count < 12) {
                                        puzzleQuestionImage.setImageDrawable(getAssetImage(PuzzleActivity.this, "puzzleQuestion" + county + "3"));
                                        level = "3";
                                        getImageGet(county, level, puzzlesJSON);
                                    } else if (count < 16) {
                                        puzzleQuestionImage.setImageDrawable(getAssetImage(PuzzleActivity.this, "puzzleQuestion" + county + "4"));
                                        level = "4";
                                        getImageGet(county, level, puzzlesJSON);
                                    } else if (count < 20) {
                                        puzzleQuestionImage.setImageDrawable(getAssetImage(PuzzleActivity.this, "puzzleQuestion" + county + "5"));
                                        level = "5";
                                        getImageGet(county, level, puzzlesJSON);
                                    } else if (count < 24) {
                                        puzzleQuestionImage.setImageDrawable(getAssetImage(PuzzleActivity.this, "puzzleQuestion" + county + "6"));
                                        level = "6";
                                        getImageGet(county, level, puzzlesJSON);
                                    }
                                } else if (tmp.equals("40")) {
                                    countyPuzzleCount = 0;
                                    level = "1";
                                    puzzleQuestionImage.setImageDrawable(getAssetImage(PuzzleActivity.this, "puzzleQuestion" + county + "1"));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }).start();

            puzzleQuestionLayout.setVisibility(View.VISIBLE);
            mDrawerLayout.closeDrawer(drawer_view);
        }
    }

    private void allPuzzle() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String url = "http://140.131.114.161:8080/Formosa/rest/puzzle/getPuzzleByUser";

                        try {
                            HttpClient httpclient = new DefaultHttpClient();
                            HttpPost httpRequst = new HttpPost(url);

                            JSONObject parameter = new JSONObject();

                            parameter.accumulate("userID", idString);

                            String json = parameter.toString();

                            StringEntity se = new StringEntity(json);
                            httpRequst.setEntity(se);
                            httpRequst.addHeader("Content-Type", "application/json");

                            HttpResponse responsePOST = httpclient.execute(httpRequst);

                            HttpEntity resEntity = responsePOST.getEntity();
                            String result = EntityUtils.toString(resEntity, "UTF-8");

                            JSONObject tmpJson = new JSONObject(result);
                            String tmp = tmpJson.get("statuscode").toString();

                            if (tmp.equals("0")) {
                                JSONArray puzzlesJSON = new JSONArray(tmpJson.get("Puzzles").toString());
                                int count[] = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                                int countyIdStr[] = new int[]{R.id.tw1ImageView, R.id.tw2ImageView, R.id.tw3ImageView, R.id.tw4ImageView, R.id.tw5ImageView, R.id.tw6ImageView,
                                        R.id.tw7ImageView, R.id.tw8ImageView, R.id.tw9ImageView, R.id.tw10ImageView, R.id.tw11ImageView, R.id.tw12ImageView,
                                        R.id.tw13ImageView, R.id.tw14ImageView, R.id.tw15ImageView, R.id.tw16ImageView, R.id.tw17ImageView, R.id.tw18ImageView,
                                        R.id.tw19ImageView, R.id.tw20ImageView, R.id.tw21ImageView, R.id.tw22ImageView};
                                String[] countyStr = new String[]{"基隆市", "台北市", "新北市", "桃園市", "新竹縣", "新竹市", "苗栗縣", "台中市", "彰化縣", "南投縣", "雲林縣",
                                        "嘉義縣", "嘉義市", "台南市", "高雄市", "屏東縣", "宜蘭縣", "花蓮縣", "台東縣", "澎湖縣", "金門縣", "連江縣"};
                                for (int i = 0; i < puzzlesJSON.length(); i++) {
                                    JSONObject puzzleJSON = new JSONObject(puzzlesJSON.get(i).toString());
                                    String county = puzzleJSON.get("county").toString();
                                    if (county.equals("基隆市")) {
                                        count[0] += 1;
                                    } else if (county.equals("台北市")) {
                                        count[1] += 1;
                                    } else if (county.equals("新北市")) {
                                        count[2] += 1;
                                    } else if (county.equals("桃園市")) {
                                        count[3] += 1;
                                    } else if (county.equals("新竹縣")) {
                                        count[4] += 1;
                                    } else if (county.equals("新竹市")) {
                                        count[5] += 1;
                                    } else if (county.equals("苗栗縣")) {
                                        count[6] += 1;
                                    } else if (county.equals("台中市")) {
                                        count[7] += 1;
                                    } else if (county.equals("彰化縣")) {
                                        count[8] += 1;
                                    } else if (county.equals("南投縣")) {
                                        count[9] += 1;
                                    } else if (county.equals("雲林縣")) {
                                        count[10] += 1;
                                    } else if (county.equals("嘉義縣")) {
                                        count[11] += 1;
                                    } else if (county.equals("嘉義市")) {
                                        count[12] += 1;
                                    } else if (county.equals("台南市")) {
                                        count[13] += 1;
                                    } else if (county.equals("高雄市")) {
                                        count[14] += 1;
                                    } else if (county.equals("屏東縣")) {
                                        count[15] += 1;
                                    } else if (county.equals("宜蘭縣")) {
                                        count[16] += 1;
                                    } else if (county.equals("花蓮縣")) {
                                        count[17] += 1;
                                    } else if (county.equals("台東縣")) {
                                        count[18] += 1;
                                    } else if (county.equals("澎湖縣")) {
                                        count[19] += 1;
                                    } else if (county.equals("金門縣")) {
                                        count[20] += 1;
                                    } else if (county.equals("連江縣")) {
                                        count[21] += 1;
                                    }
                                }
                                for (int i = 0; i < count.length; i++) {
                                    if (count[i] != 0) {
                                        if (count[i] < 4) {
                                            ImageView image = (ImageView) findViewById(countyIdStr[i]);
                                            image.setImageDrawable(getAssetImage(PuzzleActivity.this, "tw" + countyStr[i] + "1"));
                                            image.setVisibility(View.VISIBLE);
                                        } else if (count[i] < 8) {
                                            ImageView image = (ImageView) findViewById(countyIdStr[i]);
                                            image.setImageDrawable(getAssetImage(PuzzleActivity.this, "tw" + countyStr[i] + "2"));
                                            image.setVisibility(View.VISIBLE);
                                        } else if (count[i] < 12) {
                                            ImageView image = (ImageView) findViewById(countyIdStr[i]);
                                            image.setImageDrawable(getAssetImage(PuzzleActivity.this, "tw" + countyStr[i] + "3"));
                                            image.setVisibility(View.VISIBLE);
                                        } else if (count[i] < 16) {
                                            ImageView image = (ImageView) findViewById(countyIdStr[i]);
                                            image.setImageDrawable(getAssetImage(PuzzleActivity.this, "tw" + countyStr[i] + "4"));
                                            image.setVisibility(View.VISIBLE);
                                        } else if (count[i] < 20) {
                                            ImageView image = (ImageView) findViewById(countyIdStr[i]);
                                            image.setImageDrawable(getAssetImage(PuzzleActivity.this, "tw" + countyStr[i] + "5"));
                                            image.setVisibility(View.VISIBLE);
                                        } else if (count[i] < 24) {
                                            ImageView image = (ImageView) findViewById(countyIdStr[i]);
                                            image.setImageDrawable(getAssetImage(PuzzleActivity.this, "tw" + countyStr[i] + "6"));
                                            image.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

    private void getPuzzleByCounty(String county, String level, int count) {
        Location mLocation = getLocation(PuzzleActivity.this);
        if (mLocation != null) {
            double latitude = mLocation.getLatitude();
            double longitude = mLocation.getLongitude();
            String url = "http://maps.google.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude + "&language=zh-TW&sensor=true";

            try {
                HttpClient httpclient = new DefaultHttpClient(); // 宣告一個HttpClient
                HttpResponse httpResponse = httpclient.execute(new HttpGet(url)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
                InputStream inputStream = httpResponse.getEntity().getContent(); // 用InputStream存放取回的內容
                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
                String responseString = readAll(rd); // 宣告一個字串放置讀取BufferedReader後的結果

                JSONObject respJSON = new JSONObject(responseString);

                if (respJSON.length() > 0) {
                    for (int i = 0; i < respJSON.length(); i++) {
                        if (respJSON.has("results")) {
                            JSONArray resultJSON = new JSONArray(respJSON.get("results").toString());
                            JSONObject addressJSON = new JSONObject(resultJSON.get(0).toString());
                            if (addressJSON.has("address_components")) {
                                JSONArray componentsJSON = new JSONArray(addressJSON.get("address_components").toString());
                                for (int j = 0; j < componentsJSON.length(); j++) {
                                    JSONObject typeJSON = new JSONObject(componentsJSON.get(j).toString());
                                    if (typeJSON.has("types")) {
                                        JSONArray countyJSON = new JSONArray(typeJSON.get("types").toString());
                                        for (int k = 0; k < countyJSON.length(); k++) {
                                            if (countyJSON.get(0).toString().equals("administrative_area_level_1")) {
                                                String userCounty = typeJSON.get("long_name").toString();
                                                if (userCounty.equals(county)) {
                                                    if (userCounty.equals("基隆市")) {
                                                        getPuzzle(county, level, latitude, longitude, count, tw1ImageView);
                                                        tw1ImageView.setVisibility(View.VISIBLE);
                                                    } else if (userCounty.equals("台北市")) {
                                                        getPuzzle(county, level, latitude, longitude, count, tw2ImageView);
                                                        tw2ImageView.setVisibility(View.VISIBLE);
                                                    } else if (userCounty.equals("新北市")) {
                                                        getPuzzle(county, level, latitude, longitude, count, tw3ImageView);
                                                        tw3ImageView.setVisibility(View.VISIBLE);
                                                    } else if (userCounty.equals("桃園市")) {
                                                        getPuzzle(county, level, latitude, longitude, count, tw4ImageView);
                                                        tw4ImageView.setVisibility(View.VISIBLE);
                                                    } else if (userCounty.equals("新竹縣")) {
                                                        getPuzzle(county, level, latitude, longitude, count, tw5ImageView);
                                                        tw5ImageView.setVisibility(View.VISIBLE);
                                                    } else if (userCounty.equals("新竹市")) {
                                                        getPuzzle(county, level, latitude, longitude, count, tw6ImageView);
                                                        tw6ImageView.setVisibility(View.VISIBLE);
                                                    } else if (userCounty.equals("苗栗縣")) {
                                                        getPuzzle(county, level, latitude, longitude, count, tw7ImageView);
                                                        tw7ImageView.setVisibility(View.VISIBLE);
                                                    } else if (userCounty.equals("台中市")) {
                                                        getPuzzle(county, level, latitude, longitude, count, tw8ImageView);
                                                        tw8ImageView.setVisibility(View.VISIBLE);
                                                    } else if (userCounty.equals("彰化縣")) {
                                                        getPuzzle(county, level, latitude, longitude, count, tw9ImageView);
                                                        tw9ImageView.setVisibility(View.VISIBLE);
                                                    } else if (userCounty.equals("南投縣")) {
                                                        getPuzzle(county, level, latitude, longitude, count, tw10ImageView);
                                                        tw10ImageView.setVisibility(View.VISIBLE);
                                                    } else if (userCounty.equals("雲林縣")) {
                                                        getPuzzle(county, level, latitude, longitude, count, tw11ImageView);
                                                        tw11ImageView.setVisibility(View.VISIBLE);
                                                    } else if (userCounty.equals("嘉義縣")) {
                                                        getPuzzle(county, level, latitude, longitude, count, tw12ImageView);
                                                        tw12ImageView.setVisibility(View.VISIBLE);
                                                    } else if (userCounty.equals("嘉義市")) {
                                                        getPuzzle(county, level, latitude, longitude, count, tw13ImageView);
                                                        tw13ImageView.setVisibility(View.VISIBLE);
                                                    } else if (userCounty.equals("台南市")) {
                                                        getPuzzle(county, level, latitude, longitude, count, tw14ImageView);
                                                        tw14ImageView.setVisibility(View.VISIBLE);
                                                    } else if (userCounty.equals("高雄市")) {
                                                        getPuzzle(county, level, latitude, longitude, count, tw15ImageView);
                                                        tw15ImageView.setVisibility(View.VISIBLE);
                                                    } else if (userCounty.equals("屏東縣")) {
                                                        getPuzzle(county, level, latitude, longitude, count, tw16ImageView);
                                                        tw16ImageView.setVisibility(View.VISIBLE);
                                                    } else if (userCounty.equals("宜蘭縣")) {
                                                        getPuzzle(county, level, latitude, longitude, count, tw17ImageView);
                                                        tw17ImageView.setVisibility(View.VISIBLE);
                                                    } else if (userCounty.equals("花蓮縣")) {
                                                        getPuzzle(county, level, latitude, longitude, count, tw18ImageView);
                                                        tw18ImageView.setVisibility(View.VISIBLE);
                                                    } else if (userCounty.equals("台東縣")) {
                                                        getPuzzle(county, level, latitude, longitude, count, tw19ImageView);
                                                        tw19ImageView.setVisibility(View.VISIBLE);
                                                    } else if (userCounty.equals("澎湖縣")) {
                                                        getPuzzle(county, level, latitude, longitude, count, tw20ImageView);
                                                        tw20ImageView.setVisibility(View.VISIBLE);
                                                    } else if (userCounty.equals("金門縣")) {
                                                        getPuzzle(county, level, latitude, longitude, count, tw21ImageView);
                                                        tw21ImageView.setVisibility(View.VISIBLE);
                                                    } else if (userCounty.equals("連江縣")) {
                                                        getPuzzle(county, level, latitude, longitude, count, tw22ImageView);
                                                        tw22ImageView.setVisibility(View.VISIBLE);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
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
    }

    private void getPuzzle(final String county, final String level, final Double latitude, final Double longitude, final int count, final ImageView imageView) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String url = "http://140.131.114.161:8080/Formosa/rest/puzzleQuestion/getPuzzleQuestionByCountyAndLevel";

                        try {
                            HttpClient httpclient = new DefaultHttpClient();
                            HttpPost httpRequst = new HttpPost(url);

                            JSONObject parameter = new JSONObject();

                            parameter.accumulate("county", county);
                            parameter.accumulate("level", level);

                            String json = parameter.toString();

                            StringEntity se = new StringEntity(json, "UTF-8");
                            httpRequst.setEntity(se);
                            httpRequst.addHeader("Content-Type", "application/json");

                            HttpResponse responsePOST = httpclient.execute(httpRequst);

                            HttpEntity resEntity = responsePOST.getEntity();
                            String result = EntityUtils.toString(resEntity, "UTF-8");

                            JSONObject tmpJson = new JSONObject(result);

                            String tmp = tmpJson.get("statuscode").toString();

                            if (tmp.equals("0")) {
                                JSONArray puzzleQuestions = new JSONArray(tmpJson.get("PuzzleQuestions").toString());

                                for (int i = 0; i < puzzleQuestions.length(); i++) {
                                    JSONObject puzzleJSON = new JSONObject(puzzleQuestions.get(i).toString());
                                    double lon = Double.parseDouble(puzzleJSON.get("attractionLongitude").toString());
                                    double lat = Double.parseDouble(puzzleJSON.get("attractionLatitude").toString());

                                    double distance = getDistance(latitude, longitude, lat, lon);

                                    if (distance < 1000) {

                                        String getUrl = "http://140.131.114.161:8080/Formosa/rest/puzzle/addPuzzle";

                                        HttpClient getHttpclient = new DefaultHttpClient();
                                        HttpPost getHttpRequst = new HttpPost(getUrl);

                                        JSONObject getParameter = new JSONObject();

                                        getParameter.accumulate("userID", idString);
                                        getParameter.accumulate("puzzleQuestionID", puzzleJSON.get("puzzleQuestionID").toString());
                                        getParameter.accumulate("puzzleGetAttractionName", puzzleJSON.get("puzzleGetAttractionName").toString());
                                        getParameter.accumulate("county", county);
                                        getParameter.accumulate("level", level);

                                        String getJson = getParameter.toString();

                                        StringEntity getSE = new StringEntity(getJson, "UTF-8");
                                        getHttpRequst.setEntity(getSE);
                                        getHttpRequst.addHeader("Content-Type", "application/json");

                                        HttpResponse getResponsePOST = getHttpclient.execute(getHttpRequst);

                                        HttpEntity getResEntity = getResponsePOST.getEntity();
                                        String getResult = EntityUtils.toString(getResEntity, "UTF-8");

                                        JSONObject resultJson = new JSONObject(getResult);

                                        String statuscode = resultJson.get("statuscode").toString();

                                        if (statuscode.equals("0")) {
                                            if (puzzleJSON.get("image").toString().equals("左上")) {
                                                getLeftTop.setImageDrawable(getAssetImage(PuzzleActivity.this, "Get"));
                                            } else if (puzzleJSON.get("image").toString().equals("左下")) {
                                                getLelfButtom.setImageDrawable(getAssetImage(PuzzleActivity.this, "Get"));
                                            } else if (puzzleJSON.get("image").toString().equals("右下")) {
                                                getRightButtom.setImageDrawable(getAssetImage(PuzzleActivity.this, "Get"));
                                            } else if (puzzleJSON.get("image").toString().equals("右上")) {
                                                getRightTop.setImageDrawable(getAssetImage(PuzzleActivity.this, "Get"));
                                            }
                                            if (count+1 == 1){
                                                imageView.setImageDrawable(getAssetImage(PuzzleActivity.this, "tw" + county + "1"));
                                            }
                                            if (count + 1 == 4) {
                                                nextButton.setVisibility(View.VISIBLE);
                                                imageView.setImageDrawable(getAssetImage(PuzzleActivity.this, "tw" + county + (Integer.parseInt(level) + 1)));
                                            } else if (count + 1 == 8) {
                                                nextButton.setVisibility(View.VISIBLE);
                                                imageView.setImageDrawable(getAssetImage(PuzzleActivity.this, "tw" + county + (Integer.parseInt(level) + 1)));
                                            } else if (count + 1 == 12) {
                                                nextButton.setVisibility(View.VISIBLE);
                                                imageView.setImageDrawable(getAssetImage(PuzzleActivity.this, "tw" + county + (Integer.parseInt(level) + 1)));
                                            } else if (count + 1 == 16) {
                                                nextButton.setVisibility(View.VISIBLE);
                                                imageView.setImageDrawable(getAssetImage(PuzzleActivity.this, "tw" + county + (Integer.parseInt(level) + 1)));
                                            } else if (count + 1 == 20) {
                                                nextButton.setVisibility(View.VISIBLE);
                                                imageView.setImageDrawable(getAssetImage(PuzzleActivity.this, "tw" + county + (Integer.parseInt(level) + 1)));
                                            } else if (count + 1 == 24) {
                                                nextButton.setVisibility(View.VISIBLE);
                                                imageView.setImageDrawable(getAssetImage(PuzzleActivity.this, "tw" + county + (Integer.parseInt(level) + 1)));
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

    public double getDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

    private void getImageGet(final String county, final String level, final JSONArray puzzlesJSON) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String url = "http://140.131.114.161:8080/Formosa/rest/puzzleQuestion/getPuzzleQuestionByCountyAndLevel";

                        try {
                            HttpClient httpclient = new DefaultHttpClient();
                            HttpPost httpRequst = new HttpPost(url);

                            JSONObject parameter = new JSONObject();

                            parameter.accumulate("county", county);
                            parameter.accumulate("level", level);

                            String json = parameter.toString();

                            StringEntity se = new StringEntity(json, "UTF-8");
                            httpRequst.setEntity(se);
                            httpRequst.addHeader("Content-Type", "application/json");

                            HttpResponse responsePOST = httpclient.execute(httpRequst);

                            HttpEntity resEntity = responsePOST.getEntity();
                            String result = EntityUtils.toString(resEntity, "UTF-8");

                            JSONObject tmpJson = new JSONObject(result);

                            String tmp = tmpJson.get("statuscode").toString();

                            if (tmp.equals("0")) {

                                for (int i = 0; i < puzzlesJSON.length(); i++) {
                                    JSONObject puzzleJSON = new JSONObject(puzzlesJSON.get(i).toString());
                                    String attraction = puzzleJSON.get("puzzleGetAttractionName").toString();
                                    String attractionLevel = puzzleJSON.get("level").toString();

                                    JSONArray puzzleQuestionsJSON = new JSONArray(tmpJson.get("PuzzleQuestions").toString());

                                    for (int j = 0; j < puzzleQuestionsJSON.length(); j++) {
                                        JSONObject puzzleQuestionJSON = new JSONObject(puzzleQuestionsJSON.get(j).toString());

                                        if (puzzleQuestionJSON.get("puzzleGetAttractionName").toString().equals(attraction) && level.equals(attractionLevel)) {
                                            if (puzzleQuestionJSON.get("image").toString().equals("左上")) {
                                                getLeftTop.setImageDrawable(getAssetImage(PuzzleActivity.this, "Get"));
                                            } else if (puzzleQuestionJSON.get("image").toString().equals("左下")) {
                                                getLelfButtom.setImageDrawable(getAssetImage(PuzzleActivity.this, "Get"));
                                            } else if (puzzleQuestionJSON.get("image").toString().equals("右下")) {
                                                getRightButtom.setImageDrawable(getAssetImage(PuzzleActivity.this, "Get"));
                                            } else if (puzzleQuestionJSON.get("image").toString().equals("右上")) {
                                                getRightTop.setImageDrawable(getAssetImage(PuzzleActivity.this, "Get"));
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

    public Location getLocation(Context context) {
        LocationManager locMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
        try {
            location = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            return location;
        } catch (SecurityException e) {
            e.printStackTrace();
            return location;
        }
    }

    public static Drawable getAssetImage(Context context, String filename) throws IOException {
        AssetManager assets = context.getResources().getAssets();
        InputStream buffer = new BufferedInputStream((assets.open("drawable/" + filename + ".png")));
        Bitmap bitmap = BitmapFactory.decodeStream(buffer);
        return new BitmapDrawable(context.getResources(), bitmap);
    }

    public void ReadValue() {
        SharedPreferences setting = getSharedPreferences("LoginInfo", 0);
        idString = setting.getString("Id", "");
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {

            sb.append((char) cp);
        }
        return sb.toString();
    }
}
