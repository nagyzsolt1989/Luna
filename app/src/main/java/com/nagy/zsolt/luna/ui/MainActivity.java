package com.nagy.zsolt.luna.ui;

import android.app.DatePickerDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nagy.zsolt.luna.R;
import com.nagy.zsolt.luna.data.AppExecutors;
import com.nagy.zsolt.luna.data.Constants;
import com.nagy.zsolt.luna.data.MainViewModel;
import com.nagy.zsolt.luna.data.api.FetchDataListener;
import com.nagy.zsolt.luna.data.api.GetAPIRequest;
import com.nagy.zsolt.luna.data.api.RequestQueueService;
import com.nagy.zsolt.luna.data.database.AppDatabase;
import com.nagy.zsolt.luna.data.database.PortfolioEntry;
import com.nagy.zsolt.luna.services.AnalyticsApplication;
import com.nagy.zsolt.luna.utils.PortfolioAdapter;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.v7.widget.RecyclerView.VERTICAL;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DatePickerDialog.OnDateSetListener, PortfolioAdapter.ItemClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    @Nullable
    @BindView(R.id.rv_portfolio)
    RecyclerView mPortfolioRecyclerView;
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
    private AppDatabase mDb;
    private PortfolioAdapter mPortfolioAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ActionBarDrawerToggle toggle;
    private ArrayList<String> coin, amount, date, values;
    private LiveData<List<PortfolioEntry>> mPortfoioEntries;
    private double mSumPortfolio;
    private String prefCurrency;
    private char currencySymbol;
    private SharedPreferences settings;
    private Tracker mTracker;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        // Initialize Admob
        MobileAds.initialize(this, "ca-app-pub-8219404138834758~1167365723");

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mDb = AppDatabase.getsInstance(getApplicationContext());
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        prefCurrency = settings.getString(getString(R.string.pref_currency_key), getString(R.string.currency_USD));
        if (prefCurrency.equals(getString(R.string.currency_USD))) {
            currencySymbol = '$';
        } else if (prefCurrency.equals(getString(R.string.currency_EUR))) {
            currencySymbol = '€';
        }


        initViews();

    }

    private void initViews() {
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

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mPortfolioRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mPortfolioRecyclerView.setLayoutManager(mLayoutManager);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        settings.registerOnSharedPreferenceChangeListener(this);

        coin = new ArrayList<>();
        amount = new ArrayList<>();
        date = new ArrayList<>();
        values = new ArrayList<>();

        refreshPortfolioData();

        //Make a string from all of the coins
        StringBuilder allCoins = new StringBuilder();
        String prefix = "";
        for (String s : coin) {
            allCoins.append(prefix);
            prefix = ",";
            allCoins.append(s);
        }

        if (coin.size() > 0) {
            updatePortfolioPrices(allCoins.toString());
        }

        if (coin.size() < 1) {
            mPortfolioAdapter = new PortfolioAdapter(MainActivity.this, this, values);

            // Assign adapter to ListView
            mPortfolioRecyclerView.setAdapter(mPortfolioAdapter);
            setupViewModel();
        }

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        mPortfolioRecyclerView.addItemDecoration(decoration);

        /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {

                int position = viewHolder.getAdapterPosition();
                List<PortfolioEntry> portfolioEntries = mPortfolioAdapter.getPortfolioEntries();
                values.remove(position);
                mDb.portfolioDao().deletePortfolio(portfolioEntries.get(position));


                refreshPortfolioData();
                calculateSumPortfolio();
            }
        }).attachToRecyclerView(mPortfolioRecyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String name = "MainActivity";
        Log.i("MainActivity", "Setting screen name: " + name);
        mTracker.setScreenName("Image~" + name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
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
        if (id == R.id.action_add) {
            showTransactionDialog();
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

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getPortfolioEntries().observe(this, new Observer<List<PortfolioEntry>>() {
            @Override
            public void onChanged(@Nullable final List<PortfolioEntry> portfolioEntries) {
                if (values.size() > 0) {
                    mPortfolioAdapter.setPortfolioEntries(portfolioEntries);
                }
            }
        });
    }

    private void showTransactionDialog() {

        LayoutInflater inflater = LayoutInflater.from(this);
        final View mDialogView = inflater.inflate(R.layout.dialog_transaction, (ViewGroup) findViewById(R.id.dialog_linear_layout));

        AlertDialog.Builder db = new AlertDialog.Builder(this);
        ButterKnife.bind(this, mDialogView);
        db.setView(mDialogView);
        db.setTitle(R.string.transacion_dialog_title);

        mTransactionDate.setShowSoftInputOnFocus(false);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        mTransactionDate.setText(dateFormat.format(new Date()).toString());
        mTransactionDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Time today = new Time(Time.getCurrentTimezone());
                Calendar calendar = Calendar.getInstance();

                DatePickerDialog dialog = new DatePickerDialog(MainActivity.this, MainActivity.this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

        db.setPositiveButton(R.string.button_OK, new DialogInterface.OnClickListener()

        {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        db.setNegativeButton(R.string.button_Cancel, new DialogInterface.OnClickListener()

        {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        final AlertDialog dialog = db.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAmount.getText().toString().length() == 0) {
                    Snackbar snackbar = Snackbar.make(mDialogView, R.string.warning_Amount, Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.colorSnackbar));
                    snackbar.show();
                } else if (mTransactionDate.getText().toString().length() == 0) {
                    Snackbar snackbar = Snackbar.make(mDialogView, R.string.warning_Date, Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.colorSnackbar));
                    snackbar.show();
                } else {

                    //Save Transaction to the Database
                    final PortfolioEntry portfolioEntry = new PortfolioEntry(mCurrencySpinner.getSelectedItem().toString(), mAmount.getText().toString(), mTransactionDate.getText().toString());

                    getCoinPrice(mCurrencySpinner.getSelectedItem().toString());
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            mDb.portfolioDao().insertPortfolio(portfolioEntry);
                        }
                    });

                    refreshPortfolioData();


                    dialog.dismiss();
                }
            }
        });
    }

    private void refreshPortfolioData() {
        coin.clear();
        amount.clear();
        date.clear();
        coin.addAll(mDb.portfolioDao().loadAllCoins());
        amount.addAll(mDb.portfolioDao().loadAllAmounts());
        date.addAll(mDb.portfolioDao().loadAllDates());
    }

    public void getCoinPrice(final String coin) {

        new android.os.AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    //Create Instance of GETAPIRequest and call it's
                    //request() method
                    String url = getString(R.string.cc_price_prefix) + coin + getString(R.string.cc_tsysms) + prefCurrency;

                    GetAPIRequest getapiRequest = new GetAPIRequest();
                    getapiRequest.request(MainActivity.this, fetchGetResultListener, url);

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
                    double amount = Double.parseDouble(mAmount.getText().toString());
                    double price = data.optDouble(prefCurrency);
                    double value = amount * price;
                    DecimalFormat df = new DecimalFormat("#.##");
                    values.add(Double.toString(Double.valueOf(df.format(value))));
                    setupViewModel();
                    calculateSumPortfolio();
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

    public void updatePortfolioPrices(final String coinString) {

        new android.os.AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try

                {
                    //Create Instance of GETAPIRequest and call it's
                    //request() method
                    String url = getString(R.string.cc_pricemutli_prefix) + coinString + getString(R.string.cc_tsysms) + prefCurrency;

                    GetAPIRequest getapiRequest = new GetAPIRequest();
                    getapiRequest.request(MainActivity.this, fetchGetPricesResultListener, url);
                } catch (
                        Exception e)

                {
                    e.printStackTrace();
                }

                return null;
            }
        }.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR);
    }

    FetchDataListener fetchGetPricesResultListener = new FetchDataListener() {
        @Override
        public void onFetchComplete(JSONObject data) {
            //Fetch Complete. Now stop progress bar  or loader
            //you started in onFetchStart
            RequestQueueService.cancelProgressDialog();
            try {
                //Check result sent by our GETAPIRequest class
                if (data != null) {
                    Iterator<String> keysItr = data.keys();

                    //Iterating over the JSON and save values
                    int i = 0;
                    while (keysItr.hasNext()) {
                        String key = keysItr.next();
                        JSONObject coinObject = data.optJSONObject(key);

                        //Get the PRICE value and format it to 2 decimals
                        DecimalFormat df = new DecimalFormat("#.##");
                        Double coinPrice = (Double.valueOf(df.format(coinObject.optDouble(prefCurrency))));
                        double value = Double.parseDouble(amount.get(i)) * coinPrice;

                        values.add(Double.toString(Double.valueOf(df.format(value))));
                        i++;
                    }

                    mPortfolioAdapter = new PortfolioAdapter(MainActivity.this, MainActivity.this, values);

                    // Assign adapter to ListView
                    mPortfolioRecyclerView.setAdapter(mPortfolioAdapter);

                    setupViewModel();

                    calculateSumPortfolio();

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
        intent.putExtra(Constants.EXTRA_CURRENCY, coin.get(position));
        startActivity(intent);
        this.overridePendingTransition(R.anim.slide_from_right, R.anim.fade_out);
    }

    private void calculateSumPortfolio() {

        mSumPortfolioTV = findViewById(R.id.tv_portfolio_value);

        mSumPortfolio = 0.0;
        for (int i = 0; i < values.size(); i++) {
            mSumPortfolio += Double.parseDouble(values.get(i));
        }

        DecimalFormat df = new DecimalFormat("#.##");
        mSumPortfolioTV.setText(Double.toString(Double.valueOf(df.format(mSumPortfolio))).concat(" " + currencySymbol));
    }

    @Override
    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {

        month++;
        mTransactionDate.setText(year + "." + month + "." + dayOfMonth);
    }

    @Override
    public void onItemClickListener(int itemId) {
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_currency_key))) {
            try {
                String currency = settings.getString("pref_currency_key", "USD");
                refreshPortfolioData();

                //Make a string from all of the coins
                StringBuilder allCoins = new StringBuilder();
                String prefix = "";
                for (String s : coin) {
                    allCoins.append(prefix);
                    prefix = ",";
                    allCoins.append(s);
                }

                if (coin.size() > 0) {
                    updatePortfolioPrices(allCoins.toString());
                }

                if (coin.size() < 1) {
                    mPortfolioAdapter = new PortfolioAdapter(MainActivity.this, this, values);

                    // Assign adapter to ListView
                    mPortfolioRecyclerView.setAdapter(mPortfolioAdapter);
                    setupViewModel();
                }
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
