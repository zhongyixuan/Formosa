package tw.edu.ntubimd.formosa.taiwan.taipei.hotel;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import tw.edu.ntubimd.formosa.R;

public class MemberHotelComment extends Fragment {

    private String item, data,ShopAddress;
    private JSONObject itemJSON = new JSONObject();

    public MemberHotelComment() {
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

        View v = inflater.inflate(R.layout.fragment_member_hotel_comment, container, false);
        //region 取得Activity資料
        TextView textView = (TextView) getActivity().findViewById(R.id.toolbar_title);
        TextView text = (TextView) getActivity().findViewById(R.id.textViewTmp);
        data = text.getText().toString();
        item = textView.getText().toString();
        //endregion

        try {
            itemJSON = new JSONObject(data);
            ShopAddress=itemJSON.get("address").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Button btnReservation = (Button) v.findViewById(R.id.btnReservation);
        btnReservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), MemberHotelOrderActivity.class);
                intent.putExtra("ShopName", item);
                intent.putExtra("ShopAddress", ShopAddress);
                startActivity(intent);
            }
        });

        return v;
    }
}
