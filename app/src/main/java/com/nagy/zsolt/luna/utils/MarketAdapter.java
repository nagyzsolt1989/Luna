package com.nagy.zsolt.luna.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nagy.zsolt.luna.R;

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

//        final ImageView imageView = (ImageView) convertView.findViewById(R.id.thumbnail);
//        Picasso.with(mContext).load("http://image.tmdb.org/t/p/w185/" + moviePosterPath[position]).into(imageView);
//        imageView.setLayoutParams(new FrameLayout.LayoutParams(posterlWidth, posterHeight));
        TextView tt1 = (TextView) convertView.findViewById(R.id.market_coin);
        TextView tt2 = (TextView) convertView.findViewById(R.id.market_daily_change);
        TextView tt3 = (TextView) convertView.findViewById(R.id.market_value);

        tt1.setText(coin.get(position));
        tt2.setText(dailyChange.get(position));
        tt3.setText(marketValue.get(position));

        return convertView;
    }
}