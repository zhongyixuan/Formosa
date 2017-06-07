package tw.edu.ntubimd.formosa.drawer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
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

import tw.edu.ntubimd.formosa.R;

public class CarpoolPairUserInfoActivity extends AppCompatActivity {

    private ListView listView;
    private String idString;
    private ArrayList<String> travelPairIdArray = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carpool_pair_user_info);

        File file = new File(getFilesDir().getParentFile().getPath() + "/shared_prefs/", "LoginInfo.xml");
        if (file.exists()) {
            ReadValue();
        }

        //region Toolbar設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("Pair通知");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //取消AppTitle
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //endregion

        listView = (ListView) findViewById(R.id.listView);
        getTravelPairID();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String travelPairId = travelPairIdArray.get(position);
                Intent intent = new Intent();
                intent.setClass(CarpoolPairUserInfoActivity.this, CarpoolPairUserInfoDetailActivity.class);
                intent.putExtra("travePairID", travelPairId);
                startActivity(intent);
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

    private void getTravelPairID() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        List<HashMap<String, Object>> hashMapArray = new ArrayList<HashMap<String, Object>>();

                        String url = "http://140.131.114.161:8080/Formosa/rest/travelPairUserInfo/getTravelPairInfoByUserID";
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
                                        for (int i = 0; i < travelPairsJSON.length(); i++) {

                                            JSONObject travelPairJSON = new JSONObject(travelPairsJSON.get(i).toString());
                                            if (travelPairJSON.get("userSure").toString().equals("false")){
                                                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                                                hashMap.put("PairUserName", travelPairJSON.get("pairUserName").toString());
                                                hashMap.put("PairUserEMail", travelPairJSON.get("pairUserEMail").toString());
                                                hashMapArray.add(hashMap);
                                                travelPairIdArray.add(travelPairJSON.get("travelPairID").toString());
                                            }
                                        }
                                        getTravelID(travelPairIdArray, hashMapArray);
                                    }
                                }
                            } else {
                                Toast toast = Toast.makeText(CarpoolPairUserInfoActivity.this, "發生失敗請重試", Toast.LENGTH_SHORT);
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

    private void getTravelID(final ArrayList<String> travelPairId, final List<HashMap<String, Object>> hashMapArray) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        ArrayList<String> travelIdArray = new ArrayList<String>();

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
                                    Toast toast = Toast.makeText(CarpoolPairUserInfoActivity.this, "發生失敗請重試", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        getTravelInfo(travelIdArray, hashMapArray);
                    }
                });
            }
        }).start();
    }

    private void getTravelInfo(final ArrayList<String> travelId, final List<HashMap<String, Object>> hashMapArray) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
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

                                            HashMap<String, Object> hashMap = hashMapArray.get(i);
                                            hashMap.put("TravelName", travelJSON.get("travelName"));
                                            ListData.add(hashMap);
                                        }
                                    }
                                } else {
                                    Toast toast = Toast.makeText(CarpoolPairUserInfoActivity.this, "發生失敗請重試", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        SimpleAdapter ListAdapter = new SimpleAdapter(CarpoolPairUserInfoActivity.this, ListData, R.layout.carpool_userinfo_listview, // 宣告一個SimpleAdapter五個參數分別為(List位置,
                                // List資料來源,
                                // ListView介面檔,
                                // List裡資料的名稱,
                                // List裡資料的介面檔中id)
                                new String[]{"TravelName", "PairUserName", "PairUserEMail"}, new int[]{R.id.travelNameConfirm, R.id.textViewUserName, R.id.textViewUserEmail});

                        listView.setAdapter(ListAdapter);
                    }
                });
            }
        }).start();
    }

    public void ReadValue() {
        SharedPreferences setting = getSharedPreferences("LoginInfo", 0);
        idString = setting.getString("Id", "");
    }
}
