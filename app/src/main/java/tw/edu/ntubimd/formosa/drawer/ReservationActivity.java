package tw.edu.ntubimd.formosa.drawer;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import tw.edu.ntubimd.formosa.R;

public class ReservationActivity extends AppCompatActivity {

    private String idString;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        File file = new File(getApplication().getFilesDir().getParentFile().getPath() + "/shared_prefs/", "LoginInfo.xml");
        if (file.exists()) {
            ReadValue();
        }

        listView = (ListView) findViewById(R.id.listview);

        //region Toolbar設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("代訂紀錄");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //取消AppTitle
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //endregion

        getReservationRecord();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //返回鍵
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getReservationRecord() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                String url = "http://140.131.114.161:8080/Formosa/rest/hotelOrder/getHotelOrderByUser";
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

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                JSONObject reservationJson = new JSONObject(result);
                                String tmp = reservationJson.get("statuscode").toString();

                                if (tmp.equals("0")) {
                                    JSONArray ordersJSON = new JSONArray(reservationJson.get("Orders").toString());
                                    ArrayList<HashMap<String, Object>> listData = new ArrayList<HashMap<String, Object>>();
                                    if (ordersJSON.length() > 0) {
                                        for (int i = 0; i < ordersJSON.length(); i++) {
                                            JSONObject orderJSON = new JSONObject(ordersJSON.get(i).toString());
                                            HashMap<String, Object> hashMap = new HashMap<String, Object>();
                                            hashMap.put("Name", orderJSON.get("hotelName").toString());
                                            hashMap.put("HotelAddress", orderJSON.get("hotelAddress").toString());
                                            listData.add(hashMap);
                                        }
                                    }
                                    SimpleAdapter listAdapter = new SimpleAdapter(ReservationActivity.this, listData, R.layout.reservationlistviewitem, // 宣告一個SimpleAdapter五個參數分別為(List位置,
                                            // List資料來源,
                                            // ListView介面檔,
                                            // List裡資料的名稱,
                                            // List裡資料的介面檔中id)
                                            new String[]{"Name", "HotelAddress"}, new int[]{R.id.textViewName, R.id.textViewAddress});
                                    listView.setAdapter(listAdapter);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void ReadValue() {
        SharedPreferences setting = getSharedPreferences("LoginInfo", 0);
        idString = setting.getString("Id", "");
    }
}
