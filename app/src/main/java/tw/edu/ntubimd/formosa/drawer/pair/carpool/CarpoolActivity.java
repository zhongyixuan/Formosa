package tw.edu.ntubimd.formosa.drawer.pair.carpool;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import tw.edu.ntubimd.formosa.R;
import tw.edu.ntubimd.formosa.drawer.pair.PairViewPagerAdapter;

public class CarpoolActivity extends AppCompatActivity {

    private android.support.design.widget.TabLayout mTabs;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carpool);

        //region Toolbar設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("共乘");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //取消AppTitle
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //endregion

        mTabs = (android.support.design.widget.TabLayout) findViewById(R.id.tabs);
        mTabs.addTab(mTabs.newTab().setText("尋找Pair"));
        mTabs.addTab(mTabs.newTab().setText("我的Pair"));
        mTabs.addTab(mTabs.newTab().setText("一起Pair"));
        mTabs.setTabGravity(TabLayout.GRAVITY_FILL);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        final CarpoolViewPagerAdapter adapter = new CarpoolViewPagerAdapter(getSupportFragmentManager(), mTabs.getTabCount());

        mTabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabs));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //返回鍵
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}