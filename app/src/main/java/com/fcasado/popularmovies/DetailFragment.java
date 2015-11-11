
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
import android.widget.RatingBar;
import android.widget.TextView;

import com.fcasado.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * Created by fcasado on 10/11/2015.
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

    private TextView mTitleView;
    private TextView mReleaseDateView;
    private TextView mOverviewView;
    private ImageView mPosterView;
    private RatingBar mUserRatingBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mTitleView = (TextView) rootView.findViewById(R.id.title_textview);
        mReleaseDateView = (TextView) rootView.findViewById(R.id.release_date_textview);
        mOverviewView = (TextView) rootView.findViewById(R.id.overview_textview);
        mPosterView = (ImageView) rootView.findViewById(R.id.poster_imageview);
        mUserRatingBar = (RatingBar) rootView.findViewById(R.id.user_rating_ratingbar);

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
            // Read weather condition ID from cursor
            mTitleView.setText(data.getString(COL_ORIGINAL_TITLE));
            mReleaseDateView.setText(String.format("(%s)", data.getString(COL_RELEASE_DATE)));
            mOverviewView.setText(data.getString(COL_OVERVIEW));
            mUserRatingBar.setRating(data.getFloat(COL_USER_RATING) / 2);

            String portraitPath = data.getString(COL_POSTER_PATH);
            if (portraitPath != null && portraitPath.length() > 0) {
                portraitPath = getString(R.string.movie_poster_uri).concat(portraitPath);
                Picasso.with(getActivity()).load(portraitPath).noFade().noPlaceholder()
                        .into(mPosterView);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
