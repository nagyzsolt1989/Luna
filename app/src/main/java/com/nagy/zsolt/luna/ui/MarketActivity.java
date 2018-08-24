package com.nagy.zsolt.luna.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nagy.zsolt.luna.R;
import com.nagy.zsolt.luna.data.Constants;
import com.nagy.zsolt.luna.data.api.FetchDataListener;
import com.nagy.zsolt.luna.data.api.GetAPIRequest;
import com.nagy.zsolt.luna.data.api.RequestQueueService;
import com.nagy.zsolt.luna.services.AnalyticsApplication;
import com.nagy.zsolt.luna.utils.MarketAdapter;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;

import static java.lang.Math.round;

public class MarketActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    @BindView(R.id.lv_market)
    ListView mMarketListView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @Nullable
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    private ActionBarDrawerToggle toggle;
    private ArrayAdapter<String> adapter;
    private MarketAdapter mMarketAdapter;
    private JSONObject marketPrices;
    private String prefCurrency;
    private char currencySymbol;
    ArrayList<String> coin, mDailyChange, mMarketValue;
    private SharedPreferences settings;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);
        ButterKnife.bind(this);

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

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

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        settings.registerOnSharedPreferenceChangeListener(this);

        prefCurrency = settings.getString("pref_currency_key", "USD");
        if (prefCurrency.equals("USD")) {
            currencySymbol = '$';
        } else if (prefCurrency.equals("EUR")) {
            currencySymbol = '€';
        }

        coin = new ArrayList<>();
        mDailyChange = new ArrayList<>();
        mMarketValue = new ArrayList<>();

        getMarketPrices();

        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener()

                {
                    @Override
                    public void onRefresh() {
                        Log.i("onRefresh", "onRefresh called from SwipeRefreshLayout");

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        getMarketPrices();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
        );

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
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Hide add menu item
        MenuItem actionAdd = mToolbar.getMenu().findItem(R.id.action_add);
        actionAdd.setVisible(false);
        return super.onPrepareOptionsMenu(menu);
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

        new android.os.AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    //Create Instance of GETAPIRequest and call it's
                    //request() method
                    String url = "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=BTC,ETH,XRP,BCH,EOS,XLM,LTC,ADA,USDT,BNB,ETC,TRX,XMR,NEO,DASH&tsyms=" + prefCurrency;

                    GetAPIRequest getapiRequest = new GetAPIRequest();
                    getapiRequest.request(MarketActivity.this, fetchGetResultListener, url);
//            Toast.makeText(getContext(), "GET API called", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR);
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

                    marketPrices = data.optJSONObject("RAW");

                    Iterator<String> keysItr = marketPrices.keys();

                    //Iterating over the JSON and save values
                    while (keysItr.hasNext()) {
                        String key = keysItr.next();
                        JSONObject coinObject = marketPrices.optJSONObject(key);

                        //Get the PRICE and CHANGEPCTDAY value and format it to 2 decimals
                        DecimalFormat df = new DecimalFormat("#.##");
                        Double tempMarketValue = (Double.valueOf(df.format(coinObject.optJSONObject(prefCurrency).optDouble("PRICE"))));
                        Double tempDailyPriceChange = (Double.valueOf(df.format(coinObject.optJSONObject(prefCurrency).optDouble("CHANGEPCT24HOUR"))));
                        mMarketValue.add(String.valueOf(tempMarketValue).concat(" " + currencySymbol));
                        mDailyChange.add(String.valueOf(tempDailyPriceChange));
                    }

                    mMarketAdapter = new MarketAdapter(MarketActivity.this, coin, mDailyChange, mMarketValue);

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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_currency_key")) {
            try {
                String currency = settings.getString("pref_currency_key", "USD");

                getMarketPrices();
                mMarketAdapter.notifyDataSetChanged();
                recreate();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String name = "MarketActivity";
        Log.i("MarketActivity", "Setting screen name: " + name);
        mTracker.setScreenName("Image~" + name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

}