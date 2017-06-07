package tw.edu.ntubimd.formosa.drawer.pair.carpool;


import android.content.Context;
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
import android.widget.TextView;
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
import java.util.Map;

import tw.edu.ntubimd.formosa.R;
import tw.edu.ntubimd.formosa.drawer.pair.Utility;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyCarpool extends Fragment {

    private ArrayList<String> itemType = new ArrayList<String>();
    private ArrayList<String> pairUserItemType = new ArrayList<String>();
    private String idString;
    private ListView list, listViewPairUser;
    private ArrayList<String> travelIdList = new ArrayList<String>();

    public MyCarpool() {
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
        View v = inflater.inflate(R.layout.fragment_my_carpool, container, false);

        File file = new File(getActivity().getFilesDir().getParentFile().getPath() + "/shared_prefs/", "LoginInfo.xml");
        if (file.exists()) {
            ReadValue();
        }

        list = (ListView) v.findViewById(R.id.listView);
        listViewPairUser = (ListView) v.findViewById(R.id.listViewPairUser);

        showMyCarpool();
//        showMyCarpoolByPairUser();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), MyCarpoolOwnerActivity.class);
                String travelId = travelIdList.get(position);
                intent.putExtra("travelID", travelId); //將參數放入
                startActivity(intent);
            }
        });

        return v;
    }

    public void showMyCarpool() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                String url = "http://140.131.114.161:8080/Formosa/rest/travelPair/getPairByUserID";
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
                                for (int i = travelPairsJSON.length() - 1; i >= 0; i--) {
                                    JSONObject travelPairJSON = new JSONObject(travelPairsJSON.get(i).toString());

                                    if (travelPairJSON.has("travelID")) {
                                        itemType.add(travelPairJSON.get("paired").toString());
                                        travelIdList.add(travelPairJSON.get("travelID").toString());
                                    }
                                }
                            }
                        }
                        getMyCarpoolInfo(travelIdList);
                    } else if (tmp.equals("40")) {
                    } else {
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

    private void getMyCarpoolInfo(final ArrayList<String> travelID) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        List<HashMap<String, Object>> ListData = new ArrayList<HashMap<String, Object>>();

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

                                            HashMap<String, Object> hashMap = new HashMap<String, Object>();
                                            hashMap.put("TravelName", travelJSON.get("travelName"));
                                            hashMap.put("TravelDate", travelJSON.get("travelDate"));
                                            hashMap.put("TravelDays", travelJSON.get("travelDays"));
                                            ListData.add(hashMap);
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
                        MyAdapter ListAdapter = new MyAdapter(getActivity(), ListData, R.layout.pair_list_item, // 宣告一個SimpleAdapter五個參數分別為(List位置,
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

    private void showMyCarpoolByPairUser() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                String url = "http://140.131.114.161:8080/Formosa/rest/travelPairUserInfo/getTravelPairIDByPairUserID";
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
                        ArrayList<String> travelPairIdList = new ArrayList<String>();
                        if (resultJson.length() > 0) {
                            JSONArray travelPairsJSON = new JSONArray(resultJson.get("TravelPairs").toString());
                            if (travelPairsJSON.length() > 0) {
                                for (int i = travelPairsJSON.length() - 1; i >= 0; i--) {
                                    JSONObject travelPairJSON = new JSONObject(travelPairsJSON.get(i).toString());

                                    if (travelPairJSON.has("travelPairID")) {
                                        travelPairIdList.add(travelPairJSON.get("travelPairID").toString());
                                    }
                                }
                            }
                        }
                        getMyCarpoolTravelPairInfo(travelPairIdList);
                    } else if (tmp.equals("40")) {
                    } else {
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

    private void getMyCarpoolTravelPairInfo(final ArrayList<String> travelPairID) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        ArrayList<String> travelIdList = new ArrayList<String>();

                        for (int i = 0; i < travelPairID.size(); i++) {
                            String url = "http://140.131.114.161:8080/Formosa/rest/travelPair/getTravelPairById";

                            try {
                                HttpClient httpclient = new DefaultHttpClient();
                                HttpPost httpRequst = new HttpPost(url);
                                JSONObject parameter = new JSONObject();

                                parameter.accumulate("travelPairID", travelPairID.get(i));

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
                                            JSONObject travelJSON = new JSONObject(resultJson.get("TravelPair").toString());

                                            pairUserItemType.add(travelJSON.get("paired").toString());
                                            travelIdList.add(travelJSON.get("travelID").toString());
                                        }
                                    }
                                    getMyCarpoolInfoByPairUser(travelIdList);
                                } else {
                                    Toast toast = Toast.makeText(getActivity(), "發生失敗請重試", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }).start();
    }

    private void getMyCarpoolInfoByPairUser(final ArrayList<String> travelID){
        new Thread(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        List<HashMap<String, Object>> ListData = new ArrayList<HashMap<String, Object>>();

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

                                            HashMap<String, Object> hashMap = new HashMap<String, Object>();
                                            hashMap.put("TravelName", travelJSON.get("travelName"));
                                            hashMap.put("TravelDate", travelJSON.get("travelDate"));
                                            hashMap.put("TravelDays", travelJSON.get("travelDays"));
                                            ListData.add(hashMap);
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
                        MyAdapterPairUser ListAdapter = new MyAdapterPairUser(getActivity(), ListData, R.layout.pair_list_item, // 宣告一個SimpleAdapter五個參數分別為(List位置,
                                // List資料來源,
                                // ListView介面檔,
                                // List裡資料的名稱,
                                // List裡資料的介面檔中id)
                                new String[]{"TravelName", "TravelDate", "TravelDays"}, new int[]{R.id.textViewUserTravelName, R.id.textViewUserTravelDate, R.id.textViewUserTravelDays});

                        listViewPairUser.setAdapter(ListAdapter);
                    }
                });
            }
        }).start();
    }

    public class MyAdapter extends SimpleAdapter {
        private final Context context;
        private final List<? extends Map<String, ?>> data;
        private final int resource;
        private final String[] from;
        private final int[] to;

        public MyAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            this.context = context;
            this.data = data;
            this.resource = resource;
            this.from = from;
            this.to = to;
        }


        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (Boolean.parseBoolean(itemType.get(position))) {
                v = inflater.inflate(R.layout.carpool_list_item_paired, parent, false);
            } else {
                v = inflater.inflate(R.layout.carpool_list_item, parent, false);
            }

            TextView textViewUserTravelName = (TextView) v.findViewById(R.id.textViewUserTravelName);
            TextView textViewUserTravelDate = (TextView) v.findViewById(R.id.textViewUserTravelDate);
            TextView textViewUserTravelDays = (TextView) v.findViewById(R.id.textViewUserTravelDays);

            textViewUserTravelName.setText(data.get(position).get("TravelName").toString());
            textViewUserTravelDate.setText(data.get(position).get("TravelDate").toString());
            textViewUserTravelDays.setText(data.get(position).get("TravelDays").toString());
            return v;
        }
    }

    public class MyAdapterPairUser extends SimpleAdapter {
        private final Context context;
        private final List<? extends Map<String, ?>> data;
        private final int resource;
        private final String[] from;
        private final int[] to;

        public MyAdapterPairUser(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            this.context = context;
            this.data = data;
            this.resource = resource;
            this.from = from;
            this.to = to;
        }


        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (Boolean.parseBoolean(pairUserItemType.get(position))) {
                v = inflater.inflate(R.layout.carpool_list_item_paired, parent, false);
            } else {
                v = inflater.inflate(R.layout.carpool_list_item, parent, false);
            }

            TextView textViewUserTravelName = (TextView) v.findViewById(R.id.textViewUserTravelName);
            TextView textViewUserTravelDate = (TextView) v.findViewById(R.id.textViewUserTravelDate);
            TextView textViewUserTravelDays = (TextView) v.findViewById(R.id.textViewUserTravelDays);

            textViewUserTravelName.setText(data.get(position).get("TravelName").toString());
            textViewUserTravelDate.setText(data.get(position).get("TravelDate").toString());
            textViewUserTravelDays.setText(data.get(position).get("TravelDays").toString());
            return v;
        }
    }

    public void ReadValue() {
        SharedPreferences setting = getActivity().getSharedPreferences("LoginInfo", 0);
        idString = setting.getString("Id", "");
    }
}
