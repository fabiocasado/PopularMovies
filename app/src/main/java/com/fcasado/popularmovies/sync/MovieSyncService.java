package com.fcasado.popularmovies.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import timber.log.Timber;

public class MovieSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static MovieSyncAdapter sMoviesSyncAdapter = null;

    @Override
    public void onCreate() {
        Timber.d("onCreate");
        synchronized (sSyncAdapterLock) {
            if (sMoviesSyncAdapter == null) {
                sMoviesSyncAdapter = new MovieSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sMoviesSyncAdapter.getSyncAdapterBinder();
    }
}