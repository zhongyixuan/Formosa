package tw.edu.ntubimd.formosa;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import tw.edu.ntubimd.formosa.taiwan.taipei.Comment;
import tw.edu.ntubimd.formosa.taiwan.taipei.Information;
import tw.edu.ntubimd.formosa.taiwan.taipei.Introduction;

/**
 * Created by PC on 2016/11/2.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {

    int nNumOfTabs;

    public ViewPagerAdapter(FragmentManager fm, int nNumOfTabs) {
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
                Comment tab3 = new Comment();
                return tab3;
        }
        return null;
    }

    @Override
    public int getCount() {
        return nNumOfTabs;
    }
}
