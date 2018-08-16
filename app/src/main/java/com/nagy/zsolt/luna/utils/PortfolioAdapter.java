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
import com.nagy.zsolt.luna.data.database.PortfolioEntry;

import java.util.ArrayList;
import java.util.List;

public class PortfolioAdapter extends BaseAdapter {

    private final Context mContext;
    private final ArrayList<String> coin, date, amount, value;

    public PortfolioAdapter(Context context, ArrayList<String> coin, ArrayList<String> date, ArrayList<String> amount, ArrayList<String> value) {
        this.mContext = context;
        this.coin = coin;
        this.date = date;
        this.amount = amount;
        this.value = value;
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
            convertView = layoutInflater.inflate(R.layout.portfolio_list_item, null);
        }

//        final ImageView imageView = (ImageView) convertView.findViewById(R.id.thumbnail);
//        Picasso.with(mContext).load("http://image.tmdb.org/t/p/w185/" + moviePosterPath[position]).into(imageView);
//        imageView.setLayoutParams(new FrameLayout.LayoutParams(posterlWidth, posterHeight));
        TextView tt1 = (TextView) convertView.findViewById(R.id.coin);
        TextView tt2 = (TextView) convertView.findViewById(R.id.amount);
        TextView tt3 = (TextView) convertView.findViewById(R.id.value);
        TextView tt4 = (TextView) convertView.findViewById(R.id.transaction_date);

        tt1.setText(coin.get(position));
        tt2.setText(amount.get(position));
        tt3.setText(value.get(position));
        tt4.setText(date.get(position));

        return convertView;
    }
}