package com.nagy.zsolt.luna.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.nagy.zsolt.luna.R;
import com.nagy.zsolt.luna.data.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MarketActivity extends AppCompatActivity {

    @BindView(R.id.lv_market)
    ListView mMarketListView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    private ArrayAdapter<String> adapter;
    private List<String> mMarketValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        mMarketValues = new ArrayList<String>(Arrays.asList("Android", "iPhone", "WindowsMobile",
                "Blackberry"));

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mMarketValues);

        mMarketListView.setAdapter(adapter);

        mMarketListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                launchDetailActivity(position);
            }
        });
    }

    private void launchDetailActivity(int position) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Constants.EXTRA_POSITION, position);
        intent.putExtra(Constants.EXTRA_CURRENCY, mMarketValues.get(position).toString());
        startActivity(intent);
        this.overridePendingTransition(R.anim.slide_from_right, R.anim.fade_out);
    }
}