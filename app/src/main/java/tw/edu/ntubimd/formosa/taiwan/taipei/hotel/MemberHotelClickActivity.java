package tw.edu.ntubimd.formosa.taiwan.taipei.hotel;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import tw.edu.ntubimd.formosa.HotelViewPagerAdapter;
import tw.edu.ntubimd.formosa.MemberHotelViewPagerAdapter;
import tw.edu.ntubimd.formosa.R;

public class MemberHotelClickActivity extends AppCompatActivity {

    private android.support.design.widget.TabLayout mTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_hotel_click);

        Intent intent = getIntent();
        String itemName = intent.getStringExtra("selected-item");
        String data = intent.getStringExtra("data");

        //region Toolbar設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(itemName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //取消AppTitle
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //endregion

        mTabs = (android.support.design.widget.TabLayout) findViewById(R.id.tabs);
        mTabs.addTab(mTabs.newTab().setText("資訊"));
        mTabs.addTab(mTabs.newTab().setText("介紹"));
        mTabs.addTab(mTabs.newTab().setText("評論"));
        mTabs.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        final MemberHotelViewPagerAdapter adapter = new MemberHotelViewPagerAdapter(getSupportFragmentManager(), mTabs.getTabCount());

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

        //region 讀取File
        TextView text = (TextView) findViewById(R.id.textViewTmp);
        text.setText(data);
        //endregion
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
