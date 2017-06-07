package tw.edu.ntubimd.formosa.drawer.travel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import tw.edu.ntubimd.formosa.R;

public class SelectActivity extends AppCompatActivity {

    private TextView travelName, travelDate, travelCountry;
    private Button nextDayButton, okButton;
    private ListView mListView;
    ListViewAdapter listItemAdapter;
    String days;
    private View moreView;
    private int lastItem;
    private int totalcount;
    private int count;
    List<HashMap<String, Object>> ListData = new ArrayList<HashMap<String, Object>>(); // 宣告一個List放置ListView要放的資料
    private BroadcastReceiver broadcast_reciever = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        travelName = (TextView) findViewById(R.id.travelNameTextView);
        travelDate = (TextView) findViewById(R.id.dayTextView);
        travelCountry = (TextView) findViewById(R.id.countryTextView);
        moreView = getLayoutInflater().inflate(R.layout.list_moreview, null);

        mListView = (ListView) findViewById(R.id.listView);

        nextDayButton = (Button) findViewById(R.id.nextDayButton);
        okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(listener);

        Intent intent = getIntent();
        String newTravelNameText = intent.getStringExtra("selected-item");
        String countryName = intent.getStringExtra("selected-item2");
        String dates = intent.getStringExtra("selected-item3");
        days = intent.getStringExtra("selected-item4");
        travelName.setText(newTravelNameText);
        travelDate.setText(dates);
        travelCountry.setText(countryName);

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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        new attractionAsyncTask().execute();

        super.onWindowFocusChanged(hasFocus);

    }

    //region Taipei.Attraction() 傳回台北Attraction資料
    class attractionAsyncTask extends AsyncTask<String, Integer, Integer> {
        @Override
        protected Integer doInBackground(String... param) {

            JSONArray respJSON = new JSONArray(); // 宣告一個JSONArray
            JSONArray attJSON = new JSONArray(); // 宣告一個JSONArray存放respJSON處理後的資料
            String url = "http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=36847f3f-deff-4183-a5bb-800737591de5"; // 宣告一個String存放網址
            InputStream inputStream = null; // 宣告一個InputStream

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
                        totalcount = respJSON.length();
                        for (int i = 0; i < 10; i++) { // 用for迴圈判斷處理respJSON的資料
                            JSONObject tmpJSON = new JSONObject(respJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                            attJSON.put(tmpJSON); // 將JSONObject放回JSONArray方便處理資料(JSONArray才可判斷長度)

                            if (tmpJSON.has("stitle")) {
                                HashMap<String, Object> hashMap = new HashMap<String, Object>(); // 宣告一個HashMap放置List資料

                                hashMap.put("Name", tmpJSON.getString("stitle")); // 將name的資料放進HashMap裡
                                hashMap.put("Description", tmpJSON.getString("xbody"));// 將avgrank的資料放進HashMap裡
                                hashMap.put("Select", false);
                                ListData.add(hashMap);
                            }
                        }
                    }
                }
                count = ListData.size();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UnsupportedOperationException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            listItemAdapter = new ListViewAdapter(SelectActivity.this, ListData, R.layout.select_attractionlistview, new String[]{"Name", "Description", "Select"}, new int[]{R.id.textViewTitle, R.id.descriptionTextView, R.id.addCheckBox});

            mListView.addFooterView(moreView);
            mListView.setAdapter(listItemAdapter); // ListView設置Adapter
            mListView.setOnScrollListener(new AbsListView.OnScrollListener() { // list Scroll事件
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (lastItem == count && scrollState == this.SCROLL_STATE_IDLE) {
                        moreView.setVisibility(view.VISIBLE);

                        new attractionMoreAsyncTask().execute();
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    lastItem = firstVisibleItem + visibleItemCount - 1;  //減1是因為上面加了個addFooterView
                }
            });
        }
    }
    //endregion

    //region Taipei.Attraction() 傳回台北Attraction更多資料
    class attractionMoreAsyncTask extends AsyncTask<String, Integer, Integer> {
        @Override
        protected Integer doInBackground(String... param) {

            JSONArray respJSON = new JSONArray(); // 宣告一個JSONArray
            JSONArray attJSON = new JSONArray(); // 宣告一個JSONArray存放respJSON處理後的資料
            String url = "http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=36847f3f-deff-4183-a5bb-800737591de5"; // 宣告一個String存放網址
            InputStream inputStream = null; // 宣告一個InputStream

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
                        if (count + 10 > totalcount) {
                            for (int i = count; i < totalcount; i++) { // 用for迴圈判斷處理respJSON的資料
                                JSONObject tmpJSON = new JSONObject(respJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                                attJSON.put(tmpJSON); // 將JSONObject放回JSONArray方便處理資料(JSONArray才可判斷長度)

                                if (tmpJSON.has("stitle")) {
                                    HashMap<String, Object> hashMap = new HashMap<String, Object>(); // 宣告一個HashMap放置List資料

                                    hashMap.put("Name", tmpJSON.getString("stitle")); // 將name的資料放進HashMap裡
                                    hashMap.put("Description", tmpJSON.getString("xbody"));// 將avgrank的資料放進HashMap裡
                                    hashMap.put("Select", false);
                                    ListData.add(hashMap);
                                }
                            }
                        } else {
                            for (int i = count; i < count + 10; i++) { // 用for迴圈判斷處理respJSON的資料
                                JSONObject tmpJSON = new JSONObject(respJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                                attJSON.put(tmpJSON); // 將JSONObject放回JSONArray方便處理資料(JSONArray才可判斷長度)

                                if (tmpJSON.has("stitle")) {
                                    HashMap<String, Object> hashMap = new HashMap<String, Object>(); // 宣告一個HashMap放置List資料

                                    hashMap.put("Name", tmpJSON.getString("stitle")); // 將name的資料放進HashMap裡
                                    hashMap.put("Description", tmpJSON.getString("xbody"));// 將avgrank的資料放進HashMap裡
                                    hashMap.put("Select", false);
                                    ListData.add(hashMap);
                                }
                            }
                        }
                    }
                }
                count = ListData.size();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UnsupportedOperationException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            listItemAdapter.notifyDataSetChanged();
            moreView.setVisibility(View.INVISIBLE);

            if (count == totalcount) {
                Toast.makeText(SelectActivity.this, "已經沒有了", Toast.LENGTH_SHORT).show();
                mListView.removeFooterView(moreView); //移除底部視圖
            }
        }
    }
    //endregion

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            HashMap<Integer, Boolean> state = listItemAdapter.state;
            String options = "選擇的是:";
            String attractionname = null;

            ArrayList<String> Data = new ArrayList<String>();

            for (int j = 0; j < listItemAdapter.getCount(); j++) {
                System.out.println("state.get(" + j + ")==" + state.get(j));
                if (state.get(j) != null) {
                    @SuppressWarnings("unchecked")
                    HashMap<String, Object> map = (HashMap<String, Object>) listItemAdapter.getItem(j);
                    attractionname = map.get("Name").toString();
                    options += "\n" + attractionname;
                    Data.add(attractionname);
                }
            }
            for (int k = 0; k < Data.size(); k++) {
                System.out.println(Data.get(k));
            }
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
            Toast.makeText(getApplicationContext(), options, Toast.LENGTH_LONG).show();
            String newTravelNameText = travelName.getText().toString();
            String dates = travelDate.getText().toString();
            String country = travelCountry.getText().toString();
            Intent intent = new Intent(SelectActivity.this, TimeActivity.class);
            Bundle b = new Bundle();
            intent.putExtra("selected-item", newTravelNameText);
            intent.putExtra("selected-item2", dates);
            intent.putExtra("selected-item3", country);
            intent.putExtra("selected-item4", days);
            b.putStringArrayList("selectedItems", Data);
            intent.putExtras(b);
            startActivity(intent);
        }

    };

    public static ArrayList<HashMap<String, Object>> Attraction() { // Att()方法傳回值為List格式

        final ArrayList<HashMap<String, Object>> DataReturn = new ArrayList<HashMap<String, Object>>();

        Callable<ArrayList<HashMap<String, Object>>> callable = new Callable<ArrayList<HashMap<String, Object>>>() {
            public ArrayList<HashMap<String, Object>> call() throws Exception {
                // TODO code application logic here
                JSONArray respJSON = new JSONArray(); // 宣告一個JSONArray
                JSONArray attJSON = new JSONArray(); // 宣告一個JSONArray存放respJSON處理後的資料
                String url = "http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=36847f3f-deff-4183-a5bb-800737591de5"; // 宣告一個String存放網址
                InputStream inputStream = null; // 宣告一個InputStream
                String[] taipeizipcode = {"100", "103", "104", "105", "106", "108", "110", "111", "112", "114", "115", "116"};
                ArrayList<HashMap<String, Object>> Data = new ArrayList<HashMap<String, Object>>();

                try {
                    HttpClient httpclient = new DefaultHttpClient(); // 宣告一個HttpClient
                    HttpResponse httpResponse = httpclient.execute(new HttpGet(url)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
                    inputStream = httpResponse.getEntity().getContent(); // 用InputStream存放取回的內容
                    int resCode = httpResponse.getStatusLine().getStatusCode(); // 宣告一個int去取得網頁執行的回應碼
                    BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
                    String responseString = readAll(rd); // 宣告一個字串放置讀取BufferedReader後的結果
                    JSONObject OneJSON = new JSONObject(responseString); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                    StringBuffer attractionStr = new StringBuffer();
                    if (OneJSON.has("result")) { //抓出Infos
                        JSONObject TwoJSON = new JSONObject(OneJSON.get("result").toString());
                        if (TwoJSON.has("results")) { //抓出Info
                            respJSON = new JSONArray(TwoJSON.get("results").toString());
                            for (int i = 0; i < 20; i++) { // 用for迴圈判斷處理respJSON的資料
                                JSONObject tmpJSON = new JSONObject(respJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                                attJSON.put(tmpJSON); // 將JSONObject放回JSONArray方便處理資料(JSONArray才可判斷長度)
                                HashMap<String, Object> hashMap = new HashMap<String, Object>(); // 宣告一個HashMap放置List資料
                                hashMap.put("Name", tmpJSON.getString("stitle")); // 將name的資料放進HashMap裡
                                hashMap.put("Description", tmpJSON.getString("xbody"));// 將avgrank的資料放進HashMap裡
                                hashMap.put("Select", false);
                                Data.add(hashMap); // 把HashMap加到List裡

//

//                            if (attJSON.length() > 0) { // 如果JSONArray有資料才做
//                                for (int i = 0; i < attJSON.length(); i++) { // 用for迴圈判斷處理attJSON的資料
//                                    JSONObject tmpJSON = new JSONObject(attJSON.get(i).toString()); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)

//                                            if (tmpJSON.has("Picture1")) { // 如果JSONArray裡有pictures的資料
//                                                String imageUrl = tmpJSON.get("Picture1").toString(); // Picture1內的url把轉成字串放在imageUrl裡
//                                                if (imageUrl == "") {
//                                                    hashMap.put("Pictures", "no picture");
//                                                    System.out.print(imageUrl);
//                                                } else {
////                                        imageUrl.replace('\\','\0');
////                                        System.out.print(imageUrl);
////                                        InputStream inputStreamImage = null; // 用InputStream存放取回的內容
////                                        HttpClient httpclientImage = new DefaultHttpClient(); // 宣告一個HttpClient
////                                        HttpResponse httpResponseImage = httpclientImage.execute(new HttpGet(imageUrl)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟imageUrl，並且執行HttpClient
////                                        HttpEntity entity = httpResponseImage.getEntity(); // 宣告一個HttpEntity取得HttpResponse的實體
////                                        BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(entity); // 宣告一個BufferedHttpEntity取得HttpEntity的實體
////                                        inputStreamImage = bufferedHttpEntity.getContent(); // 用InputStream讀取實體內容
////                                        inputStreamImage.close(); // 關閉InputStream
////                                        hashMap.put("Pictures", decodeBitmapFromInputStream(inputStreamImage, 300, 100)); // 將url裡的圖片放進HashMap裡，並使用decodeBitmapFromInputStream調節圖片大小
//                                                    hashMap.put("Pictures", "no picture");
//                                                }
//                                            }
//                                            HashMap<String, Object> hashMap = new HashMap<String, Object>(); // 宣告一個HashMap放置List資料
//                                            hashMap.put("Name", tmpJSON.getString("stitle")); // 將name的資料放進HashMap裡
//                                            hashMap.put("Description", tmpJSON.getString("xbody"));// 將avgrank的資料放進HashMap裡
//                                            hashMap.put("Select",false);
//                                            Data.add(hashMap); // 把HashMap加到List裡

                                attractionStr.append(tmpJSON.toString() + ", ");

                            }

//                                }
//                            }
                        }
                    }
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (UnsupportedOperationException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                DataReturn.addAll(Data);
                return Data;
            }
        };
        try {
            FutureTask<ArrayList<HashMap<String, Object>>> future = new FutureTask<ArrayList<HashMap<String, Object>>>(callable);
            new Thread(future).start();
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return DataReturn;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {

            sb.append((char) cp);
        }
        return sb.toString();
    }

    class ListViewAdapter extends SimpleAdapter {
        TextView attractionName;
        TextView Description;
        CheckBox checkBox;

        HashMap<Integer, Boolean> state = new HashMap<Integer, Boolean>();
        private List<? extends Map<String, ?>> mArrayList;
        private int resource;
        private LayoutInflater mLayoutInflater;
        private ListViewAdapter viewHolder = null;

        public ListViewAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            this.mArrayList = data;
            this.resource = resource;
            mLayoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            if (mArrayList == null) {
                return 0;
            } else {
                return mArrayList.size();
            }
        }

        @Override
        public Object getItem(int position) {
            if (mArrayList == null) {
                return null;
            } else {
                return mArrayList.get(position);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(resource, null);
                attractionName = (TextView) convertView.findViewById(R.id.textViewTitle);
                attractionName.setText((String) (mArrayList.get(position).get("Name")));
                Description = (TextView) convertView.findViewById(R.id.descriptionTextView);
                Description.setText((String) (mArrayList.get(position).get("Description")));
                checkBox = (CheckBox) convertView.findViewById(R.id.addCheckBox);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        // TODO Auto-generated method stub
                        if (isChecked) {
                            state.put(position, isChecked);
                        } else {
                            state.remove(position);
                        }
                    }
                });
                convertView.setTag(viewHolder);
                checkBox.setChecked((state.get(position) == null ? false : true));

            } else {
                viewHolder = (ListViewAdapter) convertView.getTag();
            }
            return convertView;
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
