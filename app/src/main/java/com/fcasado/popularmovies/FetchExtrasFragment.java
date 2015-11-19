
package com.fcasado.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;

import com.fcasado.popularmovies.data.Movie;
import com.fcasado.popularmovies.data.Review;
import com.fcasado.popularmovies.data.Trailer;
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

        Bundle arguments = getArguments();
        if (arguments != null) {
            mMovie = arguments.getParcelable(DetailFragment.DETAIL_MOVIE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!mIsTaskFinished && mTask == null) {
            refreshContent();
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
            Utilities.presentOfflineDialog(getActivity());

            return;
        }

        if (mTask == null) {
            mIsTaskFinished = false;
            mTask = new FetchExtrasTask(this);
            mTask.execute(mMovie);
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
