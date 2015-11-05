
package com.fcasado.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by fcasado on 05/11/2015.
 */
public class FetchMovieFragment extends Fragment
        implements FetchMovieTask.OnMovieDataFetchFinished {
    private static final String LOG_TAG = FetchMovieFragment.class.getSimpleName();
    private FetchMovieTask mTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    public void fetchMovieData(String sortValue, int pageValue) {
        Log.d(LOG_TAG, "fetchMovieData: " + sortValue + ", " + pageValue);
        if (mTask == null) {
            mTask = new FetchMovieTask(getActivity(), sortValue, pageValue, this);
            mTask.execute();
        }
    }

    @Override
    public void onMovieDataFetchFinished() {
        // Do we need to clear the task context reference?
        mTask = null;
    }
}
