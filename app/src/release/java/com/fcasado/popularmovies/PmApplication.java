package com.fcasado.popularmovies;

import android.app.Application;

import timber.log.Timber;

/**
 * Application instance to use in debug environment. Will activate Timber for logging.
 */
public class PmApplication extends Application {
    public void onCreate() {
        super.onCreate();

        // Initialize timber with DebugTree
        Timber.plant(new ReleaseTree());
    }
}
