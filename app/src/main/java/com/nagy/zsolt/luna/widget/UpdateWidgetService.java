package com.nagy.zsolt.luna.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nagy.zsolt.luna.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class UpdateWidgetService extends Service {
    private static final String LOG = "de.vogella.android.widget.example";
    private int widgetIdToUse;
    private char currencySymbol;

    @Override
    public void onStart(Intent intent, int startId) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
                .getApplicationContext());

        int[] allWidgetIds = intent
                .getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

        for (int widgetId : allWidgetIds) {
            widgetIdToUse = widgetId;
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            String mSelectedCoin = mPrefs.getString("WIDGET_SELECTED_COIN", "coin was not in shared preferences");

            final String prefCurrency = mPrefs.getString(getString(R.string.pref_currency_key), "USD");
            if (prefCurrency.equals("USD")) {
                currencySymbol = '$';
            } else if (prefCurrency.equals("EUR")) {
                currencySymbol = '€';
            }

            // create some random data
            int number = (new Random().nextInt(100));

            final RemoteViews remoteViews = new RemoteViews(this
                    .getApplicationContext().getPackageName(),
                    R.layout.widget_layout);

            // Get the data from the rest service
            RequestQueue queue = Volley.newRequestQueue(this);

            String url = "https://min-api.cryptocompare.com/data/price?fsym=" + mSelectedCoin + "&tsyms=" + prefCurrency;

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(
                    Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.w("WidgetExample",
                                    "Response => " + response.toString());
                            // Set the text\
                            remoteViews.setTextViewText(R.id.widget_coin_price,
                                    response.optString(prefCurrency));
                            appWidgetManager.updateAppWidget(widgetIdToUse, remoteViews);
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO Auto-generated method stub
                    Log.w("WidgetExample",
                            "Error => " + error.toString());
                    remoteViews.setTextViewText(R.id.widget_coin_price,
                            "Error => " + error.toString());
                    appWidgetManager.updateAppWidget(widgetIdToUse, remoteViews);
                }
            });
            queue.add(jsObjRequest);

            Log.w("WidgetExample", String.valueOf(number));
            // Set the text
            remoteViews.setTextViewText(R.id.widget_coin_price,
                    "Random: " + String.valueOf(number));

            // Register an onClickListener
            Intent clickIntent = new Intent(this.getApplicationContext(),
                    WidgetProvider.class);

            clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                    allWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    getApplicationContext(), 0, clickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget_coin_price, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
        stopSelf();

        super.onStart(intent, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
