package com.fcasado.popularmovies;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.DumperPluginsProvider;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.dumpapp.DumperPlugin;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
