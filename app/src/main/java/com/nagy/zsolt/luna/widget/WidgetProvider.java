package com.nagy.zsolt.luna.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nagy.zsolt.luna.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class WidgetProvider extends AppWidgetProvider {

    private static final String ACTION_CLICK = "ACTION_CLICK";
    private int widgetIdToUse;
    private char currencySymbol;

    @Override
    public void onUpdate(Context context, final AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        // Get all ids
        ComponentName thisWidget = new ComponentName(context,
                WidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {

            widgetIdToUse = widgetId;
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            String mSelectedCoin = mPrefs.getString("WIDGET_SELECTED_COIN", "coin was not in shared preferences");

            final String prefCurrency = mPrefs.getString("pref_currency_key", "USD");
            if (prefCurrency.equals("USD")){
                currencySymbol = '$';
            }else if (prefCurrency.equals("EUR")){
                currencySymbol = '€';
            }

            final RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);

            // Get the data from the rest service
            RequestQueue queue = Volley.newRequestQueue(context);

            String url = "https://min-api.cryptocompare.com/data/price?fsym=" + mSelectedCoin + "&tsyms=" + prefCurrency;

            System.out.println("Ez az URL" + url);

            JsonObjectRequest jsObjRequest = new JsonObjectRequest(
                    Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.w("WidgetExample",
                                    "Response => " + response.toString());
                            // Set the text\
                            try {
                                remoteViews.setTextViewText(R.id.widget_coin_price,
                                        response.getString(prefCurrency).concat(" " + currencySymbol));
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
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

            // Set the text
            Picasso.get()
                    .load("https://www.cryptocompare.com/media/19633/btc.png")
                    .into(remoteViews, R.id.widget_imageview, new int[] {widgetId});
//            remoteViews.setTextViewText(R.id.widget_coin_price, String.valueOf(number));

            // Register an onClickListener
            Intent intent = new Intent(context, WidgetProvider.class);

            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget_coin_price, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }
}