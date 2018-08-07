package com.nagy.zsolt.luna.ui;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.nagy.zsolt.luna.R;
import com.nagy.zsolt.luna.data.Constants;
import com.nagy.zsolt.luna.data.FetchDataListener;
import com.nagy.zsolt.luna.data.GetAPIRequest;
import com.nagy.zsolt.luna.data.RequestQueueService;
import com.nagy.zsolt.luna.utils.PortfolioAdapter;
import com.nagy.zsolt.luna.utils.SwipeDismissListViewTouchListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DatePickerDialog.OnDateSetListener {

    @Nullable
    @BindView(R.id.lv_portfolio)
    ListView mPortfolioListView;
    @Nullable
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @Nullable
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @Nullable
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @Nullable
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @Nullable
    @BindView(R.id.currency_spinner)
    Spinner mCurrencySpinner;
    @Nullable
    @BindView(R.id.amount)
    EditText mAmount;
    @Nullable
    @BindView(R.id.tv_portfolio_value)
    TextView mSumPortfolioTV;
    @Nullable
    @BindView(R.id.transaction_date)
    Button mTransactionDate;
    private PortfolioAdapter mPortfolioAdapter;
    private ActionBarDrawerToggle toggle;
    JSONObject coinsJsonArray;
    ArrayList<String> coin, amount, values;
    float mSumPortfolio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTransactionDialog();
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        coin = new ArrayList<>();
        amount = new ArrayList<>();
        values = new ArrayList<>();

        coin.add("BTC");
        coin.add("ETH");
        coin.add("EOS");
        amount.add("0.35");
        amount.add("11");
        amount.add("27");
        values.add("1000");
        values.add("1600");
        values.add("2000");

        calculateSumPortfolio();

        mPortfolioAdapter = new PortfolioAdapter(this, coin, amount, values);


        // Assign adapter to ListView
        mPortfolioListView.setAdapter(mPortfolioAdapter);

        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        mPortfolioListView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {

                                    coin.remove(position);
                                    values.remove(position);
                                    mPortfolioAdapter.notifyDataSetChanged();
                                    calculateSumPortfolio();

                                }

                            }
                        });
        mPortfolioListView.setOnTouchListener(touchListener);
        mPortfolioListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                launchDetailActivity(position);
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
            mDrawer.closeDrawer(Gravity.LEFT);

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

    private void showTransactionDialog() {

        getCoinList();

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialog_layout = inflater.inflate(R.layout.dialog_transaction, (ViewGroup) findViewById(R.id.dialog_linear_layout));

        AlertDialog.Builder db = new AlertDialog.Builder(this);
        ButterKnife.bind(this, dialog_layout);
        db.setView(dialog_layout);
        db.setTitle("New Transaction");

        mTransactionDate.setShowSoftInputOnFocus(false);
        mTransactionDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Time today = new Time(Time.getCurrentTimezone());
                Calendar calendar = Calendar.getInstance();

                DatePickerDialog dialog = new DatePickerDialog(MainActivity.this, MainActivity.this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

        db.setPositiveButton("OK", new DialogInterface.OnClickListener()

        {
            public void onClick(DialogInterface dialog, int which) {

                coin.add(mCurrencySpinner.getSelectedItem().toString());
                amount.add(mAmount.getText().toString());
                values.add("1000");
                mPortfolioAdapter.notifyDataSetChanged();
                calculateSumPortfolio();

            }
        });
        db.setNegativeButton("Cancel", new DialogInterface.OnClickListener()

        {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = db.show();
    }

    public void getCoinList() {

        try {
            //Create Instance of GETAPIRequest and call it's
            //request() method
            String url = "https://www.cryptocompare.com/api/data/coinlist/";
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
                    coinsJsonArray = data.getJSONObject("Data");
                    System.out.println("Coinsjsonarray" + coinsJsonArray.names());

                } else {
                    RequestQueueService.showAlert(getString(R.string.noDataAlert), MainActivity.this);
                }
            } catch (
                    Exception e) {
                RequestQueueService.showAlert(getString(R.string.exceptionAlert), MainActivity.this);
                e.printStackTrace();
            }

        }

        @Override
        public void onFetchFailure(String msg) {
            RequestQueueService.cancelProgressDialog();
            //Show if any error message is there called from GETAPIRequest class
            RequestQueueService.showAlert(msg, MainActivity.this);
        }

        @Override
        public void onFetchStart() {
            //Start showing progressbar or any loader you have
            RequestQueueService.showProgressDialog(MainActivity.this);
        }
    };

    private void launchDetailActivity(int position) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Constants.EXTRA_POSITION, position);
        intent.putExtra(Constants.EXTRA_CURRENCY, coin.get(position).toString());
        startActivity(intent);
        this.overridePendingTransition(R.anim.slide_from_right, R.anim.fade_out);
    }

    private void calculateSumPortfolio(){
        mSumPortfolio = 0.0f;
        for(int i = 0; i < values.size(); i++) {
            mSumPortfolio += Float.parseFloat(values.get(i));
        }
        mSumPortfolioTV.setText(Float.toString(mSumPortfolio));
    }

    @Override
    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {

        month++;
        mTransactionDate.setText(year + "." + month + "." + dayOfMonth);
    }
}
