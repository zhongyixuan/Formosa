package tw.edu.ntubimd.formosa.taiwan.taipei.hotel;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import tw.edu.ntubimd.formosa.R;

public class HotelInformation extends Fragment implements OnMapReadyCallback {

    private TextView Address, Opentime, Tel, Parking;
    private GoogleMap map;
    private MapView mapView;
    private ProgressBar progressBar;
    private String item, data;
    private LatLng location;
    private TableLayout tableLayout;

    public HotelInformation() {
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
        View v = inflater.inflate(R.layout.fragment_hotel_information, container, false);

        //取得Toolbar Menu
        setHasOptionsMenu(true);

        //region 在Fragment裡使用map須用mapView
        mapView = (MapView) v.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        mapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //endregion


        //region 取得Activity資料
        TextView textView = (TextView) getActivity().findViewById(R.id.toolbar_title);
        TextView text = (TextView) getActivity().findViewById(R.id.textViewTmp);
        data = text.getText().toString();
        item = textView.getText().toString();
        //endregion

        //region findview
        tableLayout = (TableLayout) v.findViewById(R.id.tableLayout);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        Address = (TextView) v.findViewById(R.id.textViewAddress);
        Opentime = (TextView) v.findViewById(R.id.textViewOpentime);
        Tel = (TextView) v.findViewById(R.id.textViewTels);
        Parking = (TextView) v.findViewById(R.id.textViewParking);
        //endregion

        Button btnReservation = (Button) v.findViewById(R.id.btnReservation);
        btnReservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(getActivity().getFilesDir().getParentFile().getPath() + "/shared_prefs/", "LoginInfo.xml");
                if (file.exists()) {
                    String ShopAddress = Address.getText().toString();
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

        new informationAsyncTask().execute();

        return v;
    }


    //region Fragment的Menu調用
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.toolbar_menu_county, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_collect:
                Toast.makeText(getContext(), "請登入進行此操作", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }
    //endregion

    @Override
    public void onMapReady(GoogleMap googleMap) { //準備地圖物件

        map = googleMap;
        UiSettings us = map.getUiSettings();
//        if (us != null){
//            us.setScrollGesturesEnabled(false); //讓map不能被使用者移動
//        }
    }

    class informationAsyncTask extends AsyncTask<String, Integer, Integer> {
        JSONObject itemJSON = new JSONObject();

        @Override
        protected Integer doInBackground(String... param) {

            try {
                itemJSON = new JSONObject(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            progressBar.setVisibility(View.GONE);
            mapView.setVisibility(View.VISIBLE);
            tableLayout.setVisibility(View.VISIBLE);

            try {
                Address.setText(itemJSON.get("address").toString());
                Opentime.setText(itemJSON.get("MEMO_TIME").toString());
                Tel.setText("NoFound");
                Parking.setText(itemJSON.get("MRT").toString());
                String latput = itemJSON.get("latitude").toString();
                String lonput = itemJSON.get("longitude").toString();
                Double lat = Double.parseDouble(latput);
                Double lon = Double.parseDouble(lonput);
                location = new LatLng(lat, lon);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
