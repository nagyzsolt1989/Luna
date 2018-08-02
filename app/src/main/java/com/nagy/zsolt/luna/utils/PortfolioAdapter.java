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

public class PortfolioAdapter extends BaseAdapter {

    private final Context mContext;
    private final String coin, date, value;

    public PortfolioAdapter(Context context, String coin, String date, String value) {
        this.mContext = context;
        this.coin = coin;
        this.date = date;
        this.value = value;
    }

    @Override
    public int getCount() {
        return 0;
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

        int posterlWidth = 300;
        int posterHeight = 450;

        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.portfolio_list_item, null);
        }

        final ImageView imageView = (ImageView) convertView.findViewById(R.id.thumbnail);
//        Picasso.with(mContext).load("http://image.tmdb.org/t/p/w185/" + moviePosterPath[position]).into(imageView);
//        imageView.setLayoutParams(new FrameLayout.LayoutParams(posterlWidth, posterHeight));
        TextView tt1 = (TextView) convertView.findViewById(R.id.coin);
        TextView tt2 = (TextView) convertView.findViewById(R.id.date);
        TextView tt3 = (TextView) convertView.findViewById(R.id.value);

        return convertView;
    }
}