package tw.edu.ntubimd.formosa.taiwan.taipei.hotel;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import tw.edu.ntubimd.formosa.R;

public class HotelComment extends Fragment {

    public HotelComment() {
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
        View v = inflater.inflate(R.layout.fragment_hotel_comment, container, false);

        Button btnReservation = (Button) v.findViewById(R.id.btnReservation);
        btnReservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "請登入進行此操作", Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }
}
