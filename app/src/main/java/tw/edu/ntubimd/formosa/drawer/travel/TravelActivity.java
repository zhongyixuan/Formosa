package tw.edu.ntubimd.formosa.drawer.travel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import tw.edu.ntubimd.formosa.R;

public class TravelActivity extends AppCompatActivity {

    private FloatingActionButton newAddTravel;
    private ListView travelList;
    private ArrayList<String> travelIdList = new ArrayList<String>();
    private ArrayList<String> travelNameList = new ArrayList<String>();
    private String travelID, travelName, idString;
    private BroadcastReceiver broadcast_reciever = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);

        File file = new File(getApplication().getFilesDir().getParentFile().getPath() + "/shared_prefs/", "LoginInfo.xml");
        if (file.exists()) {
            ReadValue();
        }

        newAddTravel = (FloatingActionButton) findViewById(R.id.newAddTravel);
        travelList = (ListView) findViewById(R.id.listView);

        //region Toolbar設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("我的行程");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //取消AppTitle
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //endregion

        newAddTravel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                broadcast_reciever = new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context arg0, Intent intent) {
                        String action = intent.getAction();
                        if (action.equals("finish_activity")) {
                            finish();
                        }
                    }
                };
                registerReceiver(broadcast_reciever, new IntentFilter("finish_activity"));

                Intent intent = new Intent();
                intent.setClass(TravelActivity.this, AddTravelActivity.class);
                startActivity(intent);
            }
        });

        List<HashMap<String, Object>> ListData = showTravelName();
        SimpleAdapter ListAdapter = new SimpleAdapter(this, ListData, R.layout.travellistview, new String[]{"TravelName"}, new int[]{R.id.travelNameConfirm});

        travelList.setAdapter(ListAdapter);

        travelList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(TravelActivity.this, TravelDetailActivity.class);
                travelID = travelIdList.get(position);
                travelName = travelNameList.get(position);
                intent.putExtra("travelID", travelID); //將參數放入
                intent.putExtra("travelName", travelName); //將參數放入
//                Toast toast = Toast.makeText(TravelActivity.this, travelID + travelName, Toast.LENGTH_SHORT);
//                toast.show();
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

    public List<HashMap<String, Object>> showTravelName() {

        final List<HashMap<String, Object>> DataReturn = new ArrayList<HashMap<String, Object>>();
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

        Callable<List<HashMap<String, Object>>> callable = new Callable<List<HashMap<String, Object>>>() {
            public List<HashMap<String, Object>> call() throws Exception {
                List<HashMap<String, Object>> Data = new ArrayList<HashMap<String, Object>>();
                String url = "http://140.131.114.161:8080/Formosa/rest/travel/getTravelByUserID";
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

                    System.out.println(result);

                    JSONObject resultJson = new JSONObject(result);

                    String tmp = resultJson.get("statuscode").toString();

                    if (tmp.equals("0")) {
                        if (resultJson.length() > 0) {
                            JSONArray travelJSON = new JSONArray(resultJson.get("travels").toString());
                            if (travelJSON.length() > 0) {
                                for (int i = travelJSON.length() - 1; i >= 0; i--) {
                                    JSONObject travelsJSON = new JSONObject(travelJSON.get(i).toString());
                                    if (travelsJSON.has("travelID")) {
                                        travelIdList.add(travelsJSON.get("travelID").toString());
                                        travelNameList.add(travelsJSON.get("travelName").toString());
                                        HashMap<String, Object> hashMap = new HashMap<String, Object>();
                                        hashMap.put("TravelName", travelsJSON.get("travelName"));
                                        Data.add(hashMap);
                                    }
                                }
                            }
                        }
                    } else {
                        Toast toast = Toast.makeText(TravelActivity.this, "發生失敗請重試", Toast.LENGTH_SHORT);
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
            FutureTask<List<HashMap<String, Object>>> future = new FutureTask<List<HashMap<String, Object>>>(callable);
            new Thread(future).start();
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return DataReturn;
    }

    public void ReadValue() {
        SharedPreferences setting = getSharedPreferences("LoginInfo", 0);
        idString = setting.getString("Id", "");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcast_reciever != null){
            unregisterReceiver(broadcast_reciever);
        }
    }
}
