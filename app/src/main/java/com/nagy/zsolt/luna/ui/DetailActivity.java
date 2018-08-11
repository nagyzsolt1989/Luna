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
import android.widget.TextView;
import android.widget.Toast;

import com.nagy.zsolt.luna.R;
import com.nagy.zsolt.luna.data.FetchDataListener;
import com.nagy.zsolt.luna.data.GetAPIRequest;
import com.nagy.zsolt.luna.data.RequestQueueService;
import com.nagy.zsolt.luna.utils.MarketAdapter;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.nagy.zsolt.luna.data.Constants.*;

public class DetailActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    @BindView(R.id.tv_crypto_currency)
    TextView mCurrencyName;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @Nullable
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.tv_last_value)
    TextView mLastValue;
    @BindView(R.id.tv_high_value)
    TextView mHighValue;
    @BindView(R.id.tv_low_value)
    TextView mLowValue;
    @BindView(R.id.tv_absolute_value)
    TextView mAbsValue;
    @BindView(R.id.tv_percentage_value)
    TextView mPctValue;
    @BindView(R.id.tv_volume_value)
    TextView mVolumeValue;
    private ActionBarDrawerToggle toggle;
    private JSONObject coinDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        Intent intent = getIntent();
        if (intent == null) {
            closeOnError();
        }

        int position = intent.getIntExtra(EXTRA_POSITION, DEFAULT_POSITION);
        if (position == DEFAULT_POSITION) {
            // EXTRA_POSITION not found in intent
            closeOnError();
            return;
        }

        toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(false);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        String currencyName = intent.getStringExtra(EXTRA_CURRENCY);
        mCurrencyName.setText(currencyName);

        getCoinDetails(currencyName);
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.detail_error_message, Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_gallery) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            this.overridePendingTransition(R.anim.slide_from_right, R.anim.fade_out);

        } else if (id == R.id.nav_slideshow) {
            Intent intent = new Intent(this, MarketActivity.class);
            startActivity(intent);
            this.overridePendingTransition(R.anim.slide_from_right, R.anim.fade_out);

        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            this.overridePendingTransition(R.anim.slide_from_right, R.anim.fade_out);
        }

        return true;
    }

    public void getCoinDetails(String coin) {

        try {
            //Create Instance of GETAPIRequest and call it's
            //request() method
            String url = "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=" + coin + "&tsyms=USD,EUR";
            System.out.println(url);
            GetAPIRequest getapiRequest = new GetAPIRequest();
            getapiRequest.request(this, fetchGetResultListener, url);
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
                    System.out.println(data);
                    coinDetails = data.getJSONObject("RAW");

                    Iterator<String> keysItr = coinDetails.keys();

                    //Iterating over the JSON and save values
                    while (keysItr.hasNext()) {
                        String key = keysItr.next();
                        JSONObject coinObject = coinDetails.getJSONObject(key);

                        //Get the values from the JSON
                        DecimalFormat df = new DecimalFormat("#.##");
                        Double tempMarketPrice = (Double.valueOf(df.format(coinObject.getJSONObject("USD").getDouble("PRICE"))));
                        Double tempLow24Hour = (Double.valueOf(df.format(coinObject.getJSONObject("USD").getDouble("LOW24HOUR"))));
                        Double tempHigh24Hour = (Double.valueOf(df.format(coinObject.getJSONObject("USD").getDouble("HIGH24HOUR"))));
                        Double tempChange24Hour = (Double.valueOf(df.format(coinObject.getJSONObject("USD").getDouble("CHANGE24HOUR"))));
                        Double tempChangePct24Hour = (Double.valueOf(df.format(coinObject.getJSONObject("USD").getDouble("CHANGEPCT24HOUR"))));
                        Double tempVolume24Hour = (Double.valueOf(df.format(coinObject.getJSONObject("USD").getDouble("VOLUME24HOUR"))));

                        mLastValue.setText(String.valueOf(tempMarketPrice).concat(" $"));
                        mHighValue.setText(String.valueOf(tempHigh24Hour).concat(" $"));
                        mLowValue.setText(String.valueOf(tempLow24Hour).concat(" $"));
                        mAbsValue.setText(String.valueOf(tempChange24Hour).concat(" $"));
                        mPctValue.setText(String.valueOf(tempChangePct24Hour).concat(" %"));
                        mVolumeValue.setText(String.valueOf(tempVolume24Hour).concat(" $"));
                    }



                } else {
                    RequestQueueService.showAlert(getString(R.string.noDataAlert), DetailActivity.this);
                }
            } catch (
                    Exception e) {
                RequestQueueService.showAlert(getString(R.string.exceptionAlert), DetailActivity.this);
                e.printStackTrace();
            }

        }

        @Override
        public void onFetchFailure(String msg) {
            RequestQueueService.cancelProgressDialog();
            //Show if any error message is there called from GETAPIRequest class
            RequestQueueService.showAlert(msg, DetailActivity.this);
        }

        @Override
        public void onFetchStart() {
            //Start showing progressbar or any loader you have
            RequestQueueService.showProgressDialog(DetailActivity.this);
        }
    };
}
