package tw.edu.ntubimd.formosa.taiwan.taipei;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import tw.edu.ntubimd.formosa.R;

public class Information extends Fragment implements OnMapReadyCallback {

    private TextView Address, Opentime, Tel, Parking;
    private GoogleMap map;
    private MapView mapView;
    private ProgressBar progressBar;
    private String item, data, county, idString, attractionID;
    private LatLng location;
    private TableLayout tableLayout;
    private File file;

    public Information() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_information, container, false);

        file = new File(getActivity().getFilesDir().getParentFile().getPath() + "/shared_prefs/", "LoginInfo.xml");
        if (file.exists()) {
            ReadValue();
        }

        //取得Toolbar Menu
        setHasOptionsMenu(true);

        //region 在Fragment裡使用map須用mapView
        mapView = (MapView) v.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        mapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //endregion


        //region 取得Activity資料
        TextView textView = (TextView) getActivity().findViewById(R.id.toolbar_title);
        TextView text = (TextView) getActivity().findViewById(R.id.textViewTmp);
        TextView text2 = (TextView) getActivity().findViewById(R.id.textViewCounty);
        data = text.getText().toString();
        System.out.println("data: "+data);
        item = textView.getText().toString();
        county = text2.getText().toString();
        //endregion

        //region findview
        tableLayout = (TableLayout) v.findViewById(R.id.tableLayout);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        Address = (TextView) v.findViewById(R.id.textViewAddress);
        Opentime = (TextView) v.findViewById(R.id.textViewOpentime);
        Tel = (TextView) v.findViewById(R.id.textViewTels);
        Parking = (TextView) v.findViewById(R.id.textViewParking);
        //endregion

        new informationAsyncTask().execute();

        return v;
    }

    //region Fragment的Menu調用
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.toolbar_menu_county, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        getCollectionStatus(item);
        switch (item.getItemId()) {
            case R.id.action_collect:
                Drawable drawable = item.getIcon();
                if (!(drawable.getConstantState().equals(getResources().getDrawable(R.mipmap.ic_liked).getConstantState()))) {
                    if (file.exists()) {
                        addCollection();
                        item.setIcon(R.mipmap.ic_liked);
                    } else {
                        Toast.makeText(getContext(), "請登入進行此操作", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
        return true;
    }
    //endregion

    @Override
    public void onMapReady(GoogleMap googleMap) { //準備地圖物件
        map = googleMap;
        UiSettings us = map.getUiSettings();
        us.setScrollGesturesEnabled(false); //讓map不能被使用者移動
    }

    private void addCollection() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                String url = "http://140.131.114.161:8080/Formosa/rest/collection/addUserCollection";
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httpRequst = new HttpPost(url);

                    JSONObject parameter = new JSONObject();

                    parameter.accumulate("userID", idString);
                    parameter.accumulate("attractionID", attractionID);
                    parameter.accumulate("attractionName", item);
                    parameter.accumulate("county", county);

                    String json = parameter.toString();

                    StringEntity se = new StringEntity(json, "UTF-8");
                    httpRequst.setEntity(se);
                    httpRequst.addHeader("Content-Type", "application/json");

                    HttpResponse responsePOST = httpclient.execute(httpRequst);

                    HttpEntity resEntity = responsePOST.getEntity();
                    final String result = EntityUtils.toString(resEntity);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                JSONObject collectionJson = new JSONObject(result);
                                String tmp = collectionJson.get("statuscode").toString();

                                if (tmp.equals("0")) {
                                    Toast toast = Toast.makeText(getActivity(), "收藏成功", Toast.LENGTH_SHORT);
                                    toast.show();
                                } else if (tmp.equals("40")) {
                                    Toast toast = Toast.makeText(getActivity(), "已收藏過了", Toast.LENGTH_SHORT);
                                    toast.show();
                                } else {
                                    Toast toast = Toast.makeText(getActivity(), "失敗請重試", Toast.LENGTH_SHORT);
                                    toast.show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });


                    System.out.println(result);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getCollectionStatus(final MenuItem item) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                String url = "http://140.131.114.161:8080/Formosa/rest/collection/getUserCollectionByUser";
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
                    final String result = EntityUtils.toString(resEntity, "UTF-8");

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                JSONObject collectionJson = new JSONObject(result);
                                String tmp = collectionJson.get("statuscode").toString();

                                if (tmp.equals("0")) {
                                    JSONArray collectionsJSON = new JSONArray(collectionJson.get("Collections").toString());
                                    if (collectionsJSON.length() > 0) {
                                        for (int i = 0; i < collectionsJSON.length(); i++) {
                                            JSONObject collection = new JSONObject(collectionsJSON.get(i).toString());
                                            String collectionCounty = collection.get("county").toString();
                                            if (collectionCounty.equals(county)) {
                                                String collectioniId = collection.get("attractionID").toString();
                                                if (collectioniId.equals(attractionID)) {
                                                    item.setIcon(R.mipmap.ic_liked);
                                                }
                                            }
                                            System.out.println("collection:" + collection.toString());
                                        }
                                    }
                                } else {
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });


                    System.out.println(result);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    class informationAsyncTask extends AsyncTask<String, Integer, Integer> {

        JSONObject itemJSON = new JSONObject();

        @Override
        protected Integer doInBackground(String... param) {

            try {
                itemJSON = new JSONObject(data);
                attractionID = itemJSON.get("_id").toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            progressBar.setVisibility(View.GONE);
            mapView.setVisibility(View.VISIBLE);
            tableLayout.setVisibility(View.VISIBLE);
            System.out.println("itemJSON: "+data);

            try {
                if (county.equals("台北市")) {
                    Address.setText(itemJSON.get("address").toString());
                    Opentime.setText(itemJSON.get("MEMO_TIME").toString());
                    Tel.setText("No Data");
                    Parking.setText(itemJSON.get("MRT").toString());
                    String latput = itemJSON.get("latitude").toString();
                    String lonput = itemJSON.get("longitude").toString();
                    Double lat = Double.parseDouble(latput);
                    Double lon = Double.parseDouble(lonput);
                    location = new LatLng(lat, lon);
                    System.out.println("地圖前");
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
                    System.out.println("地圖後");
                }

                if (county.equals("桃園市")) {
                    Address.setText(itemJSON.get("Add").toString());
                    Opentime.setText(itemJSON.get("Opentime").toString());
                    Tel.setText(itemJSON.get("Tel").toString());
                    Parking.setText(itemJSON.get("Parkinginfo").toString());
                    String latput = itemJSON.get("Py").toString();
                    String lonput = itemJSON.get("Px").toString();
                    Double lat = Double.parseDouble(latput);
                    Double lon = Double.parseDouble(lonput);
                    location = new LatLng(lat, lon);
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void ReadValue() {
        SharedPreferences setting = getActivity().getSharedPreferences("LoginInfo", 0);
        idString = setting.getString("Id", "");
    }

    //region map事件
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    //endregion
}
