package tw.edu.ntubimd.formosa.drawer.travel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import tw.edu.ntubimd.formosa.R;

public class DayCountyActivity extends AppCompatActivity {

    private TextView travelName,dateText;
    private Button nextButton,addNewButton;
    int count=0;
    String[] dayname={"第一天目的地："};
    final String[] country = {"不限定", "基隆市", "台北市", "新北市", "桃園市", "新竹縣", "新竹市", "苗栗縣"};
    private BroadcastReceiver broadcast_reciever = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_county);

        travelName=(TextView)findViewById(R.id.travelName);
        dateText=(TextView)findViewById(R.id.dateTextView);
        ListView dayCountryListView = (ListView)findViewById(android.R.id.list);
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, country);
//        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final ArrayList<HashMap<String, Object>> dayCountryItem = new ArrayList<>();
        for (int i = 0; i < dayname.length; i++) {
            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("DayName", dayname[i]); // 將name的資料放進HashMap裡
            data.put("CountrySpinner", countryAdapter);// 將avgrank的資料放進HashMap裡
            dayCountryItem.add(data);
        }
        DayCountryAdapter listItemAdapter = new DayCountryAdapter(this, dayCountryItem, R.layout.travel_daylistview,
                new String[] {"DayName","CountrySpinner"},
                new int[] {R.id.daytextView,R.id.dayCountrySpinner}
        );

        dayCountryListView.setAdapter(listItemAdapter);
        nextButton=(Button)findViewById(R.id.nextButton);
        addNewButton=(Button)findViewById(R.id.addDateButton);;

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(count==2){
                    if(dayCountryItem.size()==1){
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

                        String days="1";
                        String newTravelNameText = travelName.getText().toString();
                        String countryName="台北市";
                        String dates= dateText.getText().toString();
                        Intent intent = new Intent(DayCountyActivity.this, SelectActivity.class);
                        intent.putExtra("selected-item", newTravelNameText);
                        intent.putExtra("selected-item2", countryName);
                        intent.putExtra("selected-item3", dates);
                        intent.putExtra("selected-item4",days);
                        startActivity(intent);
                    }
                }
            }
        });
        addNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(count==2){
                    String newTravelNameText = travelName.getText().toString();
                    String countryName="台北市";
                    Intent intent = new Intent(DayCountyActivity.this, SelectActivity.class);
                    intent.putExtra("selected-item", newTravelNameText);
                    intent.putExtra("selected-item2", countryName);
                    startActivity(intent);
                }
            }
        });

        Intent intent = getIntent();
        String newTravelNameText = intent.getStringExtra("selected-item");
        String dates=intent.getStringExtra("selected-item2");
        travelName.setText(newTravelNameText);
        dateText.setText(dates);

        //region Toolbar設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(newTravelNameText);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //取消AppTitle
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //endregion
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //返回鍵
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class DayCountryAdapter extends BaseAdapter {
        private class spinnerViewHolder {
            TextView day;
            Spinner countrySpinner;
        }

        private DayCountryAdapter.spinnerViewHolder holder;
        private ArrayList<HashMap<String, Object>> dayCountryList;
        private String[] keyString;
        private int[] valueViewID;
        private Context cContext;
        private LayoutInflater inflater;

        public DayCountryAdapter(Context c, ArrayList<HashMap<String, Object>> appList, int resource,
                                 String[] from, int[] to){
            dayCountryList = appList;
            cContext = c;
            inflater = (LayoutInflater) cContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            keyString = new String[from.length];
            valueViewID = new int[to.length];
            System.arraycopy(from, 0, keyString, 0, from.length);
            System.arraycopy(to, 0, valueViewID, 0, to.length);
        }
        @Override
        public int getCount() {
            return dayCountryList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView != null) {
                holder = (DayCountryAdapter.spinnerViewHolder) convertView.getTag();
            }else {
                convertView = inflater.inflate(R.layout.travel_daylistview,null);
                holder = new DayCountryAdapter.spinnerViewHolder();
                holder.day=(TextView)convertView.findViewById(valueViewID[0]);
                holder.countrySpinner = (Spinner) convertView.findViewById(valueViewID[1]);
                convertView.setTag(holder);
            }
            HashMap<String, Object> appInfo = dayCountryList.get(position);
            if (appInfo != null) {
                String day = (String) appInfo.get(keyString[0]);
                holder.day.setText(day);
                ArrayAdapter<String> daycountry=(ArrayAdapter) appInfo.get(keyString[1]);
                holder.countrySpinner.setAdapter(daycountry);
                holder.countrySpinner.setOnItemSelectedListener(new OnItemSelectedListenercountrytype());
            }
            return convertView;
        }
    }
    public class OnItemSelectedListenercountrytype implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
            if (position == 0) {

            }
            if (position == 1) {

            }
            if (position == 2) {
                count=2;
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcast_reciever != null){
            unregisterReceiver(broadcast_reciever);
        }
    }
}
