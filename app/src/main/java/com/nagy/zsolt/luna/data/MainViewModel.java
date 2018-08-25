package com.nagy.zsolt.luna.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.nagy.zsolt.luna.data.database.AppDatabase;
import com.nagy.zsolt.luna.data.database.PortfolioEntry;

import java.util.List;

public class MainViewModel extends AndroidViewModel{



    LiveData<List<PortfolioEntry>> mPortfolioEntries;

    public MainViewModel(@NonNull Application application) {
        super(application);
        AppDatabase mDb = AppDatabase.getsInstance(this.getApplication());
        Log.d("MainViewModel", "Actively retrieving the tasks form the Database");
        mPortfolioEntries = mDb.portfolioDao().loadAllPortfolioEntries();
    }

    public LiveData<List<PortfolioEntry>> getPortfolioEntries() {
        return mPortfolioEntries;
    }
}
