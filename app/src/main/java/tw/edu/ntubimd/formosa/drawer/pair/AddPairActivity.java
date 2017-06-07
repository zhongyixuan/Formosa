package tw.edu.ntubimd.formosa.drawer.pair;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import tw.edu.ntubimd.formosa.R;

public class AddPairActivity extends AppCompatActivity  implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private EditText editTextShopName, editTextProductName, editTextProductPrice, editTextPreferentialType, editTextUserFeature;
    private Button buttonPair;
    private RadioGroup radioGroup;
    private GoogleApiClient mGoogleApiClient;
    private AutoCompleteTextView mAutocompleteView;
    private PlaceAutocompleteAdapter mAdapter;
    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(21.715956, 119.419628), new LatLng(25.371160, 122.138744));
    private Time waitTime = null;
    private boolean isGPSEnabled, isNetworkEnabled;
    Location location = null;
    //The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    //The minimum time beetwen updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 0;//1000 * 60 * 1; // 1 minute
    private String idString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pair);

        File file = new File(getApplication().getFilesDir().getParentFile().getPath() + "/shared_prefs/", "LoginInfo.xml");
        if (file.exists()) {
            ReadValue();
        }

        mAutocompleteView = (AutoCompleteTextView) findViewById(R.id.autoComplete);

        editTextShopName = (EditText) findViewById(R.id.editTextShopName);
        editTextProductName = (EditText) findViewById(R.id.editTextProductName);
        editTextProductPrice = (EditText) findViewById(R.id.editTextProductPrice);
        editTextPreferentialType = (EditText) findViewById(R.id.editTextPreferentialType);
        editTextUserFeature = (EditText) findViewById(R.id.editTextUserFeature);

        buttonPair = (Button) findViewById(R.id.buttonPair);

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        //region Toolbar設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("加入Pair");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //取消AppTitle
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //endregion

        getLocation();

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);
        List<Integer> filterTypes = new ArrayList<Integer>();
        filterTypes.add(Place.TYPE_ESTABLISHMENT);
        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_SYDNEY, null);
        mAutocompleteView.setAdapter(mAdapter);

        buttonPair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((editTextShopName.getText().toString().length() != 0) && (editTextProductName.getText().toString().length() != 0) && (editTextProductPrice.getText().toString().length() != 0)
                        && (editTextPreferentialType.getText().toString().length() != 0) && (editTextUserFeature.getText().toString().length() != 0) && waitTime != null) {
                    if (location != null) {
                        System.out.println("準備執行pair");
                        pair();
                    }
                } else {
                    Toast toast = Toast.makeText(AddPairActivity.this, "有欄位未填寫", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButtonWaitTime30:
                        waitTime = new Time(0, 30, 0);
                        Toast toast = Toast.makeText(AddPairActivity.this, "wait30", Toast.LENGTH_SHORT);
                        toast.show();
                        break;
                    case R.id.radioButtonWaitTime60:
                        waitTime = new Time(0, 60, 0);
                        Toast toast2 = Toast.makeText(AddPairActivity.this, "wait60", Toast.LENGTH_SHORT);
                        toast2.show();
                        break;
                    case R.id.radioButtonWaitTime90:
                        waitTime = new Time(0, 90, 0);
                        Toast toast3 = Toast.makeText(AddPairActivity.this, "wait90", Toast.LENGTH_SHORT);
                        toast3.show();
                        break;
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

    private void getLocation(){
        LocationManager status = (LocationManager) getSystemService(LOCATION_SERVICE);
        isGPSEnabled = status.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = status.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!(isGPSEnabled || isNetworkEnabled)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(AddPairActivity.this);
            dialog.setMessage("請開啟定位服務以獲取目前位置");
            dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.show();
        } else {
            try {
                if (isNetworkEnabled) {
                    System.out.println("isNetworkEnabled");
                    status.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, mLocationListener);
                    if (status.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
                        location = status.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        System.out.println("net=" + location);
                    }
                }
                if (isGPSEnabled) {
                    System.out.println("isGPSEnabled");
                    status.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, mLocationListener);
                    if (status.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
                        location = status.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        System.out.println("gps=" + location);
                    }
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
                    Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);

            // Format details of the place for display and show it in a TextView.
            editTextShopName.setText(place.getName());

            // Display the third party attributions if set.
            places.release();
        }
    };

    private void pair() {

        final String url = "http://140.131.114.161:8080/Formosa/rest/pair/addPair";

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

                            System.out.println("執行pair中");
                            parameter.accumulate("userID", idString);
                            parameter.accumulate("shopName", editTextShopName.getText().toString());
                            parameter.accumulate("productName", editTextProductName.getText().toString());
                            parameter.accumulate("productPrice", editTextProductPrice.getText().toString());
                            parameter.accumulate("preferentialType", editTextPreferentialType.getText().toString());
                            parameter.accumulate("pairAddress", mAutocompleteView.getText().toString());
                            parameter.accumulate("userFeature", editTextUserFeature.getText().toString());
                            parameter.accumulate("pairTime", getDateTime());
                            parameter.accumulate("waitTime", waitTime.toString());
                            parameter.accumulate("pairLongitude", Double.toString(location.getLongitude()));
                            parameter.accumulate("pairLatitude", Double.toString(location.getLatitude()));


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
                                Toast toast = Toast.makeText(AddPairActivity.this, "成功", Toast.LENGTH_SHORT);
                                toast.show();
                                finish();
                            } else {
                                Toast toast = Toast.makeText(AddPairActivity.this, "失敗", Toast.LENGTH_SHORT);
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
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    public static String getDateTime() {  //無參數=傳回現在時間
        Calendar c = Calendar.getInstance();
        return getYMDHMS(c);
    }

    public static String getYMDHMS(Calendar c) { //輸出格式製作
        int[] a = {c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                c.get(Calendar.SECOND)
        };
        StringBuffer sb = new StringBuffer();
        sb.append(a[0]);
        if (a[1] < 9) {
            sb.append("-0" + (a[1] + 1));
        }   //加 1 才會得到實際月份
        else {
            sb.append("-" + (a[1] + 1));
        }
        if (a[2] < 10) {
            sb.append("-0" + (a[2]));
        } else {
            sb.append("-" + (a[2]));
        }
        if (a[3] < 10) {
            sb.append(" 0" + (a[3]));
        } else {
            sb.append(" " + (a[3]));
        }
        if (a[4] < 10) {
            sb.append(":0" + a[4]);
        } else {
            sb.append(":" + a[4]);
        }
        if (a[5] < 10) {
            sb.append(":0" + a[5]);
        } else {
            sb.append(":" + a[5]);
        }
        return sb.toString();
    }

    public void ReadValue() {
        SharedPreferences setting = getSharedPreferences("LoginInfo", 0);
        idString = setting.getString("Id", "");
    }
}
