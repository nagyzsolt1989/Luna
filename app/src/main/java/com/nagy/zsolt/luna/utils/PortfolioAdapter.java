package com.nagy.zsolt.luna.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nagy.zsolt.luna.R;
import com.nagy.zsolt.luna.data.database.PortfolioEntry;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.PortfolioEntryViewHolder> {

    private final Context mContext;
    private final ArrayList<String> values;
    private List<PortfolioEntry> mPortfolioEntries;
    final private ItemClickListener mItemClickListener;


    public PortfolioAdapter(Context context, ItemClickListener listener, ArrayList<String> values) {
        this.mContext = context;
        this.mItemClickListener = listener;
        this.values = values;
    }

    /**
     * Called when ViewHolders are created to fill a RecyclerView.
     *
     * @return A new PortfolioViewHolder that holds the view for each portfolioEntry
     */
    @Override
    public PortfolioEntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the task_layout to a view
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.portfolio_list_item, parent, false);

        return new PortfolioEntryViewHolder(view);
    }

    /**
     * Called by the RecyclerView to display data at a specified position in the Cursor.
     *
     * @param holder   The ViewHolder to bind Cursor data to
     * @param position The position of the data in the Cursor
     */
    @Override
    public void onBindViewHolder(PortfolioEntryViewHolder holder, int position) {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        String prefCurrency = settings.getString("pref_currency_key", "USD");
        char currencySymbol = 0;
        if (prefCurrency.equals("USD")){
            currencySymbol = '$';
        }else if (prefCurrency.equals("EUR")){
            currencySymbol = '€';
        }

        // Determine the values of the wanted data
        PortfolioEntry portfolioEntry = mPortfolioEntries.get(position);
        String coin = portfolioEntry.getCoin();
        String amount = portfolioEntry.getAmount();
        String value = values.get(position);
        String trDate = portfolioEntry.getDate();
        String imageUrl = getImgURL(coin);


        //Set values
        holder.tvCoin.setText(coin);
        holder.tvAmount.setText(amount);
        holder.tvValue.setText(value.concat(" " + currencySymbol));
        holder.tvTrDate.setText(trDate);
        Picasso.get().load("https://www.cryptocompare.com/" + imageUrl).into(holder.ivCoin);
    }

    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
        if (mPortfolioEntries == null) {
            return 0;
        }
        return mPortfolioEntries.size();
    }

    public List<PortfolioEntry> getPortfolioEntries() {
        return mPortfolioEntries;
    }

    /**
     * When data changes, this method updates the list of portfolioEntries
     * and notifies the adapter to use the new values on it
     */
    public void setPortfolioEntries(List<PortfolioEntry> taskEntries) {
        mPortfolioEntries = taskEntries;
        notifyDataSetChanged();
    }

    public interface ItemClickListener {
        void onItemClickListener(int itemId);
    }

    class PortfolioEntryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView ivCoin;
        TextView tvCoin;
        TextView tvAmount;
        TextView tvValue;
        TextView tvTrDate;

        public PortfolioEntryViewHolder(View itemView) {
            super(itemView);

            ivCoin = itemView.findViewById(R.id.coin_image);
            tvCoin = itemView.findViewById(R.id.coin);
            tvAmount = itemView.findViewById(R.id.amount);
            tvValue = itemView.findViewById(R.id.value);
            tvTrDate = itemView.findViewById(R.id.transaction_date);
        }

        @Override
        public void onClick(View view) {
            int elementId = mPortfolioEntries.get(getAdapterPosition()).getId();
            mItemClickListener.onItemClickListener(elementId);
        }

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