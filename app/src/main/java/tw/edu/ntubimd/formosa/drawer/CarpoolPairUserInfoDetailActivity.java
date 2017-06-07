package tw.edu.ntubimd.formosa.drawer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;

import tw.edu.ntubimd.formosa.R;

public class CarpoolPairUserInfoDetailActivity extends AppCompatActivity {

    private String travePairID;
    private TextView textViewUserName, textViewUserEmail, textViewUserLine, textViewUserPhone;
    private LinearLayout linearLayoutLine, linearLayoutPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carpool_pair_user_info_detail);

        Intent intent = getIntent();
        travePairID = intent.getStringExtra("travePairID");

        textViewUserName = (TextView) findViewById(R.id.textViewUserName);
        textViewUserEmail = (TextView) findViewById(R.id.textViewUserEmail);
        textViewUserLine = (TextView) findViewById(R.id.textViewUserLine);
        textViewUserPhone = (TextView) findViewById(R.id.textViewUserPhone);

        linearLayoutLine = (LinearLayout) findViewById(R.id.linearLayoutLine);
        linearLayoutPhone = (LinearLayout) findViewById(R.id.linearLayoutPhone);

        Button buttonPair = (Button) findViewById(R.id.buttonPair);

        //region Toolbar設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("Pair通知");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //取消AppTitle
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //endregion

        getPairUserInfo();

        buttonPair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sureTravelPair();
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

    private void getPairUserInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String url = "http://140.131.114.161:8080/Formosa/rest/travelPairUserInfo/getTravelPairUserInfoById";

                        try {
                            HttpClient httpclient = new DefaultHttpClient();
                            HttpPost httpRequst = new HttpPost(url);
                            JSONObject parameter = new JSONObject();

                            parameter.accumulate("travelPairID", travePairID);

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
                                    if (resultJson.has("TravelPairUserInfo")) {
                                        JSONObject travelPairJSON = new JSONObject(resultJson.get("TravelPairUserInfo").toString());

                                        textViewUserName.setText(travelPairJSON.get("pairUserName").toString());
                                        textViewUserEmail.setText(travelPairJSON.get("pairUserEMail").toString());
                                        if (!travelPairJSON.get("pairUserLine").toString().equals("")){
                                            linearLayoutLine.setVisibility(View.VISIBLE);
                                            textViewUserLine.setText(travelPairJSON.get("pairUserLine").toString());
                                        }
                                        if (!travelPairJSON.get("pairUserPhone").toString().equals("")){
                                            linearLayoutPhone.setVisibility(View.VISIBLE);
                                            textViewUserPhone.setText(travelPairJSON.get("pairUserPhone").toString());
                                        }
                                    }
                                }
                            } else {
                                Toast toast = Toast.makeText(CarpoolPairUserInfoDetailActivity.this, "發生失敗請重試", Toast.LENGTH_SHORT);
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

    private void sureTravelPair(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String url = "http://140.131.114.161:8080/Formosa/rest/travelPair/alreadyTravelPair";

                        try {
                            HttpClient httpclient = new DefaultHttpClient();
                            HttpPost httpRequst = new HttpPost(url);
                            JSONObject parameter = new JSONObject();

                            parameter.accumulate("travelPairID", travePairID);
                            parameter.accumulate("paired", "true");

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
                                sureTravelPairUserInfo();
                            } else {
                                Toast toast = Toast.makeText(CarpoolPairUserInfoDetailActivity.this, "發生失敗請重試", Toast.LENGTH_SHORT);
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

    private void sureTravelPairUserInfo(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String url = "http://140.131.114.161:8080/Formosa/rest/travelPairUserInfo/alreadySure";

                        try {
                            HttpClient httpclient = new DefaultHttpClient();
                            HttpPost httpRequst = new HttpPost(url);
                            JSONObject parameter = new JSONObject();

                            parameter.accumulate("travelPairID", travePairID);
                            parameter.accumulate("userSure", "true");

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
                                Toast toast = Toast.makeText(CarpoolPairUserInfoDetailActivity.this, "成功", Toast.LENGTH_SHORT);
                                toast.show();
                                finish();
                            } else {
                                Toast toast = Toast.makeText(CarpoolPairUserInfoDetailActivity.this, "發生失敗請重試", Toast.LENGTH_SHORT);
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
}
