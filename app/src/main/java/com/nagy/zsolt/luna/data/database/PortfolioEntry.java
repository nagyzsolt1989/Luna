package com.nagy.zsolt.luna.data.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "Portfolio")
public class PortfolioEntry {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String coin, amount, date;

    @Ignore
    public PortfolioEntry(String coin, String amount, String date) {
        this.coin = coin;
        this.date = date;
        this.amount = amount;
    }

    public PortfolioEntry(long id, String coin, String amount, String date) {
        this.id = id;
        this.coin = coin;
        this.date = date;
        this.amount = amount;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public int getId() {
        return (int) id;
    }

    public String getCoin() {
        return coin;
    }

    public String getDate() {
        return date;
    }

    public String getAmount() {
        return amount;
    }
}
