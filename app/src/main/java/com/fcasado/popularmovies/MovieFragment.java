
package com.fcasado.popularmovies;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.fcasado.popularmovies.data.MovieContract;

/**
 * Created by fcasado on 05/11/2015.
 */
public class MovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    // Movie columns indices. Must be updated if MOVIE_COLUMNS change.
    static final int COL_POSTER_PATH = 1;
    private static final String TAG_FETCH_MOVIE = "tagFetchMovie";

    // On gridView we only need movie poster.
    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry._ID, MovieContract.MovieEntry.COLUMN_POSTER_PATH
    };
    private static final int MOVIE_LOADER_ID = 100;
    private FetchMovieFragment mFetchMovieFragment;
    private GridView mGridView;
    private MovieAdapter mMovieAdapter;

    public MovieFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mMovieAdapter = new MovieAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Update gridview's reference and adapter
        mGridView = (GridView) rootView.findViewById(R.id.gridview_movie);
        mGridView.setAdapter(mMovieAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        FragmentManager fm = getFragmentManager();

        // Check to see if we have retained the movie data fetching fragment.
        mFetchMovieFragment = (FetchMovieFragment) fm.findFragmentByTag(TAG_FETCH_MOVIE);

        // If not retained (or first time running), we need to create it.
        if (mFetchMovieFragment == null) {
            mFetchMovieFragment = new FetchMovieFragment();
            // Tell it who it is working with.
            mFetchMovieFragment.setTargetFragment(this, 0);
            fm.beginTransaction().add(mFetchMovieFragment, TAG_FETCH_MOVIE).commit();
        }

        getLoaderManager().initLoader(MOVIE_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created. This
        // fragment only uses one loader, so we don't care about checking the id.

        String sortOrder = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC";
        return new CursorLoader(getActivity(), MovieContract.MovieEntry.CONTENT_URI, MOVIE_COLUMNS,
                null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }
}
