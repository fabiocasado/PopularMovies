package com.fcasado.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.fcasado.popularmovies.datatypes.Movie;
import com.fcasado.popularmovies.utils.Utilities;

import java.util.List;

/**
 * Ui-less fragment used to contain {@link FetchMoviesTask} in order to avoid leaks from multiple
 * tasks running due to activity recreation.
 */
public class FetchMoviesFragment extends Fragment
        implements FetchMoviesTask.OnMovieDataFetchFinished {
    private FetchMoviesTask mTask;
    private boolean mIsTaskFinished;
    private FetchMoviesTask.OnMovieDataFetchFinished mMovieDataListener;
    private List<Movie> mMovies;

    public void setOnMovieDataFetchListener(FetchMoviesTask.OnMovieDataFetchFinished listener) {
        mMovieDataListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!mIsTaskFinished && mTask == null) {
            refreshContent();
        }
    }

    @Override
    public void onMovieDataFetchFinished(List<Movie> movies) {
        // Do we need to clear the task context reference?
        mIsTaskFinished = true;
        mTask = null;
        mMovies = movies;
        if (mMovieDataListener != null) {
            mMovieDataListener.onMovieDataFetchFinished(movies);
        }
    }

    public List<Movie> getMovieData() {
        return mMovies;
    }

    public void clearMovieData() {
        mMovies = null;
    }

    /**
     * We refresh data only if task is not running already to avoid pointless queries that will
     * result in same data.
     */
    public void refreshContent() {
        if (!Utilities.isConnected(getActivity())) {
            Utilities.presentOfflineDialog(getActivity());

            return;
        }

        if (mTask == null) {
            mIsTaskFinished = false;
            mTask = new FetchMoviesTask(getActivity(), this);
            mTask.execute();
        }
    }
}
