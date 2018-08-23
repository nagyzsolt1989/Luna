package com.nagy.zsolt.luna.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.nagy.zsolt.luna.R;

import org.json.JSONArray;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

public class AppWidgetConfigureActivity extends AppCompatActivity {

    @Nullable
    @BindView(R.id.widget_currency_spinner)
    Spinner mCurrencySpinner;
    @Nullable
    @BindView(R.id.btn_create)
    AppCompatButton mCreateBtn;
    public int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    Context mContext;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        ButterKnife.bind(this);
        setContentView(R.layout.widget_configure);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        mContext = getApplicationContext();

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        mCurrencySpinner = findViewById(R.id.widget_currency_spinner);
        mCreateBtn = findViewById(R.id.btn_create);
        mCreateBtn.setOnClickListener(new View.OnClickListener() {

            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor prefsEditor = mPrefs.edit();

            @Override
            public void onClick(View v) {

                if (getIntent().hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
                    mAppWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID);

                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(AppWidgetConfigureActivity.this);

                    Intent intent = new Intent(AppWidgetConfigureActivity.this, WidgetProvider.class);
                    intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
                    int ids[] = AppWidgetManager.getInstance(
                            getApplication()).getAppWidgetIds(new ComponentName(getApplication(), WidgetProvider.class));

                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);

                    prefsEditor.putString("WIDGET_SELECTED_COIN", mCurrencySpinner.getSelectedItem().toString());
                    prefsEditor.commit();

                    AppWidgetConfigureActivity.this.sendBroadcast(intent);

                    int appWidgetIds[] = appWidgetManager.getAppWidgetIds(
                            new ComponentName(AppWidgetConfigureActivity.this, WidgetProvider.class));

                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_listview);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    mAppWidgetId = INVALID_APPWIDGET_ID;
                    Intent intent = getIntent();
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                                INVALID_APPWIDGET_ID);

                        AppWidgetProviderInfo providerInfo = AppWidgetManager.getInstance(
                                getBaseContext()).getAppWidgetInfo(mAppWidgetId);

                        Intent startWidget = new Intent(AppWidgetConfigureActivity.this,
                                WidgetProvider.class);
                        startWidget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                        startWidget.setAction("FROM CONFIGURATION ACTIVITY");
                        setResult(RESULT_OK, startWidget);

                        finish();
                    }
                    if (mAppWidgetId == INVALID_APPWIDGET_ID) {
                        Log.i(AppWidgetConfigureActivity.class.getSimpleName(), "Invalid app widget ID");
                        finish();
                    }
                }
            }
        });
    }
}
