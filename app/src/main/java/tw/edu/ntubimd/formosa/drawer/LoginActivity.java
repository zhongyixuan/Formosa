package tw.edu.ntubimd.formosa.drawer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import tw.edu.ntubimd.formosa.MemberMainActivity;
import tw.edu.ntubimd.formosa.R;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextAccount, editTextPassword;
    private Button loginButton, noAccount;
    private LoginButton facebookloginButton;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextAccount = (EditText) findViewById(R.id.editTextAccount);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        loginButton = (Button) findViewById(R.id.loginButton);
        facebookloginButton = (LoginButton) findViewById(R.id.facebookloginButton);
        noAccount = (Button) findViewById(R.id.noAccount);
        callbackManager = CallbackManager.Factory.create();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextAccount.getText().toString().length() != 0 && editTextPassword.getText().toString().length() != 0) {
                    String account = editTextAccount.getText().toString();
                    String password = editTextPassword.getText().toString();
                    UserLogin(account, password);
                } else {
                    Toast toast = Toast.makeText(LoginActivity.this, "請輸入帳號密碼", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        facebookloginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                FacebookLoging();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        noAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void UserLogin(final String account, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                String url = "http://140.131.114.161:8080/Formosa/rest/user/checkPassword";
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httpRequst = new HttpPost(url);

                    JSONObject parameter = new JSONObject();

                    parameter.accumulate("userAccount", account);
                    parameter.accumulate("userPassword", password);

                    String json = parameter.toString();

                    StringEntity se = new StringEntity(json);
                    httpRequst.setEntity(se);
                    httpRequst.addHeader("Content-Type", "application/json");

                    HttpResponse responsePOST = httpclient.execute(httpRequst);
                    final int statuscode = responsePOST.getStatusLine().getStatusCode();

                    HttpEntity resEntity = responsePOST.getEntity();
                    final String result = EntityUtils.toString(resEntity);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                if (statuscode == 200) {
                                    JSONObject loginJson = new JSONObject(result);
                                    String tmp = loginJson.get("statuscode").toString();

                                    System.out.println(tmp);


                                    if (tmp.equals("0")) {
                                        String userID = loginJson.get("userID").toString();
                                        SharedPreferences setting = getSharedPreferences("LoginInfo", 0);
                                        setting.edit()
                                                .putString("Id", userID)
                                                .putString("Account", account)
                                                .commit();
                                        SendIntent(userID, account);
                                    } else if (tmp.equals("99")) {
                                        System.out.println("密碼錯誤");
                                        Toast toast = Toast.makeText(LoginActivity.this, "密碼錯誤", Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                } else {
                                    Toast toast = Toast.makeText(LoginActivity.this, "帳號密碼錯誤", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });


                    System.out.println(result);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void FacebookLoging() {

    }

    public void SendIntent(String idString, String accountString) {
        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, MemberMainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("Id", idString);
        bundle.putString("Account", accountString);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
        Intent intentFinish = new Intent("finish_activity");
        sendBroadcast(intentFinish);
    }
}
