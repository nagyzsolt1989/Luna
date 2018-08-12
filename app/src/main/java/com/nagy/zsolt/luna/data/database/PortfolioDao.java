package com.nagy.zsolt.luna.data.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface PortfolioDao {

    @Query("SELECT * FROM Portfolio ORDER BY date")
    List<PortfolioEntry> loadAllPortfolioEntries();

    @Query("SELECT coin FROM Portfolio ORDER BY date")
    String[] loadAllCoins();

    @Query("SELECT amount FROM Portfolio ORDER BY date")
    String[] loadAllAmounts();

    @Query("SELECT date FROM Portfolio ORDER BY date")
    String[] loadAllDates();

    @Insert
    void insertPortfolio(PortfolioEntry portfolioEntry);

    @Delete
    void deletePortfolio(PortfolioEntry portfolioEntry);
}
