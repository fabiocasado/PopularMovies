package com.fcasado.popularmovies;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.DumperPluginsProvider;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.dumpapp.DumperPlugin;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Application instance to use in debug environment. Will activate Stetho and Timber for easier development.
 */
public class PmApplication extends Application {
    public void onCreate() {
        super.onCreate();

        // Initialize timber with DebugTree
        Timber.plant(new Timber.DebugTree() {
            @Override
            protected String createStackElementTag(StackTraceElement element) {
                return super.createStackElementTag(element) + ':' + element.getLineNumber();
            }
        });

        // Initialize Stetho only in debug environment
        final Context context = this;
        Stetho.initialize(
                Stetho.newInitializerBuilder(context)
                        .enableDumpapp(new SampleDumperPluginsProvider(context))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(context))
                        .build());
    }

    private static class SampleDumperPluginsProvider implements DumperPluginsProvider {
        private final Context mContext;

        public SampleDumperPluginsProvider(Context context) {
            mContext = context;
        }

        @Override
        public Iterable<DumperPlugin> get() {
            ArrayList<DumperPlugin> plugins = new ArrayList<DumperPlugin>();
            for (DumperPlugin defaultPlugin : Stetho.defaultDumperPluginsProvider(mContext).get()) {
                plugins.add(defaultPlugin);
            }
            // Add custom plugins here.
            return plugins;
        }
    }
}
