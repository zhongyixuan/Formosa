package tw.edu.ntubimd.formosa.drawer.travel;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

import tw.edu.ntubimd.formosa.R;

public class AddTravelActivity extends AppCompatActivity {

    private EditText newTravelName;
    private Button selectOutsetDateButton, confirmButton;
    private Spinner travelDay;    //天數下拉式選單
    private ArrayAdapter<String> lunchListDay;
    private TextView selectDate;
    private DatePickerDialog selectFirstDate;
    final String[] travelday = {"1", "2", "3", "4", "5", "6", "7", "8"};
    private Context tContext;
    int day = 0;
    private BroadcastReceiver broadcast_reciever = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_travel);

        //region Toolbar設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("我的行程");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //取消AppTitle
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //endregion

        findview();
        GregorianCalendar calendar = new GregorianCalendar();
        selectFirstDate = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                selectOutsetDateButton.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
        newTravelName = (EditText) findViewById(R.id.newTravelName);
        tContext = this.getApplicationContext();
        travelDay = (Spinner) findViewById(R.id.selectDaySpinner);
        selectDate = (TextView) findViewById(R.id.selectDate);
        confirmButton = (Button) findViewById(R.id.confirmButton);
        lunchListDay = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, travelday);
        travelDay.setAdapter(lunchListDay);
        travelDay.setOnItemSelectedListener(new OnItemSelectedListenertravelDaytype());

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((newTravelName.getText().toString().length() != 0)) {
                    if (day == 1) {
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

                        String newTravelNameText = newTravelName.getText().toString();
                        String dates = selectOutsetDateButton.getText().toString();
                        Intent intent = new Intent(AddTravelActivity.this, DayCountyActivity.class);
                        intent.putExtra("selected-item", newTravelNameText);
                        intent.putExtra("selected-item2", dates);
                        startActivity(intent);
                    }
                    if (day == 2) {

                    }
                    if (day == 3) {

                    }
                    if (day == 4) {

                    }
                    if (day == 5) {

                    }
                } else {
                    Toast toast = Toast.makeText(AddTravelActivity.this, "有欄位未填寫", Toast.LENGTH_SHORT);
                    toast.show();
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

    public class OnItemSelectedListenertravelDaytype implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
            if (position == 0) {
                selectDate.setText("1天");
                day = 1;
//                Toast.makeText(tContext, "你選的是" + travelday[position], Toast.LENGTH_SHORT).show();
            }
            if (position == 1) {
                selectDate.setText("2天");
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    }

    public void findview() {
        selectOutsetDateButton = (Button) findViewById(R.id.selectOutsetDateButton);
    }

    public void setDate(View v) {
        selectFirstDate.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcast_reciever != null){
            unregisterReceiver(broadcast_reciever);
        }
    }
}