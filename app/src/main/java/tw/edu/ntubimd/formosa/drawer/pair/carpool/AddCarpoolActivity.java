package tw.edu.ntubimd.formosa.drawer.pair.carpool;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.io.File;

import tw.edu.ntubimd.formosa.R;

public class AddCarpoolActivity extends AppCompatActivity {

    private String userID, idString, travePairID;
    private EditText editTextUserName, editTextUserEmail, editTextLineID, editTextPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_carpool);

        File file = new File(getApplication().getFilesDir().getParentFile().getPath() + "/shared_prefs/", "LoginInfo.xml");
        if (file.exists()) {
            ReadValue();
        }

        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");
        travePairID = intent.getStringExtra("travePairID");

        editTextUserName = (EditText) findViewById(R.id.editTextUserName);
        editTextUserEmail = (EditText) findViewById(R.id.editTextUserEmail);
        editTextLineID = (EditText) findViewById(R.id.editTextLineID);
        editTextPhone = (EditText) findViewById(R.id.editTextPhone);
        Button buttonPair = (Button) findViewById(R.id.buttonPair);

        //region Toolbar設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("加入Pair");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //取消AppTitle
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //endregion

        buttonPair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextUserName.getText().toString().equals("") || editTextUserEmail.getText().toString().equals("")) {
                    Toast toast = Toast.makeText(AddCarpoolActivity.this, "有欄位未填寫", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(AddCarpoolActivity.this);
                    dialog.setMessage("確定要配對嗎");
                    dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            addTravelPairUserInfo();
                        }
                    });
                    dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    dialog.show();
                }
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

    private void addTravelPairUserInfo() {
        final String url = "http://140.131.114.161:8080/Formosa/rest/travelPairUserInfo/addTravelPairUserInfo";

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

                            parameter.accumulate("travelPairID", travePairID);
                            parameter.accumulate("userID", userID);
                            parameter.accumulate("pairUserID", idString);
                            parameter.accumulate("pairUserName", editTextUserName.getText().toString());
                            parameter.accumulate("pairUserEMail", editTextUserEmail.getText().toString());
                            parameter.accumulate("pairUserLine", editTextLineID.getText().toString());
                            parameter.accumulate("pairUserPhone", editTextPhone.getText().toString());


                            String json = parameter.toString();
                            StringEntity se = new StringEntity(json, "UTF-8");
                            httpRequst.setEntity(se);
                            httpRequst.addHeader("Content-Type", "application/json");
                            HttpResponse responsePOST = httpclient.execute(httpRequst);
                            HttpEntity resEntity = responsePOST.getEntity();
                            String result = EntityUtils.toString(resEntity);

                            JSONObject registerJson = new JSONObject(result);
                            String tmp = registerJson.get("statuscode").toString();

                            if (tmp.equals("0")) {
                                Toast toast = Toast.makeText(AddCarpoolActivity.this, "成功", Toast.LENGTH_SHORT);
                                toast.show();
                                finish();
                            } else if (tmp.equals("40")) {
                                Toast toast = Toast.makeText(AddCarpoolActivity.this, "已經配對過囉", Toast.LENGTH_SHORT);
                                toast.show();
                            } else {
                                Toast toast = Toast.makeText(AddCarpoolActivity.this, "失敗請重試", Toast.LENGTH_SHORT);
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

    public void ReadValue() {
        SharedPreferences setting = getSharedPreferences("LoginInfo", 0);
        idString = setting.getString("Id", "");
    }
}
