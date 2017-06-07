package tw.edu.ntubimd.formosa.drawer.pair;

import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
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

import tw.edu.ntubimd.formosa.R;

public class MyPairDetailActivity extends AppCompatActivity {

    private TextView textViewUserPairAddress, textViewUserShopName, textViewUserProductName, textViewUserProductPrice, textViewUserPreferentialType, textViewUserUserFeature, textViewShowPairTime, textViewUserWaitTime;
    private String pairID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_pair_detail);

        textViewUserPairAddress = (TextView) findViewById(R.id.textViewUserPairAddress);
        textViewUserShopName = (TextView) findViewById(R.id.textViewUserShopName);
        textViewUserProductName = (TextView) findViewById(R.id.textViewUserProductName);
        textViewUserProductPrice = (TextView) findViewById(R.id.textViewUserProductPrice);
        textViewUserPreferentialType = (TextView) findViewById(R.id.textViewUserPreferentialType);
        textViewUserUserFeature = (TextView) findViewById(R.id.textViewUserUserFeature);
        textViewShowPairTime = (TextView) findViewById(R.id.textViewShowPairTime);
        textViewUserWaitTime = (TextView) findViewById(R.id.textViewUserWaitTime);

        //region Toolbar設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("買一送一");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //取消AppTitle
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //endregion

        Intent intent = getIntent();
        pairID = intent.getStringExtra("pairID");

        getPairInfo();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //返回鍵
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getPairInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String url = "http://140.131.114.161:8080/Formosa/rest/pair/getPairByPairId";
                        try {
                            HttpClient httpclient = new DefaultHttpClient();
                            HttpPost httpRequst = new HttpPost(url);
                            JSONObject parameter = new JSONObject();

                            parameter.accumulate("pairID", pairID);
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

                            System.out.printf(tmp);

                            if (tmp.equals("0")) {
                                if (resultJson.length() > 0) {
                                    JSONObject pairJSON = new JSONObject(resultJson.get("Pair").toString());
                                    if (pairJSON.has("pairID")) {
                                        textViewUserPairAddress.setText(pairJSON.get("pairAddress").toString());
                                        textViewUserShopName.setText(pairJSON.get("shopName").toString());
                                        textViewUserProductName.setText(pairJSON.get("productName").toString());
                                        textViewUserProductPrice.setText(pairJSON.get("productPrice").toString() + "元");
                                        textViewUserPreferentialType.setText(pairJSON.get("preferentialType").toString());
                                        textViewUserUserFeature.setText(pairJSON.get("userFeature").toString());
                                        int doc = pairJSON.get("pairTime").toString().indexOf(" ");
                                        String pairTime = pairJSON.get("pairTime").toString().substring(0, doc);
                                        textViewShowPairTime.setText(pairTime);
                                        String waitTime = "";
                                        if (pairJSON.get("waitTime").toString().equals("00:30:00")) {
                                            waitTime = "30分鐘";
                                        } else if (pairJSON.get("waitTime").toString().equals("01:00:00")) {
                                            waitTime = "60分鐘";
                                        } else if (pairJSON.get("waitTime").toString().equals("01:30:00")) {
                                            waitTime = "90分鐘";
                                        }
                                        textViewUserWaitTime.setText(waitTime);
                                    }
                                }
                            } else {
                                Toast toast = Toast.makeText(MyPairDetailActivity.this, "發生失敗請重試", Toast.LENGTH_SHORT);
                                toast.show();
                            }

                            System.out.println(result);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }
}
