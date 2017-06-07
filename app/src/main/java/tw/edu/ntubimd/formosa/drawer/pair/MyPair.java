package tw.edu.ntubimd.formosa.drawer.pair;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tw.edu.ntubimd.formosa.R;

public class MyPair extends Fragment {
    private ListView list;
    private ArrayList<String> itemType = new ArrayList<String>();
    private ArrayList<String> pairIdList = new ArrayList<String>();
    private String pairID, idString;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public MyPair() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_my_pair, container, false);

        File file = new File(getActivity().getFilesDir().getParentFile().getPath() + "/shared_prefs/", "LoginInfo.xml");
        if (file.exists()) {
            ReadValue();
        }

        list = (ListView) v.findViewById(R.id.listView);

        showMyPair();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), MyPairDetailActivity.class);
                pairID = pairIdList.get(position);
                intent.putExtra("pairID", pairID); //將參數放入
                Toast toast = Toast.makeText(getActivity(), pairID, Toast.LENGTH_SHORT);
                toast.show();
                startActivity(intent);
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //refresh data
                        showMyPair();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        list.setOnScrollListener(onListScroll);

        return v;
    }

    private AbsListView.OnScrollListener onListScroll = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem == 0) {
                mSwipeRefreshLayout.setEnabled(true);
            } else {
                mSwipeRefreshLayout.setEnabled(false);
            }
        }
    };

    public void showMyPair() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<HashMap<String, Object>> ListData = new ArrayList<HashMap<String, Object>>();
                        List<HashMap<String, Object>> Data = new ArrayList<HashMap<String, Object>>();

                        String url = "http://140.131.114.161:8080/Formosa/rest/pair/getPairByUserId";
                        try {
                            HttpClient httpclient = new DefaultHttpClient();
                            HttpPost httpRequst = new HttpPost(url);
                            JSONObject parameter = new JSONObject();

                            parameter.accumulate("userID", idString);

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
                                    JSONArray pairsJSON = new JSONArray(resultJson.get("Pairs").toString());
                                    if (pairsJSON.length() > 0) {
                                        for (int i = pairsJSON.length() - 1; i >= 0; i--) {
                                            JSONObject pairJSON = new JSONObject(pairsJSON.get(i).toString());

                                            if (pairJSON.has("pairID")) {
                                                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                                                itemType.add(pairJSON.get("paired").toString());
                                                pairIdList.add(pairJSON.get("pairID").toString());
                                                hashMap.put("ShopName", pairJSON.get("shopName"));
                                                hashMap.put("ProductName", pairJSON.get("productName"));
                                                hashMap.put("PreferentialType", pairJSON.get("preferentialType"));
                                                hashMap.put("PairAddress", pairJSON.get("pairAddress"));
                                                int doc = pairJSON.get("pairTime").toString().indexOf(".");
                                                String pairTime = pairJSON.get("pairTime").toString().substring(0, doc);
                                                hashMap.put("PairTime", pairTime);
                                                Data.add(hashMap);
                                            }
                                        }
                                    }
                                }
                                ListData = Data;
                                MyAdapter ListAdapter = new MyAdapter(getActivity(), ListData, R.layout.pair_list_item, // 宣告一個SimpleAdapter五個參數分別為(List位置,
                                        // List資料來源,
                                        // ListView介面檔,
                                        // List裡資料的名稱,
                                        // List裡資料的介面檔中id)
                                        new String[]{"ShopName", "ProductName", "PreferentialType", "PairAddress", "PairTime"}, new int[]{R.id.textViewUserShopName, R.id.textViewUserProductName, R.id.textViewUserPreferentialType, R.id.textViewUserPairAddress, R.id.textViewUserPairTime});

                                list.setAdapter(ListAdapter);

                            } else if(tmp.equals("40")){
                            }else {
                                Toast toast = Toast.makeText(getActivity(), "發生失敗請重試", Toast.LENGTH_SHORT);
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

    public class MyAdapter extends SimpleAdapter {
        private final Context context;
        private final List<? extends Map<String, ?>> data;
        private final int resource;
        private final String[] from;
        private final int[] to;

        public MyAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            this.context = context;
            this.data = data;
            this.resource = resource;
            this.from = from;
            this.to = to;
        }


        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (Boolean.parseBoolean(itemType.get(position))) {
                v = inflater.inflate(R.layout.pair_list_item_paired, parent, false);
            } else {
                v = inflater.inflate(R.layout.pair_list_item, parent, false);
            }

            TextView textViewUserShopName = (TextView) v.findViewById(R.id.textViewUserShopName);
            TextView textViewUserProductName = (TextView) v.findViewById(R.id.textViewUserProductName);
            TextView textViewUserPreferentialType = (TextView) v.findViewById(R.id.textViewUserPreferentialType);
            TextView textViewUserPairAddress = (TextView) v.findViewById(R.id.textViewUserPairAddress);
            TextView textViewUserPairTime = (TextView) v.findViewById(R.id.textViewUserPairTime);

            textViewUserShopName.setText(data.get(position).get("ShopName").toString());
            textViewUserProductName.setText(data.get(position).get("ProductName").toString());
            textViewUserPreferentialType.setText(data.get(position).get("PreferentialType").toString());
            textViewUserPairAddress.setText(data.get(position).get("PairAddress").toString());
            textViewUserPairTime.setText(data.get(position).get("PairTime").toString());
            return v;
        }
    }

    public void ReadValue() {
        SharedPreferences setting = getActivity().getSharedPreferences("LoginInfo", 0);
        idString = setting.getString("Id", "");
    }
}
