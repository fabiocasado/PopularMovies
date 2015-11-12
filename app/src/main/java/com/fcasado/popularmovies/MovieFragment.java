
package com.fcasado.popularmovies;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.fcasado.popularmovies.data.MovieContract;
import com.fcasado.popularmovies.utils.Utilities;

/**
 * Created by fcasado on 05/11/2015.
 */
public class MovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    // Movie columns indices. Must be updated if MOVIE_COLUMNS change.
    static final int COL_POSTER_PATH = 1;

    // On gridView we only need movie poster.
    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry._ID, MovieContract.MovieEntry.COLUMN_POSTER_PATH
    };

    private static final String TAG_FETCH_MOVIE = "tagFetchMovie";
    private static final String SHOULD_SCROLL = "shouldScroll";
    private static final String SELECTED_POSITION = "selectedPosition";
    private static final int MOVIE_LOADER_ID = 100;

    private FetchMovieFragment mFetchMovieFragment;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private GridView mGridView;
    private MovieAdapter mMovieAdapter;

    private boolean mShouldScrollToSelectedItem;
    private int mSelectedPosition = GridView.INVALID_POSITION;
    private String mSortByValue;

    public MovieFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mMovieAdapter = new MovieAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!Utilities.isConnected(getActivity())) {
                    Utilities.presentOfflineDialog(getActivity());
                    mSwipeRefreshLayout.setRefreshing(false);
                    return;
                }

                // Reset current item since we are refreshing data
                mSelectedPosition = GridView.INVALID_POSITION;
                mFetchMovieFragment.refreshContent();
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        mGridView = (GridView) rootView.findViewById(R.id.gridview);
        mGridView.setAdapter(mMovieAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    ((OnMovieItemSelected) getActivity()).onMovieItemSelected(
                            MovieContract.MovieEntry.buildMovieUri(id),
                            view.findViewById(R.id.poster_imageview));
                }
                mSelectedPosition = position;
            }
        });

        // Restore saved values if available
        if (savedInstanceState != null) {
            mSelectedPosition = savedInstanceState.getInt(SELECTED_POSITION,
                    GridView.INVALID_POSITION);
        }

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        mSortByValue = preferences.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // When we are presenting this fragment again, the Sort By setting may have change, so we do
        // a simple check of our current value and the one stored in the app's preference. If values
        // are different, we update our own and restart the loader. This way we keep the logic for
        // restarting the loader or not in the fragment, and don't depend on external
        // activities/fragments/others to tell us to update our sort value and reload
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        String prefSortBy = preferences.getString(getString(R.string.pref_sort_key),
                getString(R.string.sort_most_popular));
        if (mSortByValue != prefSortBy) {
            mSortByValue = prefSortBy;
            getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
        }
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
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SHOULD_SCROLL, mShouldScrollToSelectedItem);

        // Save selected position when we handle rotation or similar.
        if (mSelectedPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_POSITION, mSelectedPosition);
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * Tells the fragment to activate smooth scrolling to selected item on rotate/similar events.
     * 
     * @param shouldScrollToSelectedItem
     */
    public void setShouldScrollToSelectedItem(boolean shouldScrollToSelectedItem) {
        mShouldScrollToSelectedItem = shouldScrollToSelectedItem;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created. This
        // fragment only uses one loader, so we don't care about checking the id.

        String sortOrder = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC";
        if (mSortByValue.equalsIgnoreCase(getString(R.string.sort_highest_rated))) {
            sortOrder = MovieContract.MovieEntry.COLUMN_USER_RATING + " DESC";
        }

        return new CursorLoader(getActivity(), MovieContract.MovieEntry.CONTENT_URI, MOVIE_COLUMNS,
                null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieAdapter.swapCursor(data);
        if (mSelectedPosition != ListView.INVALID_POSITION && mShouldScrollToSelectedItem) {
            // If there's a desired position to restore to, do so now.
            mGridView.smoothScrollToPosition(mSelectedPosition);
        }

        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }

    /**
     * A callback to notify activities of item selections.
     */
    public interface OnMovieItemSelected {
        /**
         * DetailFragmentCallback for when an item has been selected. It also allows the setting of
         * a sharedView to use in activity transitions (should be poster view)
         */
        void onMovieItemSelected(Uri contentUri, View sharedView);
    }
}
