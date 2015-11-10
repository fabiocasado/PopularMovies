
package com.fcasado.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;

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
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mIsTaskFinished == false && mTask == null) {
            mTask = new FetchMovieTask(getActivity(), this);
            mTask.execute();
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
        if (mTask == null) {
            mIsTaskFinished = false;
            mTask = new FetchMovieTask(getActivity(), this);
            mTask.execute();
        }
    }
}
