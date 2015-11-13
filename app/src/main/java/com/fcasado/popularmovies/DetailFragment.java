
package com.fcasado.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fcasado.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Shows movie details UI. Received movie URI in arguments.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String DETAIL_URI = "URI";

    // Movie columns indices. Must be updated if MOVIE_COLUMNS change.
    private static final int COL_POSTER_PATH = 1;
    private static final int COL_ORIGINAL_TITLE = 2;
    private static final int COL_OVERVIEW = 3;
    private static final int COL_USER_RATING = 4;
    private static final int COL_RELEASE_DATE = 5;

    // On details we only need movie poster.
    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry._ID, MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW, MovieContract.MovieEntry.COLUMN_USER_RATING,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE
    };
    private static final int DETAIL_LOADER = 0;

    private Uri mUri;

    @Bind(R.id.help_textview) TextView mHelpView;
    @Bind(R.id.movie_data_linear_layout) View mMovieDetailView;

    @Bind(R.id.title_textview) TextView mTitleView;
    @Bind(R.id.release_date_textview) TextView mReleaseDateView;
    @Bind(R.id.user_rating_textview) TextView mUserRatingView;
    @Bind(R.id.overview_textview) TextView mOverviewView;
    @Bind(R.id.poster_imageview) ImageView mPosterView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);

        boolean shouldShowHelpView = getResources().getBoolean(R.bool.show_help_view);
        if (shouldShowHelpView) {
            mHelpView.setVisibility(View.VISIBLE);
            mMovieDetailView.setVisibility(View.INVISIBLE);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(getActivity(), mUri, MOVIE_COLUMNS, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            // Hide help and show movie data
            if (mMovieDetailView.getVisibility() == View.INVISIBLE) {
                mHelpView.setVisibility(View.GONE);
                mMovieDetailView.setVisibility(View.VISIBLE);
            }

            // Read weather condition ID from cursor
            mTitleView.setText(data.getString(COL_ORIGINAL_TITLE));
            mReleaseDateView.setText(String.format("(%s)", data.getString(COL_RELEASE_DATE)));
            mUserRatingView.setText(String.format("%.1f/10", data.getFloat(COL_USER_RATING)));
            mOverviewView.setText(data.getString(COL_OVERVIEW));

            String portraitPath = data.getString(COL_POSTER_PATH);
            if (portraitPath != null && portraitPath.length() > 0) {
                portraitPath = getString(R.string.movie_poster_uri).concat(portraitPath);
                Picasso.with(getActivity()).load(portraitPath)
                        .placeholder(R.drawable.ic_poster_details).noFade().into(mPosterView);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
