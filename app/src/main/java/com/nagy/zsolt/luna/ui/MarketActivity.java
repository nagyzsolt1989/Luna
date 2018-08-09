package com.nagy.zsolt.luna.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.nagy.zsolt.luna.R;
import com.nagy.zsolt.luna.data.Constants;
import com.nagy.zsolt.luna.data.FetchDataListener;
import com.nagy.zsolt.luna.data.GetAPIRequest;
import com.nagy.zsolt.luna.data.RequestQueueService;
import com.nagy.zsolt.luna.utils.MarketAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static java.lang.Math.round;

public class MarketActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.lv_market)
    ListView mMarketListView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @Nullable
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private ArrayAdapter<String> adapter;
    private MarketAdapter mMarketAdapter;
    private JSONObject marketPrices;
    ArrayList<String> coin, mDailyChange, mMarketValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(false);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        coin = new ArrayList<>();
        mDailyChange = new ArrayList<>();
        mMarketValue = new ArrayList<>();

        getMarketPrices();

        coin.add("BTC");
        coin.add("ETH");
        coin.add("XRP");
        coin.add("BCH");
        coin.add("EOS");
        coin.add("XLM");
        coin.add("LTC");
        coin.add("ADA");
        coin.add("USDT");
        coin.add("BNB");
        coin.add("ETC");
        coin.add("TRX");
        coin.add("XMR");
        coin.add("NEO");
        coin.add("DASH");

    }

    private void launchDetailActivity(int position) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Constants.EXTRA_POSITION, position);
        intent.putExtra(Constants.EXTRA_CURRENCY, coin.get(position).toString());
        startActivity(intent);
        this.overridePendingTransition(R.anim.slide_from_right, R.anim.fade_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_drawer) {

            mDrawer = findViewById(R.id.drawer_layout);

            if (!mDrawer.isDrawerOpen(Gravity.LEFT)) {
                mDrawer.openDrawer(Gravity.LEFT);
                toggle.syncState();
            } else {
                mDrawer.closeDrawer(Gravity.LEFT);
                toggle.syncState();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_gallery) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            this.overridePendingTransition(R.anim.slide_from_right, R.anim.fade_out);

        } else if (id == R.id.nav_slideshow) {
            mDrawer.closeDrawer(Gravity.LEFT);

        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            this.overridePendingTransition(R.anim.slide_from_right, R.anim.fade_out);
        }

        return true;
    }

    public void getMarketPrices() {

        try {
            //Create Instance of GETAPIRequest and call it's
            //request() method
            String url = "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=BTC,ETH,XRP,BCH,EOS,XLM,LTC,ADA,USDT,BNB,ETC,TRX,XMR,NEO,DASH&tsyms=USD,EUR";
            System.out.println(url);
            GetAPIRequest getapiRequest = new GetAPIRequest();
            getapiRequest.request(this, fetchGetResultListener, url);
//            Toast.makeText(getContext(), "GET API called", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //Implementing interfaces of FetchDataListener for GET api request
    FetchDataListener fetchGetResultListener = new FetchDataListener() {
        @Override
        public void onFetchComplete(JSONObject data) {
            //Fetch Complete. Now stop progress bar  or loader
            //you started in onFetchStart
            RequestQueueService.cancelProgressDialog();
            try {
                //Check result sent by our GETAPIRequest class
                if (data != null) {
//                    System.out.println(data);
                    marketPrices = data.getJSONObject("RAW");

                    Iterator<String> keysItr = marketPrices.keys();

                    //Iterating over the JSON and save values
                    while (keysItr.hasNext()) {
                        String key = keysItr.next();
                        JSONObject coinObject = marketPrices.getJSONObject(key);

                        //Get the PRICE and CHANGEPCTDAY value and format it to 2 decimals
                        DecimalFormat df = new DecimalFormat("#.##");
                        Double tempMarketValue = (Double.valueOf(df.format(coinObject.getJSONObject("USD").getDouble("PRICE"))));
                        Double tempDailyPriceChange = (Double.valueOf(df.format(coinObject.getJSONObject("USD").getDouble("CHANGEPCT24HOUR"))));
                        mMarketValue.add(String.valueOf(tempMarketValue));
                        mDailyChange.add(String.valueOf(tempDailyPriceChange));
                    }

                    mMarketAdapter = new MarketAdapter(MarketActivity.this, coin, getResources().getStringArray(R.array.ic_coins), mDailyChange, mMarketValue);

                    // Assign adapter to ListView
                    mMarketListView.setAdapter(mMarketAdapter);

                    mMarketListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                            launchDetailActivity(position);
                        }
                    });


                } else {
                    RequestQueueService.showAlert(getString(R.string.noDataAlert), MarketActivity.this);
                }
            } catch (
                    Exception e) {
                RequestQueueService.showAlert(getString(R.string.exceptionAlert), MarketActivity.this);
                e.printStackTrace();
            }

        }

        @Override
        public void onFetchFailure(String msg) {
            RequestQueueService.cancelProgressDialog();
            //Show if any error message is there called from GETAPIRequest class
            RequestQueueService.showAlert(msg, MarketActivity.this);
        }

        @Override
        public void onFetchStart() {
            //Start showing progressbar or any loader you have
            RequestQueueService.showProgressDialog(MarketActivity.this);
        }
    };
}