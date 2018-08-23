package com.nagy.zsolt.luna.utils;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nagy.zsolt.luna.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MarketAdapter extends BaseAdapter {

    private final Context mContext;
    private final ArrayList<String> coin, dailyChange, marketValue;

    public MarketAdapter(Context context, ArrayList<String> coin, ArrayList<String> dailyChange, ArrayList<String> marketValue) {
        this.mContext = context;
        this.coin = coin;
        this.dailyChange = dailyChange;
        this.marketValue = marketValue;
    }

    @Override
    public int getCount() {
        return coin.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.market_list_item, null);
        }

        int coinImgWidth = 120;
        int coinImgHeight = 120;

        final ImageView imageView = (ImageView) convertView.findViewById(R.id.coin_image);

        imageView.setLayoutParams(new FrameLayout.LayoutParams(coinImgWidth, coinImgHeight));
        TextView tt1 = (TextView) convertView.findViewById(R.id.market_coin);
        TextView tt2 = (TextView) convertView.findViewById(R.id.market_daily_change);
        TextView tt3 = (TextView) convertView.findViewById(R.id.market_value);

        String imageUrl = getImgURL(coin.get(position));
        Picasso.get().load("https://www.cryptocompare.com/" + imageUrl).into(imageView);

        tt1.setText(coin.get(position));
        tt3.setText(marketValue.get(position));

        //Colorize Market Daily Change
        if (Float.parseFloat(dailyChange.get(position)) > 0) {
            tt2.setTextColor(Color.GREEN);
            tt2.setText("+" + dailyChange.get(position).concat(" %"));
        } else {
            tt2.setTextColor(Color.RED);
            tt2.setText(dailyChange.get(position).concat(" %"));
        }

        return convertView;
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = mContext.getAssets().open("coin.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public String getImgURL(String crypto) {
        String imageUrl = null;
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset());
            imageUrl = obj.optJSONObject(crypto).getString("ImageUrl");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return imageUrl;
    }
}