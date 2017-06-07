package tw.edu.ntubimd.formosa.drawer.pair.carpool;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import tw.edu.ntubimd.formosa.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class NowCarpool extends Fragment {

    private String idString;
    private ListView list;
    private ArrayList<String> travelIdArray = new ArrayList<String>();

    public NowCarpool() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_now_carpool, container, false);

        File file = new File(getActivity().getFilesDir().getParentFile().getPath() + "/shared_prefs/", "LoginInfo.xml");
        if (file.exists()) {
            ReadValue();
        }

        list = (ListView) v.findViewById(R.id.listView);

        getCarpoolID();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String travelID = travelIdArray.get(position);
                Intent intent = new Intent();
                intent.setClass(getActivity(), OwnerCarpoolActivity.class);
                intent.putExtra("travelID", travelID); //將參數放入
                startActivity(intent);
            }
        });

        return v;
    }

    public void getCarpoolID() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        ArrayList<String> travelPairIdArray = new ArrayList<String>();

                        String url = "http://140.131.114.161:8080/Formosa/rest/travelPairUserInfo/getTravelPairID";
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

                            JSONObject resultJson = new JSONObject(result);

                            String tmp = resultJson.get("statuscode").toString();

                            if (tmp.equals("0")) {
                                if (resultJson.length() > 0) {
                                    JSONArray travelPairsJSON = new JSONArray(resultJson.get("TravelPairs").toString());
                                    if (travelPairsJSON.length() > 0) {
                                        for (int  i = travelPairsJSON.length()-1;i >= 0; i--) {
                                            JSONObject travelPairJSON = new JSONObject(travelPairsJSON.get(i).toString());
                                            if (travelPairJSON.get("userSure").equals("true")){
                                                travelPairIdArray.add(travelPairJSON.get("travelPairID").toString());
                                            }
                                        }
                                        getNowCarpool(travelPairIdArray);
                                    }
                                }
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

    private void getNowCarpool(final ArrayList<String> travelPairId){

        new Thread(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        for (int i = 0; i < travelPairId.size(); i++) {
                            String url = "http://140.131.114.161:8080/Formosa/rest/travelPair/getTravelPairById";

                            try {
                                HttpClient httpclient = new DefaultHttpClient();
                                HttpPost httpRequst = new HttpPost(url);
                                JSONObject parameter = new JSONObject();

                                parameter.accumulate("travelPairID", travelPairId.get(i));

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
                                        if (resultJson.has("TravelPair")) {
                                            JSONObject travelPairJSON = new JSONObject(resultJson.get("TravelPair").toString());
                                            travelIdArray.add(travelPairJSON.get("travelID").toString());
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
                        getNowCarpoolInfo(travelIdArray);
                    }
                });
            }
        }).start();
    }

    private void getNowCarpoolInfo(final ArrayList<String> travelId){
        new Thread(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        List<HashMap<String, Object>> ListData = new ArrayList<HashMap<String, Object>>();

                        for (int i = 0; i < travelId.size(); i++) {
                            String url = "http://140.131.114.161:8080/Formosa/rest/travel/getUserTravelByTravelId";

                            try {
                                HttpClient httpclient = new DefaultHttpClient();
                                HttpPost httpRequst = new HttpPost(url);
                                JSONObject parameter = new JSONObject();

                                parameter.accumulate("travelID", travelId.get(i));

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

                                            int days = Integer.parseInt(travelJSON.get("travelDays").toString());

                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                            Date date = sdf.parse(travelJSON.get("travelDate").toString());
                                            Calendar c = Calendar.getInstance();
                                            c.setTime(date);
                                            c.add(Calendar.DATE, days);
                                            Calendar now = Calendar.getInstance();

                                            if (now.before(c)) {
                                                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                                                hashMap.put("TravelName", travelJSON.get("travelName"));
                                                hashMap.put("TravelDate", travelJSON.get("travelDate"));
                                                hashMap.put("TravelDays", travelJSON.get("travelDays"));
                                                ListData.add(hashMap);
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

                        list.setAdapter(ListAdapter);
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
