package tw.edu.ntubimd.formosa;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import tw.edu.ntubimd.formosa.taiwan.taipei.Comment;
import tw.edu.ntubimd.formosa.taiwan.taipei.Information;
import tw.edu.ntubimd.formosa.taiwan.taipei.Introduction;
import tw.edu.ntubimd.formosa.taiwan.taipei.MemberComment;

/**
 * Created by PC on 2016/11/2.
 */

public class MemberViewPagerAdapter extends FragmentPagerAdapter {

    int nNumOfTabs;

    public MemberViewPagerAdapter(FragmentManager fm, int nNumOfTabs) {
        super(fm);
        this.nNumOfTabs = nNumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                Information tab1 = new Information();
                return tab1;
            case 1:
                Introduction tab2 = new Introduction();
                return tab2;
            case 2:
                MemberComment tab3 = new MemberComment();
                return tab3;
        }
        return null;
    }

    @Override
    public int getCount() {
        return nNumOfTabs;
    }
}
