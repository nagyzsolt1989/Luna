package com.nagy.zsolt.luna.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
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
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.nagy.zsolt.luna.R;
import com.nagy.zsolt.luna.data.Constants;
import com.nagy.zsolt.luna.utils.SwipeDismissListViewTouchListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Optional;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Nullable @BindView(R.id.lv_portfolio)
    ListView mPortfolioListView;
    @Nullable @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @Nullable @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @Nullable @BindView(R.id.nav_view)
    NavigationView navigationView;
    @Nullable @BindView(R.id.fab)
    FloatingActionButton fab;
    @Nullable @BindView(R.id.currency)
    EditText mTransactionCurrency;
    private List<String> mPortfolioValues;
    private ArrayAdapter<String> adapter;
    private ActionBarDrawerToggle toggle;

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

        mPortfolioValues = new ArrayList<String>(Arrays.asList( "Android", "iPhone", "WindowsMobile",
                "Blackberry"));

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mPortfolioValues);


        // Assign adapter to ListView
        mPortfolioListView.setAdapter(adapter);

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

                                    mPortfolioValues.remove(position);
                                    adapter.notifyDataSetChanged();

                                }

                            }
                        });
        mPortfolioListView.setOnTouchListener(touchListener);
//        mPortfolioListView.setOnClickListener();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            // your action...

            if (mDrawer.isDrawerOpen(GravityCompat.START)) {
                mDrawer.closeDrawer(GravityCompat.START);}
            return true;
        }
        return super.onKeyDown(keyCode, e);
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


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            mDrawer.openDrawer(Gravity.LEFT);
            toggle.syncState();
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

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showTransactionDialog() {

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialog_layout = inflater.inflate(R.layout.dialog_transaction,(ViewGroup) findViewById(R.id.dialog_linear_layout));

        AlertDialog.Builder db = new AlertDialog.Builder(this);
        ButterKnife.bind(this, dialog_layout);
        db.setView(dialog_layout);
        db.setTitle("New Transaction");
        db.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                List<String> tempPortfolioValues = new ArrayList<String>();
                tempPortfolioValues.addAll(mPortfolioValues);
                tempPortfolioValues.add(mTransactionCurrency.getEditableText().toString());
                mPortfolioValues.clear();
                mPortfolioValues.addAll(tempPortfolioValues);
                adapter.notifyDataSetChanged();
            }
        });
        db.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = db.show();
    }

    private void launchDetailActivity(int position) {
//        Intent intent = new Intent(this, DetailActivity.class);
//        intent.putExtra(Constants.EXTRA_POSITION, position);
//        intent.putExtra(Constants.EXTRA_CURRENCY, );
//        startActivity(intent);
//        this.overridePendingTransition(R.anim.slide_from_right, R.anim.fade_out);
    }
}
