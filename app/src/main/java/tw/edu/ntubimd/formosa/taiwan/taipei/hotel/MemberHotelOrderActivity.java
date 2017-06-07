package tw.edu.ntubimd.formosa.taiwan.taipei.hotel;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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
import java.util.Calendar;
import java.util.GregorianCalendar;

import tw.edu.ntubimd.formosa.R;

public class MemberHotelOrderActivity extends AppCompatActivity {

    private EditText editTextCount, editTextOrderName, editTextNumber, editTextHomeAddress, editTextUserNote;
    private TextView TextVewHotelName, TextViewShopAddress;
    private Button buttonOrder, selectDateButton;
    private String idString;
    private DatePickerDialog selectDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_hotel_order);

        File file = new File(getApplication().getFilesDir().getParentFile().getPath() + "/shared_prefs/", "LoginInfo.xml");
        if (file.exists()) {
            ReadValue();
        }

        Intent intent = getIntent();
        String HotelName = intent.getStringExtra("ShopName");
        String ShopAddress = intent.getStringExtra("ShopAddress");

        TextVewHotelName = (TextView) findViewById(R.id.TextVewHotelName);
        editTextCount = (EditText) findViewById(R.id.editTextCount);
        editTextOrderName = (EditText) findViewById(R.id.editTextOrderName);
        editTextNumber = (EditText) findViewById(R.id.editTextNumber);
        TextViewShopAddress = (TextView) findViewById(R.id.TextViewShopAddress);
        editTextHomeAddress = (EditText) findViewById(R.id.editTextHomeAddress);
        editTextUserNote = (EditText) findViewById(R.id.editTextUserNote);

        buttonOrder = (Button) findViewById(R.id.buttonOrder);
        selectDateButton = (Button) findViewById(R.id.selectDateButton);
        TextVewHotelName.setText(HotelName);
        TextViewShopAddress.setText(ShopAddress);
        //region Toolbar設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("按我代訂");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //取消AppTitle
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //endregion

        findview();
        GregorianCalendar calendar = new GregorianCalendar();
        selectDate = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                selectDateButton.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));

        buttonOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((TextVewHotelName.getText().toString().length() != 0) && (editTextCount.getText().toString().length() != 0) && (selectDateButton.getText().toString().length() != 0) && (editTextOrderName.getText().toString().length() != 0)
                        && (editTextNumber.getText().toString().length() != 0) && (TextViewShopAddress.getText().toString().length() != 0) && (editTextHomeAddress.getText().toString().length() != 0)) {
                    System.out.println("準備執行代訂");
                    order();
                } else {
                    Toast toast = Toast.makeText(MemberHotelOrderActivity.this, "有欄位未填寫", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    private void order() {

        final String url = "http://140.131.114.161:8080/Formosa/rest/hotelOrder/createHotelOrder";

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

                            System.out.println("執行代訂中");
                            parameter.accumulate("userID", idString);
                            parameter.accumulate("hotelOrderName", editTextOrderName.getText().toString());
                            parameter.accumulate("hotelUserPhone", editTextNumber.getText().toString());
                            parameter.accumulate("hotelUserAddress", editTextHomeAddress.getText().toString());
                            parameter.accumulate("hotelName", TextVewHotelName.getText().toString());
                            parameter.accumulate("hotelAddress", TextViewShopAddress.getText().toString());
                            parameter.accumulate("hotelOrderDate", selectDateButton.getText().toString());
                            parameter.accumulate("hotelOrderCount", editTextCount.getText().toString());
                            parameter.accumulate("hotelUserNote", editTextUserNote.getText().toString());

                            String json = parameter.toString();
                            System.out.println(json);
                            StringEntity se = new StringEntity(json, "UTF-8");
                            httpRequst.setEntity(se);
                            httpRequst.addHeader("Content-Type", "application/json");
                            HttpResponse responsePOST = httpclient.execute(httpRequst);
                            HttpEntity resEntity = responsePOST.getEntity();
                            String result = EntityUtils.toString(resEntity);

                            JSONObject registerJson = new JSONObject(result);
                            String tmp = registerJson.get("statuscode").toString();

                            if (tmp.equals("0")) {
                                Toast toast = Toast.makeText(MemberHotelOrderActivity.this, "成功", Toast.LENGTH_SHORT);
                                toast.show();
                                finish();
                            } else {
                                Toast toast = Toast.makeText(MemberHotelOrderActivity.this, "失敗", Toast.LENGTH_SHORT);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //返回鍵
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void findview() {
        selectDateButton = (Button) findViewById(R.id.selectDateButton);
    }

    public void setDate(View v) {
        selectDate.show();
    }

    public void ReadValue() {
        SharedPreferences setting = getSharedPreferences("LoginInfo", 0);
        idString = setting.getString("Id", "");
    }
}
