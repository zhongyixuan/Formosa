package tw.edu.ntubimd.formosa.drawer.pair;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import tw.edu.ntubimd.formosa.R;

public class AllPair extends Fragment {

    private FloatingActionButton addPair;
    private ListView list, listPaired, listExpired; // ListView
    private String pairID, userID, longitude, latitude;
    private ArrayList<String> pairIdList = new ArrayList<String>();
    private ArrayList<String> userList = new ArrayList<String>();
    private ArrayList<String> longitudeList = new ArrayList<String>();
    private ArrayList<String> latitudeList = new ArrayList<String>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean isGPSEnabled, isNetworkEnabled;
    private Location location = null;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    //The minimum time beetwen updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 0; //1000 * 60 * 1; // 1 minute
    private String idString;

    public AllPair() {
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
        View v = inflater.inflate(R.layout.fragment_all_pair, container, false);

        File file = new File(getActivity().getFilesDir().getParentFile().getPath() + "/shared_prefs/", "LoginInfo.xml");
        if (file.exists()) {
            ReadValue();
        }

        addPair = (FloatingActionButton) v.findViewById(R.id.fab);
        list = (ListView) v.findViewById(R.id.listView);
        listPaired = (ListView) v.findViewById(R.id.listViewPaired);
        listExpired = (ListView) v.findViewById(R.id.listViewExpired);

        getLocation();
        showAllPair();

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //refresh data
                        getLocation();
                        showAllPair();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pairID = pairIdList.get(position);
                userID = userList.get(position);
                longitude = longitudeList.get(position);
                latitude = latitudeList.get(position);

                if (userID.equals(idString)){
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), MyPairDetailActivity.class);
                    intent.putExtra("pairID", pairID); //將參數放入
                    intent.putExtra("userID", userID);
                    intent.putExtra("longitude", longitude);
                    intent.putExtra("latitude", latitude);
                    startActivity(intent);
                }else {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), AllPairDetailActivity.class);
                    intent.putExtra("pairID", pairID); //將參數放入
                    intent.putExtra("userID", userID);
                    intent.putExtra("longitude", longitude);
                    intent.putExtra("latitude", latitude);
                    startActivity(intent);
                }
            }
        });

        addPair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), AddPairActivity.class);
                startActivity(intent);
            }
        });

        return v;
    }

    private void getLocation() {
        LocationManager status = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        isGPSEnabled = status.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = status.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!(isGPSEnabled || isNetworkEnabled)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
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
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
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

    public void showAllPair() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<HashMap<String, Object>> ListData = new ArrayList<HashMap<String, Object>>();
                        List<HashMap<String, Object>> Data = new ArrayList<HashMap<String, Object>>();

                        List<HashMap<String, Object>> ListDataPaired = new ArrayList<HashMap<String, Object>>();
                        List<HashMap<String, Object>> DataPaired = new ArrayList<HashMap<String, Object>>();

                        List<HashMap<String, Object>> ListDataExpired = new ArrayList<HashMap<String, Object>>();
                        List<HashMap<String, Object>> DataExpired = new ArrayList<HashMap<String, Object>>();

                        String url = "http://140.131.114.161:8080/Formosa/rest/pair/getPairList";
                        try {
                            HttpClient httpclient = new DefaultHttpClient();
                            HttpPost httpRequst = new HttpPost(url);
                            JSONObject parameter = new JSONObject();

                            String json = parameter.toString();
                            StringEntity se = new StringEntity(json);
                            httpRequst.setEntity(se);
                            httpRequst.addHeader("Content-Type", "application/json");
                            HttpResponse responsePOST = httpclient.execute(httpRequst);
                            HttpEntity resEntity = responsePOST.getEntity();
                            String result = EntityUtils.toString(resEntity, "UTF-8");

                            JSONObject resultJson = new JSONObject(result);

                            String tmp = resultJson.get("statuscode").toString();

                            if (tmp.equals("0")) {
                                if (resultJson.length() > 0) {
                                    JSONArray pairsJSON = new JSONArray(resultJson.get("Pairs").toString());
                                    if (pairsJSON.length() > 0) {
                                        for (int i = pairsJSON.length() - 1; i >= 0; i--) {
                                            JSONObject pairJSON = new JSONObject(pairsJSON.get(i).toString());

                                            if (pairJSON.has("pairID")) {
                                                Double distance = null;
                                                distance = getDistance(location.getLatitude(), location.getLongitude(), Double.parseDouble(pairJSON.get("pairLatitude").toString()), Double.parseDouble(pairJSON.get("pairLongitude").toString()));
                                                if (distance < 1000.0) {
                                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //HH 24小時制 hh 12小時制
                                                    Date date = sdf.parse(pairJSON.get("pairTime").toString());
                                                    Calendar c = Calendar.getInstance();
                                                    c.setTime(date);
                                                    String s[] = pairJSON.get("waitTime").toString().split(":");
                                                    String h = s[0];
                                                    String m = s[1];
                                                    c.add(Calendar.HOUR_OF_DAY, Integer.parseInt(h));
                                                    c.add(Calendar.MINUTE, Integer.parseInt(m));
                                                    Calendar now = Calendar.getInstance();
                                                    if (now.after(c)) {
                                                        HashMap<String, Object> hashMapExpired = new HashMap<String, Object>();
                                                        hashMapExpired.put("ShopName", pairJSON.get("shopName"));
                                                        hashMapExpired.put("ProductName", pairJSON.get("productName"));
                                                        hashMapExpired.put("PreferentialType", pairJSON.get("preferentialType"));
                                                        hashMapExpired.put("PairAddress", pairJSON.get("pairAddress"));
                                                        int doc = pairJSON.get("pairTime").toString().indexOf(".");
                                                        String pairTime = pairJSON.get("pairTime").toString().substring(0, doc);
                                                        hashMapExpired.put("PairTime",pairTime);
                                                        DataExpired.add(hashMapExpired);
                                                    } else {
                                                        if (pairJSON.get("paired").toString().equals("false")) {
                                                            HashMap<String, Object> hashMap = new HashMap<String, Object>();
                                                            pairIdList.add(pairJSON.get("pairID").toString());
                                                            userList.add(pairJSON.get("userID").toString());
                                                            longitudeList.add(pairJSON.get("pairLongitude").toString());
                                                            latitudeList.add(pairJSON.get("pairLatitude").toString());
                                                            hashMap.put("ShopName", pairJSON.get("shopName"));
                                                            hashMap.put("ProductName", pairJSON.get("productName"));
                                                            hashMap.put("PreferentialType", pairJSON.get("preferentialType"));
                                                            hashMap.put("PairAddress", pairJSON.get("pairAddress"));
                                                            int doc = pairJSON.get("pairTime").toString().indexOf(".");
                                                            String pairTime = pairJSON.get("pairTime").toString().substring(0, doc);
                                                            hashMap.put("PairTime",pairTime);
                                                            Data.add(hashMap);
                                                        } else {
                                                            HashMap<String, Object> hashMapPaired = new HashMap<String, Object>();
                                                            hashMapPaired.put("ShopName", pairJSON.get("shopName"));
                                                            hashMapPaired.put("ProductName", pairJSON.get("productName"));
                                                            hashMapPaired.put("PreferentialType", pairJSON.get("preferentialType"));
                                                            hashMapPaired.put("PairAddress", pairJSON.get("pairAddress"));
                                                            int doc = pairJSON.get("pairTime").toString().indexOf(".");
                                                            String pairTime = pairJSON.get("pairTime").toString().substring(0, doc);
                                                            hashMapPaired.put("PairTime",pairTime);
                                                            DataPaired.add(hashMapPaired);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                ListData = Data;
                                ListDataPaired = DataPaired;
                                ListDataExpired = DataExpired;
                                SimpleAdapter ListAdapter = new SimpleAdapter(getActivity(), ListData, R.layout.pair_list_item, // 宣告一個SimpleAdapter五個參數分別為(List位置,
                                        // List資料來源,
                                        // ListView介面檔,
                                        // List裡資料的名稱,
                                        // List裡資料的介面檔中id)
                                        new String[]{"ShopName", "ProductName", "PreferentialType", "PairAddress", "PairTime"}, new int[]{R.id.textViewUserShopName, R.id.textViewUserProductName, R.id.textViewUserPreferentialType, R.id.textViewUserPairAddress, R.id.textViewUserPairTime});
                                SimpleAdapter ListAdapterExpired = new SimpleAdapter(getActivity(), ListDataExpired, R.layout.pair_list_item_expired, // 宣告一個SimpleAdapter五個參數分別為(List位置,
                                        // List資料來源,
                                        // ListView介面檔,
                                        // List裡資料的名稱,
                                        // List裡資料的介面檔中id)
                                        new String[]{"ShopName", "ProductName", "PreferentialType", "PairAddress", "PairTime"}, new int[]{R.id.textViewUserShopName, R.id.textViewUserProductName, R.id.textViewUserPreferentialType, R.id.textViewUserPairAddress, R.id.textViewUserPairTime});
                                SimpleAdapter ListAdapterPaired = new SimpleAdapter(getActivity(), ListDataPaired, R.layout.pair_list_item_paired, // 宣告一個SimpleAdapter五個參數分別為(List位置,
                                        // List資料來源,
                                        // ListView介面檔,
                                        // List裡資料的名稱,
                                        // List裡資料的介面檔中id)
                                        new String[]{"ShopName", "ProductName", "PreferentialType", "PairAddress", "PairTime"}, new int[]{R.id.textViewUserShopName, R.id.textViewUserProductName, R.id.textViewUserPreferentialType, R.id.textViewUserPairAddress, R.id.textViewUserPairTime});
                                list.setAdapter(ListAdapter);
                                listPaired.setAdapter(ListAdapterPaired);
                                listExpired.setAdapter(ListAdapterExpired);

                                Utility.setListViewHeightBasedOnChildren(list);
                                Utility.setListViewHeightBasedOnChildren(listPaired);
                                Utility.setListViewHeightBasedOnChildren(listExpired);
                            } else if (tmp.equals("40")){
                            }else {
                                Toast toast = Toast.makeText(getActivity(), "發生失敗請重試", Toast.LENGTH_SHORT);
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

    public double getDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

    public void ReadValue() {
        SharedPreferences setting = getActivity().getSharedPreferences("LoginInfo", 0);
        idString = setting.getString("Id", "");
    }
}
