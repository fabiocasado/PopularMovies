
package com.fcasado.popularmovies;

import android.content.ContentUris;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.fcasado.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

import timber.log.Timber;

/**
 * Shows movie details UI. Received movie URI in arguments.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String DETAIL_URI = "URI";

    // Movie columns indices. Must be updated if MOVIE_COLUMNS change.
    private static final int COL_MOVIE_POSTER_PATH = 1;
    private static final int COL_MOVIE_ORIGINAL_TITLE = 2;
    private static final int COL_MOVIE_OVERVIEW = 3;
    private static final int COL_MOVIE_USER_RATING = 4;
    private static final int COL_MOVIE_RELEASE_DATE = 5;

    // Movie data needed on details.
    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry._ID, MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW, MovieContract.MovieEntry.COLUMN_USER_RATING,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE
    };

    private static final int COL_TRAILER_VIDEO_KEY = 0;
    private static final String[] TRAILER_COLUMNS = {
            MovieContract.TrailerEntry.COLUMN_VIDEO_KEY
    };

    private static final int DETAIL_LOADER = 0;
    private static final int TRAILER_LOADER = 1;
    private static final int REVIEW_LOADER = 2;

    @Bind(R.id.help_textview)
    TextView mHelpView;
    @Bind(R.id.movie_data_linear_layout)
    ViewGroup mMovieDetailView;
    @Bind(R.id.title_textview)
    TextView mTitleView;
    @Bind(R.id.release_date_textview)
    TextView mReleaseDateView;
    @Bind(R.id.user_rating_textview)
    TextView mUserRatingView;
    @Bind(R.id.overview_textview)
    TextView mOverviewView;
    @Bind(R.id.poster_imageview)
    ImageView mPosterView;
    @Bind(R.id.trailer_button_container)
    LinearLayout mTrailerButtonContainer;

    private Uri mUri;
    private View.OnClickListener mTrailerButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent("android.intent.action.VIEW",
                    Uri.parse((String) v.getTag()));
            startActivity(intent);
        }
    };

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
        getLoaderManager().initLoader(TRAILER_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case DETAIL_LOADER:
                if (null != mUri) {
                    return new CursorLoader(getActivity(), mUri, MOVIE_COLUMNS, null, null, null);
                }
                break;
            case TRAILER_LOADER:
                if (null != mUri) {
                    long movieId = ContentUris.parseId(mUri);
                    return new CursorLoader(getActivity(), MovieContract.TrailerEntry.CONTENT_URI,
                            TRAILER_COLUMNS, MovieContract.TrailerEntry.COLUMND_MOVIE_ID + " =?",
                            new String[] {
                                    String.valueOf(movieId)
                    }, null);
                }
                break;

            default:
                Timber.d("Invalid loader id: " + id);
                break;
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            switch (loader.getId()) {
                case DETAIL_LOADER:
                    getDetailDataFromCursor(data);
                    break;
                case TRAILER_LOADER:
                    getTrailerDataFromCursor(data);
                    break;
                default:
                    Timber.d("Invalid loader finished with id: " + loader.getId());
                    break;
            }

        }
    }

    private void getDetailDataFromCursor(Cursor data) {
        // Hide help and show movie data
        if (mMovieDetailView.getVisibility() == View.INVISIBLE) {
            mHelpView.setVisibility(View.GONE);
            mMovieDetailView.setVisibility(View.VISIBLE);
        }

        // Read weather condition ID from cursor
        mTitleView.setText(data.getString(COL_MOVIE_ORIGINAL_TITLE));
        mReleaseDateView.setText(String.format("(%s)", data.getString(COL_MOVIE_RELEASE_DATE)));
        mUserRatingView.setText(String.format("%.1f/10", data.getFloat(COL_MOVIE_USER_RATING)));
        mOverviewView.setText(data.getString(COL_MOVIE_OVERVIEW));

        String portraitPath = data.getString(COL_MOVIE_POSTER_PATH);
        if (portraitPath != null && portraitPath.length() > 0) {
            portraitPath = getString(R.string.movie_poster_uri).concat(portraitPath);

            Picasso.with(getActivity()).load(portraitPath).placeholder(R.drawable.ic_poster_details)
                    .error(R.drawable.ic_poster_details_error).noFade().into(mPosterView);
        }
    }

    private void getTrailerDataFromCursor(Cursor data) {
        if (mTrailerButtonContainer.getChildCount() > 0) {
            return;
        }

        do {
            String trailerKey = data.getString(COL_TRAILER_VIDEO_KEY);

            Button button = (Button) LayoutInflater.from(getActivity())
                    .inflate(R.layout.trailer_button, mMovieDetailView, false);
            button.setText("Play trailer " + data.getPosition());
            button.setTag("http://www.youtube.com/watch?v=".concat(trailerKey));
            button.setOnClickListener(mTrailerButtonOnClickListener);

            mTrailerButtonContainer.addView(button);
        } while (data.moveToNext());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
