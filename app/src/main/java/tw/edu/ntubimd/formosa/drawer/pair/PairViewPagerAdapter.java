package tw.edu.ntubimd.formosa.drawer.pair;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import tw.edu.ntubimd.formosa.taiwan.taipei.Comment;
import tw.edu.ntubimd.formosa.taiwan.taipei.Information;
import tw.edu.ntubimd.formosa.taiwan.taipei.Introduction;

/**
 * Created by PC on 2016/11/2.
 */

public class PairViewPagerAdapter extends FragmentPagerAdapter {

    int nNumOfTabs;

    public PairViewPagerAdapter(FragmentManager fm, int nNumOfTabs) {
        super(fm);
        this.nNumOfTabs = nNumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                AllPair tab1 = new AllPair();
                return tab1;
            case 1:
                MyPair tab2 = new MyPair();
                return tab2;
            case 2:
                NowPair tab3 = new NowPair();
                return tab3;
        }
        return null;
    }

    @Override
    public int getCount() {
        return nNumOfTabs;
    }
}
