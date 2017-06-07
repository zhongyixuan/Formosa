package tw.edu.ntubimd.formosa.drawer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import tw.edu.ntubimd.formosa.R;
import tw.edu.ntubimd.formosa.taiwan.taipei.MemberTaipeiActivityClick;
import tw.edu.ntubimd.formosa.taiwan.taipei.TaipeiActivityClick;

public class CollectionActivity extends AppCompatActivity {

    private String idString,attractionID,county,attractionName;
    private ListView collectList;
    private ArrayList<String> attractionIdList = new ArrayList<String>();
    private ArrayList<String> attractionNameList = new ArrayList<String>();
    private ArrayList<String> countyList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        collectList = (ListView) findViewById(R.id.listView);
        File file = new File(getApplication().getFilesDir().getParentFile().getPath() + "/shared_prefs/", "LoginInfo.xml");
        if (file.exists()) {
            ReadValue();
        }

        //region Toolbar設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("口袋名單");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //取消AppTitle
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //endregion

        ArrayList<HashMap<String, Object>> ListData =  showCollectRecord();
        System.out.println(ListData);
        SimpleAdapter ListAdapter = new SimpleAdapter(this,ListData,R.layout.collection_listview_item,new String[]{"Name"},new int[]{R.id.textViewattractionName});
        collectList.setAdapter(ListAdapter);

        collectList.setOnItemClickListener(new CollectionActivity.ListClickHandlerAttraction());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //返回鍵
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class ListClickHandlerAttraction implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
            final File file = new File(getApplication().getFilesDir().getParentFile().getPath() + "/shared_prefs/", "LoginInfo.xml");
            attractionID = attractionIdList.get(position);
            county = countyList.get(position);
            attractionName= attractionNameList.get(position);
            // TODO Auto-generated method stub
            if (county.equals("台北市")) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONArray respJSON = new JSONArray(); // 宣告一個JSONArray
                        String url = "http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=36847f3f-deff-4183-a5bb-800737591de5"; // 宣告一個String存放網址
                        InputStream inputStream = null; // 宣告一個InputStream
                        String data = "";

                        try {
                            HttpClient httpclient = new DefaultHttpClient(); // 宣告一個HttpClient
                            HttpResponse httpResponse = httpclient.execute(new HttpGet(url)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
                            inputStream = httpResponse.getEntity().getContent(); // 用InputStream存放取回的內容
                            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
                            String responseString = readAll(rd); // 宣告一個字串放置讀取BufferedReader後的結果
                            JSONObject OneJSON = new JSONObject(responseString); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                            if (OneJSON.has("result")) { //抓出Infos
                                JSONObject TwoJSON = new JSONObject(OneJSON.get("result").toString());
                                if (TwoJSON.has("results")) { //抓出Info
                                    respJSON = new JSONArray(TwoJSON.get("results").toString());
                                    JSONObject itemJSON = new JSONObject(respJSON.get(position).toString());
                                    data = itemJSON.toString();
                                }
                            }
                            inputStream.close();

                            if (file.exists()) {
                                Intent intent = new Intent(CollectionActivity.this, MemberTaipeiActivityClick.class);
                                intent.putExtra("selected-item", attractionName);
                                intent.putExtra("county", county);
                                intent.putExtra("data", data);
                                startActivity(intent);
                            }else {
                                Intent intent = new Intent(CollectionActivity.this, TaipeiActivityClick.class);
                                intent.putExtra("selected-item", attractionName);
                                intent.putExtra("county", county);
                                intent.putExtra("data", data);
                                startActivity(intent);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (UnsupportedOperationException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            if (county.equals("桃園市")) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONArray respJSON = new JSONArray(); // 宣告一個JSONArray
                        String url = "http://data.tycg.gov.tw/api/v1/rest/datastore/bd906b29-9006-40ed-8bd7-67597c2577fc?format=json"; // 宣告一個String存放網址
                        InputStream inputStream = null; // 宣告一個InputStream
                        String data = "";

                        try {
                            HttpClient httpclient = new DefaultHttpClient(); // 宣告一個HttpClient
                            HttpResponse httpResponse = httpclient.execute(new HttpGet(url)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
                            inputStream = httpResponse.getEntity().getContent(); // 用InputStream存放取回的內容
                            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
                            String responseString = readAll(rd); // 宣告一個字串放置讀取BufferedReader後的結果
                            JSONObject OneJSON = new JSONObject(responseString); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                            if (OneJSON.has("result")) { //抓出Infos
                                JSONObject TwoJSON = new JSONObject(OneJSON.get("result").toString());
                                if (TwoJSON.has("records")) { //抓出Info
                                    respJSON = new JSONArray(TwoJSON.get("records").toString());
                                    JSONObject itemJSON = new JSONObject(respJSON.get(position).toString());
                                    data = itemJSON.toString();
                                }
                            }
                            inputStream.close();

                            if (file.exists()) {
                                Intent intent = new Intent(CollectionActivity.this, MemberTaipeiActivityClick.class);
                                intent.putExtra("selected-item", attractionName);
                                intent.putExtra("county", county);
                                intent.putExtra("data", data);
                                startActivity(intent);
                            }else {
                                Intent intent = new Intent(CollectionActivity.this, TaipeiActivityClick.class);
                                intent.putExtra("selected-item", attractionName);
                                intent.putExtra("county", county);
                                intent.putExtra("data", data);
                                startActivity(intent);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (UnsupportedOperationException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

    public ArrayList<HashMap<String, Object>> showCollectRecord() {

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
                    String result = EntityUtils.toString(resEntity, "UTF-8");

                    System.out.println(result);
                    JSONObject reservationJson = new JSONObject(result);
                    String tmp = reservationJson.get("statuscode").toString();
                    System.out.println(tmp);

                    if (tmp.equals("0")) {
                        JSONArray collectsJSON = new JSONArray(reservationJson.get("Collections").toString());
                        if (collectsJSON.length() > 0) {
                            for (int i = 0; i < collectsJSON.length(); i++) {
                                JSONObject collectJSON = new JSONObject(collectsJSON.get(i).toString());
                                if (collectJSON.has("collectionID")) {
                                    attractionIdList.add(collectJSON.get("attractionID").toString());
                                    attractionNameList.add(collectJSON.get("attractionName").toString());
                                    countyList.add(collectJSON.get("county").toString());
                                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                                    hashMap.put("Name",collectJSON.get("attractionName"));
                                    Data.add(hashMap);
                                }
                            }
                        }
                    }else {
                        Toast toast = Toast.makeText(CollectionActivity.this, "發生失敗請重試", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                DataReturn.addAll(Data);
                return Data;
            }
        };
        try {
            FutureTask<ArrayList<HashMap<String, Object>>> future = new FutureTask<ArrayList<HashMap<String,Object>>>(callable);
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
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {

            sb.append((char) cp);
        }
        return sb.toString();
    }
}
