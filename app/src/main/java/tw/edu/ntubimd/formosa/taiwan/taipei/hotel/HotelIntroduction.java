package tw.edu.ntubimd.formosa.taiwan.taipei.hotel;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tw.edu.ntubimd.formosa.R;

public class HotelIntroduction extends Fragment {

    private String item, data, ShopAddress;
    private LinearLayout descriptionLayout;
    private TextView descripition;
    private ImageView imageViewExpand;
    private ProgressBar progressBar, progressBarPixnet;
    private ListView listPixnet;
    final int MAX_DESCRIP_LINE = 6;
    pixnetAsyncTask pixnetAsyncTask = new pixnetAsyncTask();

    public HotelIntroduction() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_hotel_introduction, container, false);

        //region 取得Activity資料
        TextView textView = (TextView) getActivity().findViewById(R.id.toolbar_title);
        TextView text = (TextView) getActivity().findViewById(R.id.textViewTmp);
        data = text.getText().toString();
        item = textView.getText().toString();
        //endregion

        //region findview
        descriptionLayout = (LinearLayout) v.findViewById(R.id.description_layout);
        descripition = (TextView) v.findViewById(R.id.textViewDescripition);
        imageViewExpand = (ImageView) v.findViewById(R.id.imageViewExpand);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        progressBarPixnet = (ProgressBar) v.findViewById(R.id.progressBarPixnet);
        listPixnet = (ListView) v.findViewById(R.id.listPixnet);
        //endregion

        //region 摺疊效果
        descriptionLayout.setOnClickListener(new View.OnClickListener() {
            boolean isExpand;// 是否已是展開的狀態

            @Override
            public void onClick(View v) {
                isExpand = !isExpand;
                descripition.clearAnimation();// 清楚動畫效果
                final int deltaValue;// 默認高度，即前邊由maxLine確定的高度
                final int startValue = descripition.getHeight();// 起始高度
                int durationMillis = 350;// 動畫持續時間
                if (isExpand) {
                    //摺疊動畫，從實際高度縮回起始高度
                    deltaValue = descripition.getLineHeight() * descripition.getLineCount() - startValue;
                    RotateAnimation animation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f);
                    animation.setDuration(durationMillis);
                    animation.setFillAfter(true);
                    imageViewExpand.startAnimation(animation);
                } else {
                    //展開動畫，從起始高度增長至實際高度
                    deltaValue = descripition.getLineHeight() * MAX_DESCRIP_LINE - startValue;
                    RotateAnimation animation = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f);
                    animation.setDuration(durationMillis);
                    animation.setFillAfter(true);
                    imageViewExpand.startAnimation(animation);
                }
                Animation animation = new Animation() {
                    protected void applyTransformation(float interpolatedTime, Transformation t) { // 根據ImageView旋轉動畫的百分比來顯示textview高度，達到動畫效果
                        descripition.setHeight((int) (startValue + deltaValue * interpolatedTime));
                    }
                };
                animation.setDuration(durationMillis);
                descripition.startAnimation(animation);
            }
        });
        //endregion

        //region UniversalImageLoader初始化
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration //創建默認的ImageLoader配置參數
                .createDefault(getContext());
        ImageLoader.getInstance().init(configuration); //Initialize ImageLoader with configuration.
        //endregion

        Button btnReservation = (Button) v.findViewById(R.id.btnReservation);
        btnReservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(getActivity().getFilesDir().getParentFile().getPath() + "/shared_prefs/", "LoginInfo.xml");
                if (file.exists()) {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), MemberHotelOrderActivity.class);
                    intent.putExtra("ShopName", item);
                    intent.putExtra("ShopAddress", ShopAddress);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "請登入進行此操作", Toast.LENGTH_SHORT).show();
                }
            }
        });

        new introductionAsyncTask().execute();

        return v;
    }

    class introductionAsyncTask extends AsyncTask<String, Integer, Integer> {
        JSONObject itemJSON = new JSONObject();

        @Override
        protected Integer doInBackground(String... param) {

            try {
                itemJSON = new JSONObject(data);
                ShopAddress=itemJSON.get("address").toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            progressBar.setVisibility(View.GONE);

            try {
                if (itemJSON.get("xbody").toString().equals("")) {
                    descripition.setText(itemJSON.get("Toldescribe").toString());    //將tmpJSON的description資料放入名為descripition的textview裡
                    descripition.setHeight(descripition.getLineHeight() * MAX_DESCRIP_LINE);    // 名為descriptionView的text設置默認顯示高度
                    descripition.post(new Runnable() {    // 根據高度來判斷是否需要點擊展開
                        @Override
                        public void run() {
                            imageViewExpand.setVisibility(
                                    descripition.getLineCount() > MAX_DESCRIP_LINE ? View.VISIBLE : View.GONE);
                        }
                    });
                } else {
                    descripition.setText(itemJSON.get("xbody").toString());    //將tmpJSON的description資料放入名為descripition的textview裡
                    descripition.setHeight(descripition.getLineHeight() * MAX_DESCRIP_LINE);    // 名為descriptionView的text設置默認顯示高度
                    descripition.post(new Runnable() {    // 根據高度來判斷是否需要點擊展開
                        @Override
                        public void run() {
                            imageViewExpand.setVisibility(
                                    descripition.getLineCount() > MAX_DESCRIP_LINE ? View.VISIBLE : View.GONE);
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            pixnetAsyncTask.execute();
        }
    }

    class pixnetAsyncTask extends AsyncTask<String, Integer, Integer> {

        List<HashMap<String, Object>> Data = new ArrayList<HashMap<String, Object>>();
        ArrayList<String> pixnetUrl = new ArrayList<>();

        @Override
        protected Integer doInBackground(String... param) {

            try {
                String urlItem = URLEncoder.encode(item, "UTF-8");
                String url = "https://emma.pixnet.cc/blog/articles/search?key=" + urlItem; // 宣告一個String存放網址
                int count = 0;
                InputStream inputStream = null; // 宣告一個InputStream

                HttpClient httpclient = new DefaultHttpClient(); // 宣告一個HttpClient
                HttpResponse httpResponse = httpclient.execute(new HttpGet(url)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
                inputStream = httpResponse.getEntity().getContent(); // 用InputStream存放取回的內容

                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
                String responseString = readAll(rd); // 宣告一個字串放置讀取BufferedReader後的結果
                JSONObject searchJSON = new JSONObject(responseString); // 把JSONArray轉成JSONObject(JSONObjec可放置資料)
                ArrayList<String> countArray = new ArrayList<String>();

                if (searchJSON.has("articles")) {
                    JSONArray articlesJSON = new JSONArray(searchJSON.get("articles").toString());
                    if (articlesJSON.length() > 0) {
                        for (int i = 0; i < articlesJSON.length(); i++) {
                            JSONObject articleJSON = new JSONObject(articlesJSON.get(i).toString());
                            JSONObject infoJSON = new JSONObject(articleJSON.get("info").toString());
                            int hit = Integer.parseInt(infoJSON.get("hit").toString());

                            if (hit > 5000) {
                                count += 1;
                                countArray.add(Integer.toString(i));
                            }
                        }

                        if (count > 5) {
                            for (int i = 0; i < 5; i++) {
                                JSONObject articleJSON = new JSONObject(articlesJSON.get(Integer.parseInt(countArray.get(i))).toString());
                                HashMap<String, Object> hashMap = new HashMap<String, Object>(); // 宣告一個HashMap放置List資料

                                String articleID = articleJSON.get("id").toString();
                                String title = articleJSON.get("title").toString();
                                JSONObject userJSON = new JSONObject(articleJSON.get("user").toString());
                                String userName = userJSON.get("name").toString();

                                String articleUrl = "https://emma.pixnet.cc/blog/articles/" + articleID + "?user=" + userName;

                                System.out.println("Url    " + articleUrl);

                                InputStream articleInputStream = null; // 宣告一個InputStream

                                HttpClient articleHttpclient = new DefaultHttpClient(); // 宣告一個HttpClient
                                HttpResponse articleHttpResponse = articleHttpclient.execute(new HttpGet(articleUrl)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
                                articleInputStream = articleHttpResponse.getEntity().getContent(); // 用InputStream存放取回的內容

                                BufferedReader articleRd = new BufferedReader(new InputStreamReader(articleInputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
                                String articleResponseString = readAll(articleRd); // 宣告一個字串放置讀取BufferedReader後的結果
                                JSONObject oneJSON = new JSONObject(articleResponseString);
                                JSONObject articleJSONO = new JSONObject(oneJSON.get("article").toString());
                                String pictureUrl = articleJSONO.get("thumb").toString();
                                pixnetUrl.add(articleJSONO.get("link").toString());

                                System.out.println("pictureUrl " + pictureUrl);

                                String body = articleJSONO.get("body").toString();
                                Document doc = Jsoup.parse(body);
                                String text = doc.body().text();

                                if (pictureUrl.equals("")) {
                                    hashMap.put("Picture", "no picture");
                                } else {
                                    DisplayImageOptions options = new DisplayImageOptions.Builder()
                                            .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                                            .build();
                                    ImageLoader imageLoader = ImageLoader.getInstance();
                                    ; // Get singleton instance
                                    Bitmap bitmap = imageLoader.loadImageSync(pictureUrl, options);
                                    hashMap.put("Picture", bitmap);
                                }
                                hashMap.put("Title", title);
                                hashMap.put("Summary", text);
                                System.out.println("hashMap= " + hashMap);
                                Data.add(hashMap);
                                articleInputStream.close();
                            }
                        } else {
                            for (int i = 0; i < count; i++) {
                                JSONObject articleJSON = new JSONObject(articlesJSON.get(Integer.parseInt(countArray.get(i))).toString());
                                HashMap<String, Object> hashMap = new HashMap<String, Object>(); // 宣告一個HashMap放置List資料

                                String articleID = articleJSON.get("id").toString();
                                String title = articleJSON.get("title").toString();
                                JSONObject userJSON = new JSONObject(articleJSON.get("user").toString());
                                String userName = userJSON.get("name").toString();

                                String articleUrl = "https://emma.pixnet.cc/blog/articles/" + articleID + "?user=" + userName;

                                System.out.println("Url    " + articleUrl);

                                InputStream articleInputStream = null; // 宣告一個InputStream

                                HttpClient articleHttpclient = new DefaultHttpClient(); // 宣告一個HttpClient
                                HttpResponse articleHttpResponse = articleHttpclient.execute(new HttpGet(articleUrl)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
                                articleInputStream = articleHttpResponse.getEntity().getContent(); // 用InputStream存放取回的內容

                                BufferedReader articleRd = new BufferedReader(new InputStreamReader(articleInputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
                                String articleResponseString = readAll(articleRd); // 宣告一個字串放置讀取BufferedReader後的結果

                                JSONObject oneJSON = new JSONObject(articleResponseString);
                                JSONObject articleJSONO = new JSONObject(oneJSON.get("article").toString());
                                String pictureUrl = articleJSONO.get("thumb").toString();
                                pixnetUrl.add(articleJSONO.get("link").toString());

                                System.out.println("pictureUrl " + pictureUrl);

                                String body = articleJSONO.get("body").toString();
                                Document doc = Jsoup.parse(body);
                                String text = doc.body().text();

                                if (pictureUrl.equals("")) {
                                    hashMap.put("Picture", "no picture");
                                } else {
                                    DisplayImageOptions options = new DisplayImageOptions.Builder()
                                            .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                                            .build();
                                    ImageLoader imageLoader = ImageLoader.getInstance();
                                    ; // Get singleton instance
                                    Bitmap bitmap = imageLoader.loadImageSync(pictureUrl, options);
                                    hashMap.put("Picture", bitmap);
                                }
                                hashMap.put("Title", title);
                                hashMap.put("Summary", text);
                                System.out.println("hashMap= " + hashMap);
                                Data.add(hashMap);
                                articleInputStream.close();
                            }
                        }
                    }
                }
                inputStream.close();
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

            List<HashMap<String, Object>> ListData = new ArrayList<HashMap<String, Object>>();

            if (!Thread.interrupted()) {
                if (Data != null) {
                    SimpleAdapter ListAdapter = new SimpleAdapter(getContext(), ListData, R.layout.pixnet_listitem, // 宣告一個SimpleAdapter五個參數分別為(List位置,
                            // List資料來源,
                            // ListView介面檔,
                            // List裡資料的名稱,
                            // List裡資料的介面檔中id)
                            new String[]{"Picture", "Title", "Summary"}, new int[]{R.id.imageViewPicture, R.id.textViewTitle, R.id.textViewContent});

                    ListAdapter.setViewBinder(new SimpleAdapter.ViewBinder() { // 讓SimpleAdapter顯示網路抓取的圖片
                        @Override
                        public boolean setViewValue(View view, Object data, String textRepresentation) {
                            if ((view instanceof ImageView) & (data instanceof Bitmap)) {
                                ImageView iv = (ImageView) view;
                                Bitmap bmp = (Bitmap) data;
                                iv.setImageBitmap((bmp));
                                return true;
                            }
                            return false;
                        }
                    });

                    progressBarPixnet.setVisibility(View.GONE);
                    listPixnet.setAdapter(ListAdapter);
                    listPixnet.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Uri uri = Uri.parse(pixnetUrl.get(position));
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }
                    });

                    listPixnet.setVisibility(View.GONE);
                    ListData.addAll(Data);
                    ListAdapter.notifyDataSetChanged();
                    listPixnet.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pixnetAsyncTask.cancel(true);
    }
}
