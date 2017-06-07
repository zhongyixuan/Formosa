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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import tw.edu.ntubimd.formosa.R;

public class NowPair extends Fragment {
    private ListView list;
    private String pairID, idString;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ArrayList<String> pairIdList = new ArrayList<String>();
    List<HashMap<String, Object>> ListData = new ArrayList<HashMap<String, Object>>();

    public NowPair() {
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

        getNowPairID();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), NowPairDetailActivity.class);
                pairID = pairIdList.get(position);
                intent.putExtra("pairID", pairID);
//                Toast toast = Toast.makeText(getActivity(), pairID, Toast.LENGTH_SHORT);
//                toast.show();
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
                        getNowPairID();
                        System.out.println("滑動更新中");
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

    public void getNowPairID() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        ArrayList<String> pairIdArray = new ArrayList<String>();

                        String url = "http://140.131.114.161:8080/Formosa/rest/pairTracing/getPairID";
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

                            System.out.println(result);

                            JSONObject resultJson = new JSONObject(result);

                            String tmp = resultJson.get("statuscode").toString();

                            if (tmp.equals("0")) {
                                if (resultJson.length() > 0) {
                                    JSONArray pairsJSON = new JSONArray(resultJson.get("PairID").toString());
                                    if (pairsJSON.length() > 0) {
                                        for (int i = 0; i < pairsJSON.length(); i++) {
                                            pairIdArray.add(pairsJSON.get(i).toString());
                                        }
                                        getNowPairThread(pairIdArray);
                                    }
                                }


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

    public void getNowPairThread(ArrayList<String> id) {
        final ArrayList<String> userPairID = id;
        final List<HashMap<String, Object>> Data = new ArrayList<HashMap<String, Object>>();
        final HashMap<String, Object> hashMap = new HashMap<String, Object>();
        final StringBuffer pairsString = new StringBuffer();

        new Thread(new Runnable() {
            @Override
            public void run() {

                for (int i = 0; i < userPairID.size(); i++) {
                    String url = "http://140.131.114.161:8080/Formosa/rest/pair/getPairByPairId";
                    try {
                        HttpClient httpclient = new DefaultHttpClient();
                        HttpPost httpRequst = new HttpPost(url);
                        JSONObject parameter = new JSONObject();

                        parameter.accumulate("pairID", userPairID.get(i).toString());

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
                                if (resultJson.has("Pair")) {
                                    JSONObject pairJSON = new JSONObject(resultJson.get("Pair").toString());

                                    if (pairJSON.get("paired").toString().equals("true")) {
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                        Date date = sdf.parse(pairJSON.get("pairTime").toString());
                                        Calendar c = Calendar.getInstance();
                                        c.setTime(date);

                                        String s[] = pairJSON.get("waitTime").toString().split(":");
                                        String h = s[0];
                                        String m = s[1];
                                        c.add(Calendar.HOUR, Integer.parseInt(h));
                                        c.add(Calendar.MINUTE, Integer.parseInt(m));
                                        Calendar now = Calendar.getInstance();
                                        Date dateTest = c.getTime();

                                        if (now.before(c)) {

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

                        } else {
                            Toast toast = Toast.makeText(getActivity(), "發生失敗請重試", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ListData = Data;
                        SimpleAdapter ListAdapter = new SimpleAdapter(getActivity(), ListData, R.layout.pair_list_item, // 宣告一個SimpleAdapter五個參數分別為(List位置,
                                // List資料來源,
                                // ListView介面檔,
                                // List裡資料的名稱,
                                // List裡資料的介面檔中id)
                                new String[]{"ShopName", "ProductName", "PreferentialType", "PairAddress", "PairTime"}, new int[]{R.id.textViewUserShopName, R.id.textViewUserProductName, R.id.textViewUserPreferentialType, R.id.textViewUserPairAddress, R.id.textViewUserPairTime});

                        list.setAdapter(ListAdapter);
                    }
                });
            }
        }).start();
    }

    public void ReadValue() {
        SharedPreferences setting = getActivity().getSharedPreferences("LoginInfo", 0);
        idString = setting.getString("Id", "");
    }
}
