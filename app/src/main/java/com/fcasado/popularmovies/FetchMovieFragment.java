
package com.fcasado.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.fcasado.popularmovies.data.MovieContract;
import com.fcasado.popularmovies.utils.Utilities;

/**
 * Created by fcasado on 05/11/2015.
 */
public class FetchMovieFragment extends Fragment
        implements FetchMovieTask.OnMovieDataFetchFinished {
    private static final String LOG_TAG = FetchMovieFragment.class.getSimpleName();
    private FetchMovieTask mTask;
    private boolean mIsTaskFinished;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // When starting the app, we delete old content since we are going to fetch updated one
        getActivity().getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI, null, null);
        Log.d(LOG_TAG, "Content in DB deleted");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!mIsTaskFinished && mTask == null) {
            refreshContent();
        }
    }

    @Override
    public void onMovieDataFetchFinished() {
        // Do we need to clear the task context reference?
        mIsTaskFinished = true;
        mTask = null;
    }

    /**
     * We refresh data only if task is not running already to avoid pointless queries that will
     * result in same data.
     */
    public void refreshContent() {
        if (!Utilities.isConnected(getActivity())) {
            Utilities.presentOfflineDialog(getActivity());

            // If we were trying to do a new fetch, but we are offline, we still delete old records,
            // since some images and data may not be there, thus creating a weird/bad UX
            int deleted = getActivity().getContentResolver()
                    .delete(MovieContract.MovieEntry.CONTENT_URI, null, null);
            Log.d(LOG_TAG, "No internet connection. Deleting old data to avoid bad UX. " + deleted
                    + " deleted");
            return;
        }

        if (mTask == null) {
            mIsTaskFinished = false;
            mTask = new FetchMovieTask(getActivity(), this);
            mTask.execute();
        }
    }
}
