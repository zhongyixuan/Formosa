package tw.edu.ntubimd.formosa;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import tw.edu.ntubimd.formosa.taiwan.taipei.Comment;
import tw.edu.ntubimd.formosa.taiwan.taipei.Information;
import tw.edu.ntubimd.formosa.taiwan.taipei.Introduction;
import tw.edu.ntubimd.formosa.taiwan.taipei.hotel.HotelComment;
import tw.edu.ntubimd.formosa.taiwan.taipei.hotel.HotelInformation;
import tw.edu.ntubimd.formosa.taiwan.taipei.hotel.HotelIntroduction;
import tw.edu.ntubimd.formosa.taiwan.taipei.hotel.MemberHotelComment;

/**
 * Created by PC on 2016/11/2.
 */

public class HotelViewPagerAdapter extends FragmentPagerAdapter {

    int nNumOfTabs;

    public HotelViewPagerAdapter(FragmentManager fm, int nNumOfTabs) {
        super(fm);
        this.nNumOfTabs = nNumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                HotelInformation tab1 = new HotelInformation();
                return tab1;
            case 1:
                HotelIntroduction tab2 = new HotelIntroduction();
                return tab2;
            case 2:
                HotelComment tab3 = new HotelComment();
                return tab3;
        }
        return null;
    }

    @Override
    public int getCount() {
        return nNumOfTabs;
    }
}
