package tw.edu.ntubimd.formosa.drawer.pair;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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

public class NowPairDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapFragment mapFragment;
    private GoogleMap mMap;
    private TextView textViewUserPairAddress, textViewUserShopName, textViewUserProductName, textViewUserProductPrice, textViewUserPreferentialType, textViewUserUserFeature, textViewShowPairTime, textViewUserWaitTime;
    private String pairID, idString;
    private boolean isGPSEnabled, isNetworkEnabled;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    //The minimum time beetwen updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 0; //1000 * 60 * 1; // 1 minute
    Location location = null;
    LatLng userLocation, tracingUserLocation;
    private Marker userMarker, traceUserMarker;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_pair_detail);

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


        Intent intent = getIntent();
        pairID = intent.getStringExtra("pairID");

        getLocation();

        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        showPairInfo();

        //設定Delay的時間
        handler.postDelayed(uptadeTraceUserMarket, 0);
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
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        userMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pair_marker_my))
                .title("User"));

        getPairTracingAndUpdate();

        traceUserMarker = mMap.addMarker(new MarkerOptions()
                .position(tracingUserLocation)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pair_marker_another))
                .title("TraceUser"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18));
    }

    private void getLocation() {
        LocationManager status = (LocationManager) getSystemService(LOCATION_SERVICE);
        isGPSEnabled = status.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = status.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!(isGPSEnabled || isNetworkEnabled)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(NowPairDetailActivity.this);
            dialog.setMessage("請開啟定位服務以獲取目前位置");
            dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
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
                getPairTracingAndUpdate();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public void getPairTracingAndUpdate() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                String url = "http://140.131.114.161:8080/Formosa/rest/pairTracing/getPairTracingById";
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
                        if (resultJson.has("PairTracing")) {
                            JSONObject pairTracingJSON = new JSONObject(resultJson.get("PairTracing").toString());
                            if (pairTracingJSON.has("pairID")) {

                                if (pairTracingJSON.get("userID").equals(idString)) {

                                    tracingUserLocation = new LatLng(Double.parseDouble(pairTracingJSON.get("tracingLatitude").toString()), Double.parseDouble(pairTracingJSON.get("tracingLongitude").toString()));
                                    System.out.println("tracingUserLocation"+tracingUserLocation);

                                    String updateUrl = "http://140.131.114.161:8080/Formosa/rest/pairTracing/updatePairTracingByUserID";
                                    HttpClient updateHttpclient = new DefaultHttpClient();
                                    HttpPost updateHttpRequst = new HttpPost(updateUrl);
                                    JSONObject updateParameter = new JSONObject();

                                    updateParameter.accumulate("pairID", pairID);
                                    updateParameter.accumulate("userID", idString);
                                    updateParameter.accumulate("pairLongitude", Double.toString(location.getLongitude()));
                                    updateParameter.accumulate("pairLatitude", Double.toString(location.getLatitude()));

                                    String updateJson = updateParameter.toString();
                                    StringEntity updateSe = new StringEntity(updateJson);
                                    updateHttpRequst.setEntity(updateSe);
                                    updateHttpRequst.addHeader("Content-Type", "application/json");
                                    HttpResponse updateResponsePOST = updateHttpclient.execute(updateHttpRequst);
                                    HttpEntity updateResEntity = updateResponsePOST.getEntity();
                                    String updateResult = EntityUtils.toString(updateResEntity, "UTF-8");

                                    JSONObject updateResultJson = new JSONObject(updateResult);

                                    String updateTmp = updateResultJson.get("statuscode").toString();

                                    System.out.printf(updateTmp);

                                    if (!updateTmp.equals("0")) {
                                        Toast toast = Toast.makeText(NowPairDetailActivity.this, "發生錯誤請重試", Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                } else {
                                    if (pairTracingJSON.get("tracingUserID").equals(idString)) {

                                        tracingUserLocation = new LatLng(Double.parseDouble(pairTracingJSON.get("pairLatitude").toString()), Double.parseDouble(pairTracingJSON.get("pairLongitude").toString()));
                                        System.out.println("tracingUserLocation"+tracingUserLocation);

                                        String updateUrl = "http://140.131.114.161:8080/Formosa/rest/pairTracing/updatePairTracingByTracingUserID";
                                        HttpClient updateHttpclient = new DefaultHttpClient();
                                        HttpPost updateHttpRequst = new HttpPost(updateUrl);
                                        JSONObject updateParameter = new JSONObject();

                                        updateParameter.accumulate("pairID", pairID);
                                        updateParameter.accumulate("tracingUserID", idString);
                                        updateParameter.accumulate("tracingLongitude", Double.toString(location.getLongitude()));
                                        updateParameter.accumulate("tracingLatitude", Double.toString(location.getLatitude()));

                                        String updateJson = updateParameter.toString();
                                        StringEntity updateSe = new StringEntity(updateJson);
                                        updateHttpRequst.setEntity(updateSe);
                                        updateHttpRequst.addHeader("Content-Type", "application/json");
                                        HttpResponse updateResponsePOST = updateHttpclient.execute(updateHttpRequst);
                                        HttpEntity updateResEntity = updateResponsePOST.getEntity();
                                        String updateResult = EntityUtils.toString(updateResEntity, "UTF-8");

                                        System.out.println("updateResult"+updateResult);

                                        JSONObject updateResultJson = new JSONObject(updateResult);

                                        String updateTmp = updateResultJson.get("statuscode").toString();

                                        System.out.printf(updateTmp);

                                        if (!updateTmp.equals("0")) {
                                            Toast toast = Toast.makeText(NowPairDetailActivity.this, "發生錯誤請重試", Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Toast toast = Toast.makeText(NowPairDetailActivity.this, "發生失敗請重試", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void showPairInfo() {

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
                                Toast toast = Toast.makeText(NowPairDetailActivity.this, "發生失敗請重試", Toast.LENGTH_SHORT);
                                toast.show();
                            }

                            System.out.println(result);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

    public void uptadeUserMarker(Location loc) {
        if (mMap != null) {
            if (userMarker != null) {
                userMarker.remove();
            }
            LatLng gps = new LatLng(loc.getLatitude(), loc.getLongitude());
            userMarker = mMap.addMarker(new MarkerOptions()
                    .position(gps)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pair_marker_my))
                    .title("User"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 18));
        }
    }

    public Runnable uptadeTraceUserMarket = new Runnable() {
        @Override
        public void run() {
            getPairTracingAndUpdate();

            if (traceUserMarker != null) {
                traceUserMarker.remove();
            }

            traceUserMarker = mMap.addMarker(new MarkerOptions()
                    .position(tracingUserLocation)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pair_marker_another))
                    .title("TraceUser"));

            handler.postDelayed(this, 300000);
        }
    };

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            uptadeUserMarker(location);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(uptadeTraceUserMarket);
        }
    }
}
