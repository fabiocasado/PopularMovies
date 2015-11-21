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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.fcasado.popularmovies.data.FavoriteContract;
import com.fcasado.popularmovies.datatypes.Movie;
import com.fcasado.popularmovies.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Shows movie list ui. If we are in two pane mode, it automatically scrolls to selected movie on
 * screen rotation or similar events. It contains the {@link FetchMoviesFragment} to handle Movie
 * API data query.
 */
public class MovieFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, FetchMoviesTask.OnMovieDataFetchFinished {

    private static final String TAG_FETCH_MOVIE = "tagFetchMovie";
    private static final String SHOULD_SCROLL = "shouldScroll";
    private static final String SELECTED_POSITION = "selectedPosition";
    private static final int FAVORITE_LOADER_ID = 100;

    @Bind(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.recyclerview)
    RecyclerView mRecyclerView;
    @Bind(R.id.recyclerview_empty_textview)
    TextView mEmptyView;

    private FetchMoviesFragment mFetchMoviesFragment;
    private MovieAdapter mMovieAdapter;

    private boolean mShouldScrollToSelectedItem;
    private int mSelectedPosition = RecyclerView.NO_POSITION;
    private String mSortByValue;

    public MovieFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!Utilities.isConnected(getActivity())) {
                    Utilities.presentOfflineDialog(getActivity());

                    mSwipeRefreshLayout.setRefreshing(false);
                    return;
                }

                // Reset current item since we are refreshing data
                mSelectedPosition = RecyclerView.NO_POSITION;
                mFetchMoviesFragment.refreshContent();
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                getResources().getInteger(R.integer.grid_columns)));

        // Restore saved values if available
        if (savedInstanceState != null) {
            mSelectedPosition = savedInstanceState.getInt(SELECTED_POSITION,
                    RecyclerView.NO_POSITION);
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
        if (mSortByValue.compareTo(prefSortBy) != 0) {
            mSortByValue = prefSortBy;

            // Clear old data
            mFetchMoviesFragment.clearMovieData();
            mMovieAdapter.setMovies(null);

            // Get new values
            // If we want favorites, we get new values from loader
            if (mSortByValue.compareTo(getString(R.string.sort_favorite)) == 0) {
                getLoaderManager().restartLoader(FAVORITE_LOADER_ID, null, this);
                mSwipeRefreshLayout.setEnabled(false);
            } else {
                // If not, we get them from server
                getLoaderManager().destroyLoader(FAVORITE_LOADER_ID);
                mFetchMoviesFragment.refreshContent();
                mSwipeRefreshLayout.setEnabled(true);
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        FragmentManager fm = getFragmentManager();

        // Check to see if we have retained the movie data fetching fragment.
        mFetchMoviesFragment = (FetchMoviesFragment) fm.findFragmentByTag(TAG_FETCH_MOVIE);

        // If not retained (or first time running), we need to create it.
        if (mFetchMoviesFragment == null) {
            mFetchMoviesFragment = new FetchMoviesFragment();
            // Tell it who it is working with.
            mFetchMoviesFragment.setTargetFragment(this, 0);
            fm.beginTransaction().add(mFetchMoviesFragment, TAG_FETCH_MOVIE).commit();
        }

        mFetchMoviesFragment.setTargetFragment(this, 0);
        mFetchMoviesFragment.setOnMovieDataFetchListener(this);

        mMovieAdapter = new MovieAdapter(getActivity(), mEmptyView, mFetchMoviesFragment.getMovieData(),
                new MovieAdapter.MovieAdapterOnClickListener() {
                    @Override
                    public void onClick(Movie movie, int position) {
                        ((MainActivity) getActivity()).onMovieItemSelected(movie);
                        mSelectedPosition = position;
                    }
                });
        mRecyclerView.setAdapter(mMovieAdapter);
        if (mSelectedPosition != RecyclerView.NO_POSITION && mShouldScrollToSelectedItem) {
            mRecyclerView.smoothScrollToPosition(mSelectedPosition);
        }

        if (mSortByValue.compareTo(getString(R.string.sort_favorite)) == 0) {
            mSwipeRefreshLayout.setEnabled(false);
            if (getLoaderManager().getLoader(FAVORITE_LOADER_ID) == null) {
                getLoaderManager().initLoader(FAVORITE_LOADER_ID, null, this);
            } else {
                getLoaderManager().restartLoader(FAVORITE_LOADER_ID, null, this);
            }
        } else {
            getLoaderManager().destroyLoader(FAVORITE_LOADER_ID);
            mSwipeRefreshLayout.setEnabled(true);
        }
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
     * @param shouldScrollToSelectedItem whether the fragment should scroll to selected item or not
     */
    public void setShouldScrollToSelectedItem(boolean shouldScrollToSelectedItem) {
        mShouldScrollToSelectedItem = shouldScrollToSelectedItem;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created. This
        // fragment only uses one loader, so we don't care about checking the id.
        return new CursorLoader(getActivity(), FavoriteContract.MovieEntry.CONTENT_URI,
                null,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        List<Movie> movies = new ArrayList<Movie>();
        if (data != null && data.getCount() > 0) {
            int idColumn = data.getColumnIndex(FavoriteContract.MovieEntry._ID);
            int titleColumn = data.getColumnIndex(FavoriteContract.MovieEntry.COLUMN_TITLE);
            int originalTitleColumn = data.getColumnIndex(FavoriteContract.MovieEntry.COLUMN_ORIGINAL_TITLE);
            int overviewColumn = data.getColumnIndex(FavoriteContract.MovieEntry.COLUMN_OVERVIEW);
            int posterPathColumn = data.getColumnIndex(FavoriteContract.MovieEntry.COLUMN_POSTER_PATH);
            int releaseDateColumn = data.getColumnIndex(FavoriteContract.MovieEntry.COLUMN_RELEASE_DATE);
            int popularityColumn = data.getColumnIndex(FavoriteContract.MovieEntry.COLUMN_POPULARITY);
            int userRatingColumn = data.getColumnIndex(FavoriteContract.MovieEntry.COLUMN_USER_RATING);

            while (data.moveToNext()) {
                long id = data.getLong(idColumn);
                String title = data.getString(titleColumn);
                String originalTitle = data.getString(originalTitleColumn);
                String overview = data.getString(overviewColumn);
                String posterPath = data.getString(posterPathColumn);
                String releaseDate = data.getString(releaseDateColumn);
                double popularity = data.getDouble(popularityColumn);
                double userRating = data.getDouble(userRatingColumn);

                movies.add(new Movie(id, title, originalTitle, overview, posterPath, releaseDate, popularity, userRating));
            }
        }

        mMovieAdapter.setMovies(movies);
        if (mSelectedPosition != ListView.INVALID_POSITION && mShouldScrollToSelectedItem) {
            // If there's a desired position to restore to, do so now.
            mRecyclerView.smoothScrollToPosition(mSelectedPosition);
        }

        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onMovieDataFetchFinished(List<Movie> movies) {
        mMovieAdapter.setMovies(movies);
        if (mSelectedPosition != RecyclerView.NO_POSITION && mShouldScrollToSelectedItem) {
            mRecyclerView.smoothScrollToPosition(mSelectedPosition);
        }

        mSwipeRefreshLayout.setRefreshing(false);
    }

    public interface OnMovieItemSelected {
        void onMovieItemSelected(Movie movie);
    }
}
