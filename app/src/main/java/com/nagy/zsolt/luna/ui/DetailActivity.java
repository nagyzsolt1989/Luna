package com.nagy.zsolt.luna.ui;

import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.view.animation.BounceInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.db.chart.animation.Animation;
import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.renderer.AxisRenderer;
import com.db.chart.renderer.XRenderer;
import com.db.chart.tooltip.Tooltip;
import com.db.chart.util.Tools;
import com.db.chart.view.LineChartView;
import com.nagy.zsolt.luna.R;
import com.nagy.zsolt.luna.data.api.FetchDataListener;
import com.nagy.zsolt.luna.data.api.GetAPIRequest;
import com.nagy.zsolt.luna.data.api.RequestQueueService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.nagy.zsolt.luna.data.Constants.*;

public class DetailActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener{

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
    private JSONArray chartDetails;
    private String prefCurrency, currencyName;
    private char currencySymbol;
    private SharedPreferences settings;

    private Context mContext;

    private final String[] mXLabels = new String[8];

    private final float[][] mValues = {{3.5f, 4.7f, 4.3f, 8f, 6.5f, 9.9f, 7f, 8.3f},
            {4.5f, 2.5f, 2.5f, 9f, 4.5f, 9.5f, 5f, 8.3f}};

    private Tooltip mTip;

    private LineChartView mChart;

    private Runnable mBaseAction;

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

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        settings.registerOnSharedPreferenceChangeListener(this);
        prefCurrency = settings.getString("pref_currency_key", "USD");
        if (prefCurrency.equals("USD")){
            currencySymbol = '$';
        }else if (prefCurrency.equals("EUR")){
            currencySymbol = '€';
        }


        currencyName = intent.getStringExtra(EXTRA_CURRENCY);
        mCurrencyName.setText(currencyName);

        getCoinDetails(currencyName);
        getChartData(currencyName);
    }

    // Get the miniumum value
    public static float getMinValue(float[] array) {
        float minValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < minValue) {
                minValue = array[i];
            }
        }
        return minValue;
    }

    // Getting the maximum value
    public static float getMaxValue(float[] array) {
        float maxValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxValue = array[i];
            }
        }
        return maxValue;
    }

    private void drawChart() {
        mContext = getApplicationContext();

        mChart = (LineChartView) findViewById(R.id.chart);

        for (int i = 0; i < mValues[0].length; i++) {
            System.out.println("mValues " + mValues[0][i]);
        }

        // Tooltip
        mTip = new Tooltip(mContext, R.layout.linechart_tooltip, R.id.value);

        ((TextView) mTip.findViewById(R.id.value)).setTypeface(
                Typeface.createFromAsset(mContext.getAssets(), "OpenSans-Semibold.ttf"));

        mTip.setVerticalAlignment(Tooltip.Alignment.BOTTOM_TOP);
        mTip.setDimensions((int) Tools.fromDpToPx(58), (int) Tools.fromDpToPx(25));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

            mTip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)).setDuration(200);

            mTip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 0f)).setDuration(200);

            mTip.setPivotX(Tools.fromDpToPx(65) / 2);
            mTip.setPivotY(Tools.fromDpToPx(25));
        }

        // Data
        LineSet dataset = new LineSet(mXLabels, mValues[0]);

        dataset = new LineSet(mXLabels, mValues[0]);
        dataset.setColor(Color.parseColor("#b3b5bb"))
                .setFill(Color.parseColor("#2d374c"))
                .setDotsColor(Color.parseColor("#ffc755"))
                .setThickness(4)
                .endAt(8);
        mChart.addData(dataset);

        mBaseAction = new Runnable() {
            @Override
            public void run() {

            }
        };
        Runnable chartAction = new Runnable() {
            @Override
            public void run() {

                mBaseAction.run();
                mTip.prepare(mChart.getEntriesArea(0).get(7), mValues[0][7]);
                mChart.showTooltip(mTip, true);
            }
        };

        //Normalize Y axis
        float min = getMinValue(mValues[0]);
        float max = getMaxValue(mValues[0]);
        mChart.setAxisBorderValues(min-(min/10), max+(max/10))
                .setYLabels(AxisRenderer.LabelPosition.NONE)
                .setTooltips(mTip)
                .show(new Animation().setInterpolator(new BounceInterpolator())
                        .fromAlpha(0)
                        .withEndAction(chartAction));
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
            String url = "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=" + coin + "&tsyms=" + prefCurrency;
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
                        Double tempMarketPrice = (Double.valueOf(df.format(coinObject.getJSONObject(prefCurrency).getDouble("PRICE"))));
                        Double tempLow24Hour = (Double.valueOf(df.format(coinObject.getJSONObject(prefCurrency).getDouble("LOW24HOUR"))));
                        Double tempHigh24Hour = (Double.valueOf(df.format(coinObject.getJSONObject(prefCurrency).getDouble("HIGH24HOUR"))));
                        Double tempChange24Hour = (Double.valueOf(df.format(coinObject.getJSONObject(prefCurrency).getDouble("CHANGE24HOUR"))));
                        Double tempChangePct24Hour = (Double.valueOf(df.format(coinObject.getJSONObject(prefCurrency).getDouble("CHANGEPCT24HOUR"))));
                        Double tempVolume24Hour = (Double.valueOf(df.format(coinObject.getJSONObject(prefCurrency).getDouble("VOLUME24HOUR"))));

                        mLastValue.setText(String.valueOf(tempMarketPrice).concat(" " + currencySymbol));
                        mHighValue.setText(String.valueOf(tempHigh24Hour).concat(" " + currencySymbol));
                        mLowValue.setText(String.valueOf(tempLow24Hour).concat(" " + currencySymbol));
                        mAbsValue.setText(String.valueOf(tempChange24Hour).concat(" " + currencySymbol));
                        mPctValue.setText(String.valueOf(tempChangePct24Hour).concat(" %"));
                        mVolumeValue.setText(String.valueOf(tempVolume24Hour).concat(" " + currencySymbol));
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

    public void getChartData(String coin) {

        try {
            //Create Instance of GETAPIRequest and call it's
            //request() method
            String url = "https://min-api.cryptocompare.com/data/histoday?fsym=" + coin + "&tsym=" + prefCurrency + "&limit=7&aggregate=1";
            System.out.println(url);
            GetAPIRequest getapiRequest = new GetAPIRequest();
            getapiRequest.request(this, fetchGetChartResultListener, url);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //Implementing interfaces of FetchDataListener for GET api request
    FetchDataListener fetchGetChartResultListener = new FetchDataListener() {
        @Override
        public void onFetchComplete(JSONObject data) {
            //Fetch Complete. Now stop progress bar  or loader
            //you started in onFetchStart
            RequestQueueService.cancelProgressDialog();
            try {
                //Check result sent by our GETAPIRequest class
                if (data != null) {
                    System.out.println(data);
                    chartDetails = data.getJSONArray("Data");

                    System.out.println("Chart Details: " + chartDetails);
                    //Iterating over the JSON and save values
                    for (int i = 0; i < chartDetails.length(); i++) {

                        JSONObject chartData = chartDetails.getJSONObject(i);

                        //Get the values from the JSON
                        DecimalFormat df = new DecimalFormat("#.##");
                        Float tempCloseValue = (Float.valueOf(df.format(chartData.getDouble("close"))));
                        Long tempTime = (Long.valueOf(df.format(chartData.getDouble("time"))));
//
//                        System.out.println("Close value " + tempCloseValue);
//                        System.out.println("Time " + tempTime);

                        Calendar myDate = Calendar.getInstance();
                        myDate.setTimeInMillis(tempTime*1000);

                        int month = myDate.get(Calendar.MONTH);
                        month++;

                        System.out.println("Date " + (month < 10 ? "0" : "") + month + "." + myDate.get(Calendar.DAY_OF_MONTH));
                        System.out.println("Value " + tempCloseValue);

                        mXLabels[i] = String.valueOf((month < 10 ? "0" : "") + month + "." + myDate.get(Calendar.DAY_OF_MONTH));
                        mValues[0][i] = tempCloseValue;
                    }

                    drawChart();



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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_currency_key")) {
            try {
                String currency = settings.getString("pref_currency_key", "USD");
                System.out.println("Currency " + currency);
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
}
