package tw.edu.ntubimd.formosa.weather;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import tw.edu.ntubimd.formosa.MainActivity;
import tw.edu.ntubimd.formosa.R;

/**
 * Created by PC on 2016/10/31.
 */

public class WeatherService extends Service {
    private int mStartMode;       // indicates how to behave if the service is killed
    private IBinder mBinder;      // interface for clients that bind
    private boolean mAllowRebind; // indicates whether onRebind should be used

    private Handler handler;
    private Context mContext;

    private boolean getService = false;

    public Location getLocation(Context context) {
        LocationManager locMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
        try {
            location = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            return location;
        } catch (SecurityException e) {
            e.printStackTrace();
            return location;
        }
    }

    public WeatherService() {
        handler = new Handler();
    }

    public WeatherService setContext(Context mContext) {
        this.mContext = mContext;
        return this;
    }

    @Override
    public void onCreate() {
        // The service is being created
        testLocationProvider();
    }

    private void testLocationProvider() { //取得系統定位服務
        LocationManager status = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (status.isProviderEnabled(LocationManager.GPS_PROVIDER) || status.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //如果GPS或網路定位開啟，呼叫locationServiceInitial()更新位置
            getService = true;    //確認開啟定位服務
            updateWithNewLocation();
        }
    }

    private void updateWithNewLocation() {  //取得天氣
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
                Location mLocation = getLocation(WeatherService.this);
                if (mLocation != null) {
                    double latitude = mLocation.getLatitude();
                    double longitude = mLocation.getLongitude();
                    String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&units=metric&APPID=eeaf4460f92063034691ec3b9fb229d3";

                    try {
                        HttpClient httpclient = new DefaultHttpClient(); // 宣告一個HttpClient
                        HttpResponse httpResponse = httpclient.execute(new HttpGet(url)); // 宣告一個HttpResponse，讓HttpClient去new一個HttpGet開啟url，並且執行HttpClient
                        InputStream inputStream = httpResponse.getEntity().getContent(); // 用InputStream存放取回的內容
                        BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8"))); // 宣告一個BufferedReader去讀取InputStream
                        String responseString = readAll(rd); // 宣告一個字串放置讀取BufferedReader後的結果

                        JSONObject respJSON = new JSONObject(responseString);

                        if (respJSON.length() > 0) {
                            JSONObject main = new JSONObject(respJSON.get("main").toString());
                            String Humidity = main.getString("humidity");
                            String temp = main.getString("temp");
                            int Hum = Integer.parseInt(Humidity);
                            Double tem = Double.parseDouble(temp);
                            System.out.println(Hum);
                            if (Hum >= 80) {
                                NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                Notification notification;
                                Intent notificationIntent = new Intent(WeatherService.this, MainActivity.class);
                                PendingIntent pendingIntent = PendingIntent.getActivity(WeatherService.this, 0, notificationIntent, 0);
                                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                Notification.Builder builder = new Notification.Builder(WeatherService.this)
                                        .setAutoCancel(true)
                                        .setContentTitle("貼心提醒")
                                        .setContentText("附近有全家(民復店)賣雨具")
                                        .setContentIntent(pendingIntent)
                                        .setSmallIcon(R.drawable.logo)
                                        .setWhen(System.currentTimeMillis())
                                        .setOngoing(true);
                                notification = builder.getNotification();
                                notification.flags = Notification.FLAG_AUTO_CANCEL;
                                mNotificationManager.notify(0, notification);
                            }
                            if (tem >= 25) {
                                NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                Notification notification;
                                Intent notificationIntent = new Intent(WeatherService.this, MainActivity.class);
                                PendingIntent pendingIntent = PendingIntent.getActivity(WeatherService.this, 0, notificationIntent, 0);
                                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                Notification.Builder builder = new Notification.Builder(WeatherService.this)
                                        .setAutoCancel(true)
                                        .setContentTitle("貼心提醒")
                                        .setContentText("附近有莫凡彼歐風餐廳(民生店)可解暑")
                                        .setContentIntent(pendingIntent)
                                        .setSmallIcon(R.drawable.logo)
                                        .setWhen(System.currentTimeMillis())
                                        .setOngoing(true);
                                notification = builder.getNotification();
                                notification.flags = Notification.FLAG_AUTO_CANCEL;
                                mNotificationManager.notify(1, notification);
                            }
                            if (tem < 25) {
                                NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                Notification notification;
                                Intent notificationIntent = new Intent(WeatherService.this, MainActivity.class);
                                PendingIntent pendingIntent = PendingIntent.getActivity(WeatherService.this, 0, notificationIntent, 0);
                                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                Notification.Builder builder = new Notification.Builder(WeatherService.this)
                                        .setAutoCancel(true)
                                        .setContentTitle("貼心提醒")
                                        .setContentText("天冷了來怡客咖啡 杭州店喝杯熱熱的飲品吧")
                                        .setContentIntent(pendingIntent)
                                        .setSmallIcon(R.drawable.logo)
                                        .setWhen(System.currentTimeMillis())
                                        .setOngoing(true);
                                notification = builder.getNotification();
                                notification.flags = Notification.FLAG_AUTO_CANCEL;
                                mNotificationManager.notify(1, notification);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (UnsupportedOperationException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        testLocationProvider();

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        return mAllowRebind;
    }

    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(), after onUnbind() has already been called
    }

    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
    }
}
