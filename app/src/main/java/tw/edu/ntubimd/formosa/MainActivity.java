package tw.edu.ntubimd.formosa;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import tw.edu.ntubimd.formosa.drawer.CarpoolPairUserInfoActivity;
import tw.edu.ntubimd.formosa.drawer.CollectionActivity;
import tw.edu.ntubimd.formosa.drawer.LoginActivity;
import tw.edu.ntubimd.formosa.drawer.PuzzleActivity;
import tw.edu.ntubimd.formosa.drawer.ReservationActivity;
import tw.edu.ntubimd.formosa.drawer.pair.PairActivity;
import tw.edu.ntubimd.formosa.drawer.travel.TravelActivity;
import tw.edu.ntubimd.formosa.taiwan.taipei.TaipeiActivity;
import tw.edu.ntubimd.formosa.weather.WeatherService;

public class MainActivity extends AppCompatActivity {

    private TextView textViewLocation, weather, icon;
    private LinearLayout weatherLayout;
    private BroadcastReceiver broadcast_reciever = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageView imageButtonTaiwan = (ImageView) findViewById(R.id.imageButtonTaiwan); //首頁台灣地圖
        ImageButton imageButtonPopular = (ImageButton) findViewById(R.id.imageButtonPopular);
        Button loginButton = (Button) findViewById(R.id.loginButton);
        textViewLocation = (TextView) findViewById(R.id.textViewLocation);
        weather = (TextView) findViewById(R.id.weather);
        icon = (TextView) findViewById(R.id.icon);
        Typeface weatherFont = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/weather.ttf"); //設定天氣字型
        icon.setTypeface(weatherFont);
        weatherLayout = (LinearLayout) findViewById(R.id.weatherLayout);

        File file = new File(getApplication().getFilesDir().getParentFile().getPath() + "/shared_prefs/", "LoginInfo.xml");
        if (file.exists()) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, MemberMainActivity.class);
            startActivity(intent);
            finish();
        }

        checkNetwork(); //判斷使用者網路狀態

        //region 設定Drawer
        ListView drawerList = (ListView) findViewById(R.id.drawerList);
        ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        final String[] drawer_menu = new String[]{"代訂紀錄", "口袋名單", "我的拼圖", "Pair", "我的行程", "我的明信片"};
        final int[] drawer_icon = new int[]{R.drawable.drawer_reservation, R.drawable.drawer_collect, R.drawable.drawer_puzzle, R.drawable.drawer_reservation, R.drawable.drawer_stroke, R.drawable.drawer_postcard};
        for (int i = 0; i < drawer_menu.length; i++) {
            HashMap<String, Object> item = new HashMap<String, Object>();
            item.put("Icon", drawer_icon[i]);
            item.put("Menu", drawer_menu[i]);
            list.add(item);
        }
        SimpleAdapter ListAdapter = new SimpleAdapter(this, list, R.layout.drawerlistview, // 宣告一個SimpleAdapter五個參數分別為(List位置,
                // List資料來源,
                // ListView介面檔,
                // List裡資料的名稱,
                // List裡資料的介面檔中id)
                new String[]{"Icon", "Menu"}, new int[]{R.id.imageViewIcon, R.id.textViewName});
        drawerList.setAdapter(ListAdapter);
        drawerList.setDividerHeight(0);
        drawerList.setOnItemClickListener(new DrawerItemClickListener());
        //endregion

        //region Toolbar設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.formosa);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //取消AppTitle
        toolbar.setOnMenuItemClickListener(onMenuItemClick); //設定Toolbar監聽器
        //endregion

        //region 綁定Toolbar與Drawer
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer); // 實作 drawer toggle 並放入 toolbar
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.syncState(); //將返回建置換成三條槓
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        //endregion

        //region 點擊台灣地圖事件
        imageButtonTaiwan.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int touchX = (int) event.getX();
                int touchY = (int) event.getY();

                imageButtonTaiwan.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(imageButtonTaiwan.getDrawingCache());

                int pixel = bitmap.getPixel(touchX, touchY);

                switch (pixel) {
                    case 0:
                        break;
                    case -16736072:
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, TaipeiActivity.class); //觸發後連到MemberTaipeiActivity.class
                        startActivity(intent);
                        break;
                    case -16737980:
                        break;
                    case -946944:
                        break;
                    case -1703918:
                        break;
                }
                return false;
            }
        });
        //endregion

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               broadcast_reciever = new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context arg0, Intent intent) {
                        String action = intent.getAction();
                        if (action.equals("finish_activity")) {
                            finish();
                        }
                    }
                };
                registerReceiver(broadcast_reciever, new IntentFilter("finish_activity"));

                Intent intent = new Intent();
                intent.setClass(MainActivity.this, LoginActivity.class); //觸發後連到MemberTaipeiActivity.class
                startActivity(intent);
            }
        });

        imageButtonPopular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, PopularActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //createMenu內容
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        return true;
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() { //Toolbar監聽器
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) { //ToolbarMenu點擊
            int id = menuItem.getItemId();

            if (id == R.id.action_search) {
                return true;
            }

            if (id == R.id.action_qrcode) {
                openQrcode();
                return true;
            }

            return true;
        }
    };

    public void openQrcode() {  //QRcode方法
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.google.zxing.client.android");
        if (intent != null) {
            startActivity(intent);
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setMessage("你沒有安裝條碼掃描器\n要前往Google Play商店下載條碼掃描器嗎？");
            dialog.setPositiveButton("前往", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent installIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.zxing.client.android"));
                    startActivity(installIntent);
                }
            });
            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.show();
        }
    }

    public void Postcard(){ //Postcard方法
        Intent intent = getPackageManager().getLaunchIntentForPackage("hd.tintint.l.android");
        if (intent != null) {
            startActivity(intent);
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setMessage("你沒有安裝點點印\n要前往Google Play商店下載點點印嗎？");
            dialog.setPositiveButton("前往", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent installIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=hd.tintint.l.android"));
                    startActivity(installIntent);
                }
            });
            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.show();
        }
    }

    private void checkNetwork() { //確認使用者網路狀態
        ConnectivityManager cm = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            testLocationProvider();
        } else {
            Toast.makeText(this, "請開啟網路獲得旅遊資訊", Toast.LENGTH_LONG).show();
        }
    }

    private void testLocationProvider() { //取得系統定位服務
        LocationManager status = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (status.isProviderEnabled(LocationManager.GPS_PROVIDER) || status.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            getWeather();
            //如果GPS或網路定位開啟，呼叫locationServiceInitial()更新位置
            Intent intent = new Intent(this, WeatherService.class);
            this.startService(intent);
        } else {
            Toast.makeText(this, "請開啟定位服務提供天氣資訊", Toast.LENGTH_LONG).show();
        }
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

    private void getWeather() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Location mLocation = getLocation(MainActivity.this);
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
                                                    if (countyJSON.get(0).toString().equals("administrative_area_level_3")) {
                                                        final String county = typeJSON.get("long_name").toString();
                                                        System.out.println(county);
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                textViewLocation.setText(county);
                                                                weatherLayout.setVisibility(View.VISIBLE);
                                                            }
                                                        });
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        String weatherUrl = "http://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&units=metric&APPID=eeaf4460f92063034691ec3b9fb229d3";

                        HttpClient weatherHttpclient = new DefaultHttpClient(); // 宣告一個HttpClient
                        HttpResponse weatherHttpResponse = weatherHttpclient.execute(new HttpGet(weatherUrl)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
                        InputStream weatherInputStream = weatherHttpResponse.getEntity().getContent(); // 用InputStream存放取回的內容
                        BufferedReader weatherRD = new BufferedReader(new InputStreamReader(weatherInputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
                        String weatherResponseString = readAll(weatherRD); // 宣告一個字串放置讀取BufferedReader後的結果

                        JSONObject weatherRespJSON = new JSONObject(weatherResponseString);
                        System.out.println("weatherRespJSON: " + weatherRespJSON);

                        if (weatherRespJSON.length() > 0) {
                            JSONObject main = new JSONObject(weatherRespJSON.get("main").toString());
                            JSONArray weatherArray = new JSONArray(weatherRespJSON.get("weather").toString());
                            JSONObject weatherJSON = new JSONObject(weatherArray.get(0).toString());
                            final String temp = main.getString("temp");
                            final String weatherIcon = weatherJSON.get("main").toString();
                            System.out.println("temp: " + temp);
                            System.out.println("weatherIcon: " + weatherIcon);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    weather.setText(temp + "℃");

                                    if (weatherIcon.equals("Thunderstorm")) {
                                        String iconstr = getApplicationContext().getString(R.string.weather_thunder);
                                        icon.setText(iconstr);
                                    } else if (weatherIcon.equals("Drizzle")) {
                                        String iconstr = getApplicationContext().getString(R.string.weather_drizzle);
                                        icon.setText(iconstr);
                                    } else if (weatherIcon.equals("Rain")) {
                                        String iconstr = getApplicationContext().getString(R.string.weather_rainy);
                                        icon.setText(iconstr);
                                    } else if (weatherIcon.equals("Snow")) {
                                        String iconstr = getApplicationContext().getString(R.string.weather_snowy);
                                        icon.setText(iconstr);
                                    } else if (weatherIcon.equals("Atmosphere")) {
                                        String iconstr = getApplicationContext().getString(R.string.weather_foggy);
                                        icon.setText(iconstr);
                                    } else if (weatherIcon.equals("Clear")) {
                                        String iconstr = getApplicationContext().getString(R.string.weather_sunny);
                                        icon.setText(iconstr);
                                    } else if (weatherIcon.equals("Clouds")) {
                                        String iconstr = getApplicationContext().getString(R.string.weather_cloudy);
                                        icon.setText(iconstr);
                                    }
                                }
                            });
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
        }).start();
    }

    public void ReadValue() {
        SharedPreferences setting = getSharedPreferences("LoginInfo", 0);
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast toast = Toast.makeText(MainActivity.this, "請登入以使用該功能", Toast.LENGTH_SHORT);
            switch (position) {
                case 0:
                    toast.show();
                    break;
                case 1:
                    toast.show();
                    break;
                case 2:
                    toast.show();
                    break;
                case 3:
                    toast.show();
                    break;
                case 4:
                    toast.show();
                    break;
                case 5:
                    Postcard();
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcast_reciever != null){
            unregisterReceiver(broadcast_reciever);
        }
    }
}