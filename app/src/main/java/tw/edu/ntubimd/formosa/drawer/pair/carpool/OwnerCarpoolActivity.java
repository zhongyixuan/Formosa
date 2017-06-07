package tw.edu.ntubimd.formosa.drawer.pair.carpool;

import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import tw.edu.ntubimd.formosa.R;

public class OwnerCarpoolActivity extends AppCompatActivity {

    private TextView textViewTravelName;
    private ListView travelDateList;
    private String travelID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_carpool);

        textViewTravelName = (TextView) findViewById(R.id.textViewTravelName);
        travelDateList = (ListView) findViewById(R.id.listView);

        Intent intent = getIntent();
        travelID = intent.getStringExtra("travelID");
        getTravelInfo();

        //region Toolbar設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("共乘");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //取消AppTitle
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //endregion

        ArrayList<HashMap<String, Object>> ListData = showTravelDetail();
        SimpleAdapter ListAdapter = new SimpleAdapter(this, ListData, R.layout.traveldetaillistview, new String[]{"TravelTime", "TravelAttractionName"}, new int[]{R.id.travelTimeTextView, R.id.travelNameTextView});
        //SimpleAdapter ListAdapter = new SimpleAdapter(this,ListData,R.layout.activity_traveldetaillistview,new String[]{"Time","TravelTime","Name","TravelAttractionName"},new int[]{R.id.imageView9,R.id.travelTimeTextView,R.id.travelNameTextView,R.id.imageView10});
        travelDateList.setAdapter(ListAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //返回鍵
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getTravelInfo() {
        final String url = "http://140.131.114.161:8080/Formosa/rest/travel/getUserTravelByTravelId";

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HttpClient httpclient = new DefaultHttpClient();
                            HttpPost httpRequst = new HttpPost(url);
                            JSONObject parameter = new JSONObject();

                            parameter.accumulate("travelID", travelID);

                            String json = parameter.toString();
                            StringEntity se = new StringEntity(json);
                            httpRequst.setEntity(se);
                            httpRequst.addHeader("Content-Type", "application/json");
                            HttpResponse responsePOST = httpclient.execute(httpRequst);
                            HttpEntity resEntity = responsePOST.getEntity();
                            String result = EntityUtils.toString(resEntity, "UTF-8");

                            JSONObject registerJson = new JSONObject(result);
                            String tmp = registerJson.get("statuscode").toString();

                            if (tmp.equals("0")) {
                                JSONObject travelJSON = new JSONObject(registerJson.get("Travel").toString());

                                textViewTravelName.setText(travelJSON.get("travelName").toString());
                            } else {
                                Toast toast = Toast.makeText(OwnerCarpoolActivity.this, "失敗請重試", Toast.LENGTH_SHORT);
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

    public ArrayList<HashMap<String, Object>> showTravelDetail() {

        final ArrayList<HashMap<String, Object>> DataReturn = new ArrayList<HashMap<String, Object>>();
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(
                    new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork() // or
                            // .detectAll()
                            // for
                            // all
                            // detectable
                            // problems
                            .penaltyLog().build());
        }

        Callable<ArrayList<HashMap<String, Object>>> callable = new Callable<ArrayList<HashMap<String, Object>>>() {
            public ArrayList<HashMap<String, Object>> call() throws Exception {
                ArrayList<HashMap<String, Object>> Data = new ArrayList<HashMap<String, Object>>();
                String url = "http://140.131.114.161:8080/Formosa/rest/travelAttraction/getTravelAttractionByID";
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httpRequst = new HttpPost(url);
                    JSONObject parameter = new JSONObject();

                    parameter.accumulate("travelID", travelID);

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
                    System.out.println(tmp);

                    if (tmp.equals("0")) {
                        if (resultJson.length() > 0) {
                            JSONArray travelJSON = new JSONArray(resultJson.get("travelAttractions").toString());
                            if (travelJSON.length() > 0) {
                                for (int i = 0; i < travelJSON.length(); i++) {
                                    JSONObject travelsJSON = new JSONObject(travelJSON.get(i).toString());
                                    if (travelsJSON.has("travelID")) {
                                        HashMap<String, Object> hashMap = new HashMap<String, Object>();
                                        int doc = travelsJSON.get("dayDate").toString().indexOf(".");
                                        String TravelTime = travelsJSON.get("dayDate").toString().substring(0, doc);
                                        hashMap.put("TravelTime", TravelTime);
                                        hashMap.put("TravelAttractionName", travelsJSON.get("attractionName"));
                                        Data.add(hashMap);
                                    }
                                }
                            }
                        }
                    } else {
                        Toast toast = Toast.makeText(OwnerCarpoolActivity.this, "發生失敗請重試", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    System.out.println(result);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                DataReturn.addAll(Data);
                return Data;
            }
        };
        try {
            FutureTask<ArrayList<HashMap<String, Object>>> future = new FutureTask<ArrayList<HashMap<String, Object>>>(callable);
            new Thread(future).start();
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return DataReturn;
    }
}
