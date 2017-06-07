package tw.edu.ntubimd.formosa.drawer.travel;

import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import tw.edu.ntubimd.formosa.R;

public class TimeActivity extends AppCompatActivity {

    private TextView travelName, travelDate,travelCountry;
    private Button nextDayButton, okButton;
    private ListView mListView;
    private HashMap<String, Object> mHashMap;
    private ArrayList<String> TimeList=new ArrayList<String>();

    private ArrayList<HashMap<String, Object>> mArrayList;
    private SelectTimeButtonAdapter.ViewHolder viewHolder;
    SelectTimeButtonAdapter listItemAdapter;
    String days,travelID,date, idString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);

        File file = new File(getApplication().getFilesDir().getParentFile().getPath() + "/shared_prefs/", "LoginInfo.xml");
        if (file.exists()) {
            ReadValue();
        }

        travelName = (TextView) findViewById(R.id.travelNameTextView);
        travelDate = (TextView) findViewById(R.id.travelDayTextView);
        travelCountry = (TextView) findViewById(R.id.travelCountryTextView);

        Bundle b = getIntent().getExtras();
        Intent intent = getIntent();
        String newTravelNameText = intent.getStringExtra("selected-item");
        String dates =intent.getStringExtra("selected-item2");
        String country =intent.getStringExtra("selected-item3");
        days =intent.getStringExtra("selected-item4");
        ArrayList<String> resultArr = new ArrayList<String>();
        resultArr = b.getStringArrayList("selectedItems");
        System.out.println(resultArr);

        for (int i = 0; i < resultArr.size(); i++) {
            System.out.println(resultArr.get(i));
        }
        ArrayList<HashMap<String, Object>> Data = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < resultArr.size(); i++) {
            HashMap<String, Object> result = new HashMap<String, Object>();
            result.put("Name", resultArr.get(i).toString());
            result.put("Delete", R.drawable.travel_delete);
            result.put("SelectTime", R.id.buttonTime);
            result.put("Time",R.drawable.travel_time);
            Data.add(result);
        }
        mArrayList = Data;
        System.out.println("............."+mArrayList);
        mListView = (ListView) findViewById(android.R.id.list);
        listItemAdapter = new SelectTimeButtonAdapter(TimeActivity.this, mArrayList, R.layout.time_attractionlistview, new String[] {"Delete","Time","SelectTime","Name"}, new int[] {R.id.imageDeleteButton,R.id.imageTime,R.id.buttonTime,R.id.attractionName});
        mListView.setAdapter(listItemAdapter);

        travelName.setText(newTravelNameText);
        travelDate.setText(dates);
        travelCountry.setText(country);

        nextDayButton = (Button) findViewById(R.id.nextDayButton);
        okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(listener);

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


    View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String message="確認成功";
            Toast.makeText(getApplicationContext(),message, Toast.LENGTH_LONG).show();
            travel();
            travelAttraction();
            Intent intentFinish = new Intent("finish_activity");
            sendBroadcast(intentFinish);
            Intent intent = new Intent(TimeActivity.this, TravelActivity.class);
            startActivity(intent);
        }
    };

    class SelectTimeButtonAdapter extends BaseAdapter {

        private class ViewHolder {
            TextView attractionName,Time;
            ImageButton buttonDelete;
            Button buttonTime;
            ImageView imageTime;
        }

        private ArrayList<HashMap<String, Object>> ArrayList;

        private int resource;
        private LayoutInflater mLayoutInflater;
        GregorianCalendar calendar = new GregorianCalendar();
        private TimePickerDialog selectTime;
        private Context tContext;
        private String[] keyString;
        private int[] valueViewID;

        public SelectTimeButtonAdapter(Context context, ArrayList<HashMap<String, Object>> data, int resource, String[] from, int[] to) {
            ArrayList = data;
            tContext = context;
            mLayoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            keyString = new String[from.length];
            valueViewID = new int[to.length];
            System.arraycopy(from, 0, keyString, 0, from.length);
            System.arraycopy(to, 0, valueViewID, 0, to.length);
        }

        @Override
        public int getCount() {
            if (ArrayList == null) {
                return 0;
            } else {
                return ArrayList.size();
            }
        }

        @Override
        public Object getItem(int position) {
            if (ArrayList == null) {
                return null;
            } else {
                return ArrayList.get(position);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void removeItem(int position) {
            ArrayList.remove(position);
            this.notifyDataSetChanged();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = mLayoutInflater.inflate(R.layout.time_attractionlistview, null);
                viewHolder.buttonDelete = (ImageButton) convertView.findViewById(R.id.imageDeleteButton);
                viewHolder.attractionName = (TextView) convertView.findViewById(R.id.attractionName);
                viewHolder.buttonTime = (Button) convertView.findViewById(R.id.buttonTime);
                viewHolder.imageTime=(ImageView)convertView.findViewById(R.id.imageTime);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.buttonDelete.setImageResource((Integer)mArrayList.get(position).get("Delete"));
            viewHolder.imageTime.setImageResource((Integer)mArrayList.get(position).get("Time"));
            viewHolder.buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int vid = v.getId();
                    if (vid == viewHolder.buttonDelete.getId())
                        removeItem(position);
                }
            });
            viewHolder.attractionName.setText((String)mArrayList.get(position).get("Name"));
            viewHolder.buttonTime.setId(position);

            viewHolder.buttonTime.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    final Button time =(Button)v;
                    selectTime = new TimePickerDialog(tContext, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            time.setText((hourOfDay > 12 ? hourOfDay : hourOfDay) + ":" + minute + ":" + "00");
                            TimeList.add(time.getText().toString());
                            System.out.println(TimeList);
                        }
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(calendar.MINUTE), false);
                    selectTime.show();
                    notifyDataSetChanged();
                }
//                String timese=viewHolder.buttonTime.getText().toString();

            });

            return convertView;
        }
    }

    private void travel() {
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

        final String url = "http://140.131.114.161:8080/Formosa/rest/travel/addTravel";

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

                            System.out.println("執行行程中");
                            parameter.accumulate("userID", idString);
                            parameter.accumulate("travelName", travelName.getText().toString());
                            parameter.accumulate("travelDate", travelDate.getText().toString());
                            parameter.accumulate("travelDays", days);

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
                                travelID=registerJson.get("travelID").toString();
                                Toast toast = Toast.makeText(TimeActivity.this, "成功", Toast.LENGTH_SHORT);
                                toast.show();
                                TimeActivity.this.finish();
                            } else {
                                Toast toast = Toast.makeText(TimeActivity.this, "失敗", Toast.LENGTH_SHORT);
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
    private void travelAttraction() {
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

        final String url = "http://140.131.114.161:8080/Formosa/rest/travelAttraction/addTravelAttraction";

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            for(int i=0;i<mArrayList.size();i++) {

                                HttpClient httpclient = new DefaultHttpClient();
                                HttpPost httpRequst = new HttpPost(url);
                                JSONObject parameter = new JSONObject();

                                System.out.println("執行我的行程景點輸入中");

                                HashMap<String, Object> resultHash = new HashMap<String, Object>();
                                resultHash = mArrayList.get(i);
                                String attraction = resultHash.get("Name").toString();
                                String time = TimeList.get(i);
                                String datetime = travelDate.getText().toString() + " " + time;
                                parameter.accumulate("travelID", travelID);
                                parameter.accumulate("attractionName", attraction);
                                parameter.accumulate("dayDate", datetime);
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
                                    Toast toast = Toast.makeText(TimeActivity.this, "成功", Toast.LENGTH_SHORT);
                                    toast.show();
                                    TimeActivity.this.finish();
                                }  else {
                                    Toast toast = Toast.makeText(TimeActivity.this, "失敗", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                                System.out.println(tmp);
                                System.out.println(result);
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
