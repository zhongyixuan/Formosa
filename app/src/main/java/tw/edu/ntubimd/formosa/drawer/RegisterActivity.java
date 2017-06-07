package tw.edu.ntubimd.formosa.drawer;

import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import tw.edu.ntubimd.formosa.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextEMail, editTextName, editTextAccount, editTextPassword, editTextCheckPassword;
    private Button registerButton, haveAccount;
    private LoginButton facebookloginButton;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextEMail = (EditText) findViewById(R.id.editTextEMail);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextAccount = (EditText) findViewById(R.id.editTextAccount);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextCheckPassword = (EditText) findViewById(R.id.editTextCheckPassword);
        registerButton = (Button) findViewById(R.id.registerButton);
        facebookloginButton = (LoginButton) findViewById(R.id.facebookloginButton);
        haveAccount = (Button) findViewById(R.id.haveAccount);
        callbackManager = CallbackManager.Factory.create();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((editTextAccount.getText().toString().length() != 0) && (editTextPassword.getText().toString().length() != 0) &&
                        (editTextCheckPassword.getText().toString().length() != 0) && (editTextName.getText().toString().length() != 0) &&
                        (editTextEMail.getText().toString().length() != 0)) {
                    if (editTextPassword.getText().toString().equals(editTextCheckPassword.getText().toString())) {
                        Register();
                    } else {
                        Toast toast = Toast.makeText(RegisterActivity.this, "密碼確認錯誤", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } else {
                    Toast toast = Toast.makeText(RegisterActivity.this, "有欄位未填寫", Toast.LENGTH_SHORT);
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

        haveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void Register() {
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

        final String url = "http://140.131.114.161:8080/Formosa/rest/user/checkUserAccount";

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

                            parameter.accumulate("userAccount", editTextAccount.getText().toString());

                            String json = parameter.toString();
                            StringEntity se = new StringEntity(json);
                            httpRequst.setEntity(se);
                            httpRequst.addHeader("Content-Type", "application/json");
                            HttpResponse responsePOST = httpclient.execute(httpRequst);
                            HttpEntity resEntity = responsePOST.getEntity();
                            String result = EntityUtils.toString(resEntity);

                            JSONObject registerJson = new JSONObject(result);
                            String tmp = registerJson.get("statuscode").toString();

                            if (tmp.equals("0")) {
                                CreateUser();
                            } else {
                                Toast toast = Toast.makeText(RegisterActivity.this, "帳號重複", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                            System.out.println(tmp);
                            System.out.println(result);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

    private void CreateUser() {
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String url = "http://140.131.114.161:8080/Formosa/rest/user/addUser";
                        try {
                            HttpClient httpclient = new DefaultHttpClient();
                            HttpPost httpRequst = new HttpPost(url);

                            JSONObject parameter = new JSONObject();

                            parameter.accumulate("userName", editTextName.getText().toString());
                            parameter.accumulate("userAccount", editTextAccount.getText().toString());
                            parameter.accumulate("userPassword", editTextPassword.getText().toString());
                            parameter.accumulate("userEMail", editTextEMail.getText().toString());

                            String json = parameter.toString();

                            StringEntity se = new StringEntity(json);
                            httpRequst.setEntity(se);
                            httpRequst.addHeader("Content-Type", "application/json");

                            HttpResponse responsePOST = httpclient.execute(httpRequst);

                            HttpEntity resEntity = responsePOST.getEntity();
                            String result = EntityUtils.toString(resEntity);

                            JSONObject registerJson = new JSONObject(result);
                            String tmp = registerJson.get("statuscode").toString();

                            if (tmp.equals("0")) {
                                Toast toast = Toast.makeText(RegisterActivity.this, "註冊成功！", Toast.LENGTH_SHORT);
                                toast.show();
                                Intent intent = new Intent();
                                intent.setClass(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                                RegisterActivity.this.finish();
                            } else {
                                Toast toast = Toast.makeText(RegisterActivity.this, "發生錯誤請重試", Toast.LENGTH_SHORT);
                                toast.show();
                            }

                            System.out.println(tmp);
                            System.out.println(result);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

    private void FacebookLoging() {
    }
}
