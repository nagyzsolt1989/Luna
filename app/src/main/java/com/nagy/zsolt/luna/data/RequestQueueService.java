package com.nagy.zsolt.luna.data;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.nagy.zsolt.luna.R;

import java.util.HashMap;
import java.util.Map;

public class RequestQueueService {
    private static RequestQueueService mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;
    private static Dialog mProgressDialog;

    private RequestQueueService(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized RequestQueueService getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RequestQueueService(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue().add(req);
    }

    public Map<String, String> getRequestHeader() {
        Map<String, String> headerMap = new HashMap<>();
        return headerMap;
    }

    public void clearCache() {
        mRequestQueue.getCache().clear();
    }

    public void removeCache(String key) {
        mRequestQueue.getCache().remove(key);
    }

    //To show alert / error message
    public static void showAlert(String message, final Activity context) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Error!");
            builder.setMessage(message);
            builder.setPositiveButton("OK", null);

            builder.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //Start showing progress
    public static void showProgressDialog(final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    if (mProgressDialog.isShowing() == true) cancelProgressDialog();
                }

                mProgressDialog = new Dialog(activity);
                mProgressDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                mProgressDialog.setContentView(R.layout.progress_indicator);
                mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mProgressDialog.show();
                mProgressDialog.setCancelable(false);
            }
        });

    }

    //Stop showing progress
    public static void cancelProgressDialog() {
        if (mProgressDialog != null){
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    }

}
