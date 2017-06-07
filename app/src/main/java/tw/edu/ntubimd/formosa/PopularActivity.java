package tw.edu.ntubimd.formosa;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PopularActivity extends AppCompatActivity {

    private final String[] popular = new String[]{"象山登山步道", "康莊蓮園", "復興鄉 基國派教堂", "法鼓山農禪寺", "八煙聚落", "小烏來天空步道"};
    private final int[] picure = new int[]{R.drawable.popular_mountain, R.drawable.popular_flower, R.drawable.popular_church, R.drawable.popular_temple, R.drawable.popular_settlement, R.drawable.popular_skytrail};
    List<Map<String, Object>> pData = new ArrayList<Map<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular);

        //region Toolbar設定
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("熱門景點");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //取消AppTitle
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //endregion

        ListView list = (ListView) findViewById(R.id.list);
        pData = getData();
        PopularAdapter adapter = new PopularAdapter(this);
        list.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //返回鍵
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list2 = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < popular.length; i++) {
            HashMap<String, Object> item = new HashMap<String, Object>();
            item.put("picture", picure[i]);
            item.put("popularName", popular[i]);
            list2.add(item);
        }
        return list2;
    }

    public final class ViewHolder {
        public ImageView count, picture;
        public TextView popularName;
    }

    class PopularAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public PopularAdapter(Context c) {
            mInflater = LayoutInflater.from(c);
        }

        public int getCount() {
            // TODO Auto-generated method stub
            return pData.size();
        }

        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder popularview = null;
            popularview = new ViewHolder();
            convertView = mInflater.inflate(R.layout.popular_attractionlistview, null);
            popularview.picture = (ImageView) convertView.findViewById(R.id.imagePicture);
            popularview.popularName = (TextView) convertView.findViewById(R.id.populartextView);
            popularview.picture.setImageResource(picure[position]);
            popularview.popularName.setText((String) pData.get(position).get("popularName"));

            return convertView;
        }
    }
}
