package tw.edu.ntubimd.formosa.drawer.pair;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;

import tw.edu.ntubimd.formosa.R;

public class AllPairDetailActivity extends AppCompatActivity {

    private TextView textViewUserPairAddress, textViewUserShopName, textViewUserProductName, textViewUserProductPrice, textViewUserPreferentialType, textViewUserUserFeature, textViewShowPairTime, textViewUserWaitTime;
    private Button buttonPair;
    private String pairID, userID, longitude, latitude;
    private boolean isGPSEnabled, isNetworkEnabled;
    Location location = null;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    //The minimum time beetwen updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 0; //1000 * 60 * 1; // 1 minute
    private String idString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_pair_detail);

        File file = new File(getApplication().getFilesDir().getParentFile().getPath() + "/shared_prefs/", "LoginInfo.xml");
        if (file.exists()) {
            ReadValue();
        }

        //region Toolbar設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("買一送一");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //取消AppTitle
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //endregion

        textViewUserPairAddress = (TextView) findViewById(R.id.textViewUserPairAddress);
        textViewUserShopName = (TextView) findViewById(R.id.textViewUserShopName);
        textViewUserProductName = (TextView) findViewById(R.id.textViewUserProductName);
        textViewUserProductPrice = (TextView) findViewById(R.id.textViewUserProductPrice);
        textViewUserPreferentialType = (TextView) findViewById(R.id.textViewUserPreferentialType);
        textViewUserUserFeature = (TextView) findViewById(R.id.textViewUserUserFeature);
        textViewShowPairTime = (TextView) findViewById(R.id.textViewShowPairTime);
        textViewUserWaitTime = (TextView) findViewById(R.id.textViewUserWaitTime);

        buttonPair = (Button) findViewById(R.id.buttonPair);

        Intent intent = getIntent();
        pairID = intent.getStringExtra("pairID");
        userID = intent.getStringExtra("userID");
        longitude = intent.getStringExtra("longitude");
        latitude = intent.getStringExtra("latitude");

        System.out.println(pairID);
        System.out.println(userID);
        System.out.println(longitude);
        System.out.println(latitude);

        getPairInfo();

        buttonPair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(AllPairDetailActivity.this);
                dialog.setMessage("你確定要進行這個Pair嗎？");
                dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        getLocation();
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dialog.show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //返回鍵
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getPairInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String url = "http://140.131.114.161:8080/Formosa/rest/pair/getPairByPairId";
                        try {
                            HttpClient httpclient = new DefaultHttpClient();
                            HttpPost httpRequst = new HttpPost(url);
                            JSONObject parameter = new JSONObject();

                            parameter.accumulate("pairID", pairID);
                            String json = parameter.toString();
                            StringEntity se = new StringEntity(json);
                            httpRequst.setEntity(se);
                            httpRequst.addHeader("Content-Type", "application/json");
                            HttpResponse responsePOST = httpclient.execute(httpRequst);
                            HttpEntity resEntity = responsePOST.getEntity();
                            String result = EntityUtils.toString(resEntity, "UTF-8");

                            System.out.println(result);

                            JSONObject resultJson = new JSONObject(result);

                            String tmp = resultJson.get("statuscode").toString();

                            System.out.printf(tmp);

                            if (tmp.equals("0")) {
                                if (resultJson.length() > 0) {
                                    JSONObject pairJSON = new JSONObject(resultJson.get("Pair").toString());
                                    if (pairJSON.has("pairID")) {
                                        textViewUserPairAddress.setText(pairJSON.get("pairAddress").toString());
                                        textViewUserShopName.setText(pairJSON.get("shopName").toString());
                                        textViewUserProductName.setText(pairJSON.get("productName").toString());
                                        textViewUserProductPrice.setText(pairJSON.get("productPrice").toString() + "元");
                                        textViewUserPreferentialType.setText(pairJSON.get("preferentialType").toString());
                                        textViewUserUserFeature.setText(pairJSON.get("userFeature").toString());
                                        int doc = pairJSON.get("pairTime").toString().indexOf(" ");
                                        String pairTime = pairJSON.get("pairTime").toString().substring(0, doc);
                                        textViewShowPairTime.setText(pairTime);
                                        String waitTime = "";
                                        if (pairJSON.get("waitTime").toString().equals("00:30:00")) {
                                            waitTime = "30分鐘";
                                        } else if (pairJSON.get("waitTime").toString().equals("01:00:00")) {
                                            waitTime = "60分鐘";
                                        } else if (pairJSON.get("waitTime").toString().equals("01:30:00")) {
                                            waitTime = "90分鐘";
                                        }
                                        textViewUserWaitTime.setText(waitTime);
                                    }
                                }
                            } else {
                                Toast toast = Toast.makeText(AllPairDetailActivity.this, "發生失敗請重試", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

    public void getLocation() {
        LocationManager status = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        isGPSEnabled = status.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = status.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!(isGPSEnabled || isNetworkEnabled)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(AllPairDetailActivity.this);
            dialog.setMessage("請開啟定位服務以獲取目前位置");
            dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            dialog.show();
        } else {
            try {
                if (isNetworkEnabled) {
                    System.out.println("isNetworkEnabled");
                    status.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, mLocationListener);
                    if (status.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
                        location = status.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        System.out.println("net=" + location);
                    }

                }
                if (isGPSEnabled) {
                    System.out.println("isGPSEnabled");
                    status.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, mLocationListener);
                    if (status.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
                        location = status.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        System.out.println("gps=" + location);
                    }
                }
                if (location != null) {
                    addPairTracing();
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public void addPairTracing() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                String url = "http://140.131.114.161:8080/Formosa/rest/pairTracing/addPairTracing";
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httpRequst = new HttpPost(url);
                    JSONObject parameter = new JSONObject();

                    parameter.accumulate("pairID", pairID);
                    parameter.accumulate("userID", userID);
                    parameter.accumulate("pairLongitude", longitude);
                    parameter.accumulate("pairLatitude", latitude);
                    parameter.accumulate("tracingUserID", idString);
                    parameter.accumulate("tracingLongitude", Double.toString(location.getLongitude()));
                    parameter.accumulate("tracingLatitude", Double.toString(location.getLatitude()));

                    String json = parameter.toString();
                    System.out.println(json);
                    StringEntity se = new StringEntity(json);
                    httpRequst.setEntity(se);
                    httpRequst.addHeader("Content-Type", "application/json");
                    HttpResponse responsePOST = httpclient.execute(httpRequst);
                    HttpEntity resEntity = responsePOST.getEntity();
                    String result = EntityUtils.toString(resEntity, "UTF-8");

                    System.out.println(result);

                    JSONObject resultJson = new JSONObject(result);

                    final String tmp = resultJson.get("statuscode").toString();

                    System.out.printf(tmp);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (tmp.equals("0")) {
                                paired();
                            } else {
                                Toast toast = Toast.makeText(AllPairDetailActivity.this, "發生失敗請重試", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void paired() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                String url = "http://140.131.114.161:8080/Formosa/rest/pair/alreadyPair";
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httpRequst = new HttpPost(url);
                    JSONObject parameter = new JSONObject();

                    parameter.accumulate("pairID", pairID);
                    parameter.accumulate("paired", "true");

                    String json = parameter.toString();
                    System.out.println(json);
                    StringEntity se = new StringEntity(json);
                    httpRequst.setEntity(se);
                    httpRequst.addHeader("Content-Type", "application/json");
                    HttpResponse responsePOST = httpclient.execute(httpRequst);
                    HttpEntity resEntity = responsePOST.getEntity();
                    String result = EntityUtils.toString(resEntity, "UTF-8");

                    System.out.println(result);

                    JSONObject resultJson = new JSONObject(result);

                    final String tmp = resultJson.get("statuscode").toString();

                    System.out.printf(tmp);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (tmp.equals("0")) {
                                Toast toast = Toast.makeText(AllPairDetailActivity.this, "成功", Toast.LENGTH_SHORT);
                                toast.show();

                                finish();

                            } else {
                                Toast toast = Toast.makeText(AllPairDetailActivity.this, "發生失敗請重試", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };

    public void ReadValue() {
        SharedPreferences setting = getSharedPreferences("LoginInfo", 0);
        idString = setting.getString("Id", "");
    }
}
