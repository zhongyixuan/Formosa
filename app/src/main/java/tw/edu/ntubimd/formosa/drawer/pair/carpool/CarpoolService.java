package tw.edu.ntubimd.formosa.drawer.pair.carpool;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
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

import tw.edu.ntubimd.formosa.R;
import tw.edu.ntubimd.formosa.drawer.CarpoolPairUserInfoActivity;

/**
 * Created by PC on 2016/11/24.
 */

public class CarpoolService extends Service {

    private Handler handler = new Handler();
    private int count;
    private String idString;

    public Runnable getTravelPairUserInfo = new Runnable() {
        @Override
        public void run() {
            getTravelPairUserNowCount();
            handler.postDelayed(this, 60000);
        }
    };

    private void getTravelPairUserCount() {
        final String url = "http://140.131.114.161:8080/Formosa/rest/travelPairUserInfo/getTravelPairIDByUserID";

        new Thread(new Runnable() {
            @Override
            public void run() {
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
                    String result = EntityUtils.toString(resEntity);

                    JSONObject registerJson = new JSONObject(result);
                    String tmp = registerJson.get("statuscode").toString();

                    if (tmp.equals("0")) {
                        JSONArray travelPairsJSON = new JSONArray(registerJson.get("TravelPairs").toString());

                        count = travelPairsJSON.length();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getTravelPairUserNowCount() {
        final String url = "http://140.131.114.161:8080/Formosa/rest/travelPairUserInfo/getTravelPairIDByUserID";
        System.out.println("getTravelPairUserNowCount.......");

        new Thread(new Runnable() {
            @Override
            public void run() {
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
                    String result = EntityUtils.toString(resEntity);

                    JSONObject registerJson = new JSONObject(result);
                    String tmp = registerJson.get("statuscode").toString();

                    if (tmp.equals("0")) {
                        JSONArray travelPairsJSON = new JSONArray(registerJson.get("TravelPairs").toString());

                        int nowCount = travelPairsJSON.length();

                        if (nowCount > count) {

                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            Notification notification;
                            Intent notificationIntent = new Intent(CarpoolService.this, CarpoolPairUserInfoActivity.class);
                            PendingIntent pendingIntent = PendingIntent.getActivity(CarpoolService.this, 0, notificationIntent, 0);
                            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            Notification.Builder builder = new Notification.Builder(CarpoolService.this)
                                    .setAutoCancel(true)
                                    .setContentTitle("TravelPair")
                                    .setContentText("有人想跟你一起Pair")
                                    .setContentIntent(pendingIntent)
                                    .setSmallIcon(R.drawable.logo)
                                    .setWhen(System.currentTimeMillis())
                                    .setOngoing(true);
                            notification = builder.getNotification();
                            notification.flags = Notification.FLAG_AUTO_CANCEL;
                            mNotificationManager.notify(1, notification);

                            count = nowCount;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        File file = new File(getApplication().getFilesDir().getParentFile().getPath() + "/shared_prefs/", "LoginInfo.xml");
        if (file.exists()) {
            ReadValue();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getTravelPairUserCount();
        System.out.println("count: "+count);
        //設定Delay的時間
        handler.postDelayed(getTravelPairUserInfo, 30000);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //設定定時要執行的方法
        handler.removeCallbacks(getTravelPairUserInfo);
    }

    public void ReadValue() {
        SharedPreferences setting = getSharedPreferences("LoginInfo", 0);
        idString = setting.getString("Id", "");
    }
}
