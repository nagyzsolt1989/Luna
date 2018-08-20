package com.nagy.zsolt.luna.data.api;

import android.content.Context;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class GetAPIRequest {

    public void request(final Context context, final FetchDataListener listener, String url) throws JSONException {
        if (listener != null) {
            //call onFetchComplete of the listener
            //to show progress dialog
            //-indicates calling started
            listener.onFetchStart();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (listener != null) {
                                if (response.has("BTC") || response.has("USD") || response.has("EUR")
                                        || response.has("RAW") || response.getString("Response").equals("Success")){
                                    //received response
                                    //call onFetchComplete of the listener
                                    listener.onFetchComplete(response);
                                } else if (response.getString("Response") == "Error") {
                                    //has error in response
                                    //call onFetchFailure of the listener
                                    listener.onFetchFailure(response.getString("error"));
                                } else {
                                    listener.onFetchComplete(null);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NoConnectionError) {
                    listener.onFetchFailure("Network Connectivity Problem");
                } else if (error.networkResponse != null && error.networkResponse.data != null) {
                    VolleyError volley_error = new VolleyError(new String(error.networkResponse.data));
                    String errorMessage = "";
                    try {
                        JSONObject errorJson = new JSONObject(volley_error.getMessage().toString());
                        if (errorJson.has("error")) errorMessage = errorJson.getString("error");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (errorMessage.isEmpty()) {
                        errorMessage = volley_error.getMessage();
                    }

                    if (listener != null) listener.onFetchFailure(errorMessage);
                } else {
                    listener.onFetchFailure("Something went wrong. Please try again later");
                }

            }
        });

        RequestQueueService.getInstance(context).addToRequestQueue(postRequest.setShouldCache(false));
    }
}

