package com.fcasado.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;

import com.fcasado.popularmovies.datatypes.Movie;
import com.fcasado.popularmovies.datatypes.Review;
import com.fcasado.popularmovies.datatypes.Trailer;
import com.fcasado.popularmovies.utils.Utilities;

import java.util.List;

/**
 * Ui-less fragment used to contain {@link FetchExtrasTask} in order to avoid leaks from multiple
 * tasks running due to activity recreation.
 */
public class FetchExtrasFragment extends Fragment
        implements FetchExtrasTask.OnMovieExtrasFetchFinished {
    private FetchExtrasTask mTask;
    private boolean mIsTaskFinished;
    private FetchExtrasTask.OnMovieExtrasFetchFinished mExtrasDataListener;
    private Pair<List<Trailer>, List<Review>> mExtras;
    private Movie mMovie;

    public void setOnMovieExtasFetchListener(FetchExtrasTask.OnMovieExtrasFetchFinished listener) {
        mExtrasDataListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            Movie movie = arguments.getParcelable(DetailFragment.DETAIL_MOVIE);
            if (movie != null) {
                refreshContent(movie);
            }

        }
    }

    public Pair<List<Trailer>, List<Review>> getMovieExtras() {
        return mExtras;
    }

    /**
     * We refresh data only if task is not running already to avoid pointless queries that will
     * result in same data.
     */
    public void refreshContent() {
        if (!Utilities.isConnected(getActivity())) {
            return;
        }

        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }

        if (mMovie != null) {
            mIsTaskFinished = false;
            mTask = new FetchExtrasTask(this);
            mTask.execute(mMovie);
        }
    }

    public void refreshContent(Movie movie) {
        if (mMovie != movie) {
            mMovie = movie;
            mExtras = null;
            refreshContent();
        }
    }

    @Override
    public void onMovieExtrasFetchFinished(Pair<List<Trailer>, List<Review>> movieExtras) {
        // Do we need to clear the task context reference?
        mIsTaskFinished = true;
        mTask = null;
        mExtras = movieExtras;
        if (mExtrasDataListener != null) {
            mExtrasDataListener.onMovieExtrasFetchFinished(mExtras);
        }
    }
}
