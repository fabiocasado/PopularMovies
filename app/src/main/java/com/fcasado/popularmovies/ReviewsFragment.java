
package com.fcasado.popularmovies;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.fcasado.popularmovies.adapters.ReviewAdapter;
import com.fcasado.popularmovies.data.MovieContract;

import timber.log.Timber;

/**
 * Created by fcasado on 19/11/2015.
 */
public class ReviewsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    // Review columns indices. Must be updated if REVIEW_COLUMNS change.
    public static final int COL_AUTHOR = 1;
    public static final int COL_CONTENT = 2;
    static final String MOVIE_ID = "MOVIE_ID";
    // On reviews we only need author and review.
    private static final String[] REVIEW_COLUMNS = {
            MovieContract.ReviewEntry._ID, MovieContract.ReviewEntry.COLUMN_AUTHOR,
            MovieContract.ReviewEntry.COLUMN_CONTENT
    };

    private static final int REVIEW_LOADER_ID = 100;
    @Bind(R.id.reviews_recyclerview)
    RecyclerView mRecyclerView;
    private long mMovieId;
    private ReviewAdapter mReviewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_reviews, container, false);
        ButterKnife.bind(this, rootView);

        mReviewAdapter = new ReviewAdapter();

        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                getResources().getInteger(R.integer.review_grid_columns),
                StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mReviewAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (getActivity().getIntent().hasExtra(MOVIE_ID)) {
            mMovieId = getActivity().getIntent().getLongExtra(MOVIE_ID, -1);
            Timber.d("Movie id: " + mMovieId);
        }

        getLoaderManager().initLoader(REVIEW_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), MovieContract.ReviewEntry.CONTENT_URI,
                REVIEW_COLUMNS, MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " =?", new String[] {
                        String.valueOf(mMovieId)
        }, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mReviewAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mReviewAdapter.swapCursor(null);
    }
}
