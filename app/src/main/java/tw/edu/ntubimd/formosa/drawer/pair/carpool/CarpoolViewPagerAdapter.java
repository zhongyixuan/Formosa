package tw.edu.ntubimd.formosa.drawer.pair.carpool;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import tw.edu.ntubimd.formosa.drawer.pair.AllPair;
import tw.edu.ntubimd.formosa.drawer.pair.MyPair;
import tw.edu.ntubimd.formosa.drawer.pair.NowPair;

/**
 * Created by PC on 2016/11/2.
 */

public class CarpoolViewPagerAdapter extends FragmentPagerAdapter {

    int nNumOfTabs;

    public CarpoolViewPagerAdapter(FragmentManager fm, int nNumOfTabs) {
        super(fm);
        this.nNumOfTabs = nNumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                AllCarpool tab1 = new AllCarpool();
                return tab1;
            case 1:
                MyCarpool tab2 = new MyCarpool();
                return tab2;
            case 2:
                NowCarpool tab3 = new NowCarpool();
                return tab3;
        }
        return null;
    }

    @Override
    public int getCount() {
        return nNumOfTabs;
    }
}
