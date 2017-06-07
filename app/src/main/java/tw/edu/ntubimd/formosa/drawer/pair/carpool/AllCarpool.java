package tw.edu.ntubimd.formosa.drawer.pair.carpool;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import tw.edu.ntubimd.formosa.drawer.pair.Utility;
import tw.edu.ntubimd.formosa.drawer.travel.TravelActivity;

public class AllCarpool extends Fragment {

    private FloatingActionButton addPair;
    private ListView list, listPaired, listExpired; // ListView
    private String idString;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ArrayList<String> travelPairIdList = new ArrayList<String>();
    private ArrayList<String> travelIdList = new ArrayList<String>();
    private ArrayList<String> userIdList = new ArrayList<String>();

    public AllCarpool() {
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
        View v = inflater.inflate(R.layout.fragment_all_carpool, container, false);

        File file = new File(getActivity().getFilesDir().getParentFile().getPath() + "/shared_prefs/", "LoginInfo.xml");
        if (file.exists()) {
            ReadValue();
        }

        addPair = (FloatingActionButton) v.findViewById(R.id.fab);
        list = (ListView) v.findViewById(R.id.listView);
        listPaired = (ListView) v.findViewById(R.id.listViewPaired);
        listExpired = (ListView) v.findViewById(R.id.listViewExpired);

        showAllCarpool();

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //refresh data
                        travelPairIdList.clear();
                        travelIdList.clear();
                        showAllCarpool();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        addPair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), TravelActivity.class);
                startActivity(intent);
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String travelId = travelIdList.get(position);
                String travePairId = travelPairIdList.get(position);
                String userID = userIdList.get(position);
                if (userID.equals(idString)){
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), OwnerCarpoolActivity.class);
                    intent.putExtra("travelID", travelId); //將參數放入
                    startActivity(intent);
                }else {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), AllCarpoolDetailActivity.class);
                    intent.putExtra("travelID", travelId); //將參數放入
                    intent.putExtra("travePairID", travePairId);
                    startActivity(intent);
                }
            }
        });

        return v;
    }

    public void showAllCarpool() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                String url = "http://140.131.114.161:8080/Formosa/rest/travelPair/getTravelPairList";
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
                        ArrayList<String> pairedList = new ArrayList<String>();
                        if (resultJson.length() > 0) {
                            JSONArray travelPairsJSON = new JSONArray(resultJson.get("TravelPairs").toString());
                            if (travelPairsJSON.length() > 0) {
                                for (int i = travelPairsJSON.length() - 1; i >= 0; i--) {
                                    JSONObject travelPairJSON = new JSONObject(travelPairsJSON.get(i).toString());

                                    if (travelPairJSON.has("travelID")) {

                                        travelIdList.add(travelPairJSON.get("travelID").toString());
                                        travelPairIdList.add(travelPairJSON.get("travelPairID").toString());
                                        userIdList.add(travelPairJSON.get("userID").toString());
                                        pairedList.add(travelPairJSON.get("paired").toString());
                                    }
                                }
                            }
                        }
                        getAllCarpoolInfo(travelIdList, pairedList);
                    } else if (tmp.equals("40")){
                    }else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast toast = Toast.makeText(getActivity(), "發生失敗請重試", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getAllCarpoolInfo(final ArrayList<String> travelID, final ArrayList<String> paired) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        List<HashMap<String, Object>> ListData = new ArrayList<HashMap<String, Object>>();
                        List<HashMap<String, Object>> ListDataPaired = new ArrayList<HashMap<String, Object>>();
                        List<HashMap<String, Object>> ListDataExpired = new ArrayList<HashMap<String, Object>>();

                        for (int i = 0; i < travelID.size(); i++) {
                            String url = "http://140.131.114.161:8080/Formosa/rest/travel/getUserTravelByTravelId";

                            try {
                                HttpClient httpclient = new DefaultHttpClient();
                                HttpPost httpRequst = new HttpPost(url);
                                JSONObject parameter = new JSONObject();

                                parameter.accumulate("travelID", travelID.get(i));

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
                                        if (resultJson.has("Travel")) {
                                            JSONObject travelJSON = new JSONObject(resultJson.get("Travel").toString());

                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                            Date date = sdf.parse(travelJSON.get("travelDate").toString());
                                            Calendar c = Calendar.getInstance();
                                            c.setTime(date);
                                            Calendar now = Calendar.getInstance();
                                            if (now.after(c)) {
                                                HashMap<String, Object> hashMapExpired = new HashMap<String, Object>();
                                                hashMapExpired.put("TravelName", travelJSON.get("travelName"));
                                                hashMapExpired.put("TravelDate", travelJSON.get("travelDate"));
                                                hashMapExpired.put("TravelDays", travelJSON.get("travelDays"));
                                                ListDataExpired.add(hashMapExpired);
                                            } else {
                                                if (paired.get(i).equals("false")) {
                                                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                                                    hashMap.put("TravelName", travelJSON.get("travelName"));
                                                    hashMap.put("TravelDate", travelJSON.get("travelDate"));
                                                    hashMap.put("TravelDays", travelJSON.get("travelDays"));
                                                    ListData.add(hashMap);
                                                } else {
                                                    HashMap<String, Object> hashMapPaired = new HashMap<String, Object>();
                                                    hashMapPaired.put("TravelName", travelJSON.get("travelName"));
                                                    hashMapPaired.put("TravelDate", travelJSON.get("travelDate"));
                                                    hashMapPaired.put("TravelDays", travelJSON.get("travelDays"));
                                                    ListDataPaired.add(hashMapPaired);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Toast toast = Toast.makeText(getActivity(), "發生失敗請重試", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        SimpleAdapter ListAdapter = new SimpleAdapter(getActivity(), ListData, R.layout.carpool_list_item, // 宣告一個SimpleAdapter五個參數分別為(List位置,
                                // List資料來源,
                                // ListView介面檔,
                                // List裡資料的名稱,
                                // List裡資料的介面檔中id)
                                new String[]{"TravelName", "TravelDate", "TravelDays"}, new int[]{R.id.textViewUserTravelName, R.id.textViewUserTravelDate, R.id.textViewUserTravelDays});
                        SimpleAdapter ListAdapterExpired = new SimpleAdapter(getActivity(), ListDataExpired, R.layout.carpool_list_item_expired, // 宣告一個SimpleAdapter五個參數分別為(List位置,
                                // List資料來源,
                                // ListView介面檔,
                                // List裡資料的名稱,
                                // List裡資料的介面檔中id)
                                new String[]{"TravelName", "TravelDate", "TravelDays"}, new int[]{R.id.textViewUserTravelName, R.id.textViewUserTravelDate, R.id.textViewUserTravelDays});
                        SimpleAdapter ListAdapterPaired = new SimpleAdapter(getActivity(), ListDataPaired, R.layout.carpool_list_item_paired, // 宣告一個SimpleAdapter五個參數分別為(List位置,
                                // List資料來源,
                                // ListView介面檔,
                                // List裡資料的名稱,
                                // List裡資料的介面檔中id)
                                new String[]{"TravelName", "TravelDate", "TravelDays"}, new int[]{R.id.textViewUserTravelName, R.id.textViewUserTravelDate, R.id.textViewUserTravelDays});
                        list.setAdapter(ListAdapter);
                        listPaired.setAdapter(ListAdapterPaired);
                        listExpired.setAdapter(ListAdapterExpired);

                        Utility.setListViewHeightBasedOnChildren(list);
                        Utility.setListViewHeightBasedOnChildren(listPaired);
                        Utility.setListViewHeightBasedOnChildren(listExpired);
                    }
                });
            }
        }).start();
    }

    public void ReadValue() {
        SharedPreferences setting = getActivity().getSharedPreferences("LoginInfo", 0);
        idString = setting.getString("Id", "");
    }
}
