
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
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.fcasado.popularmovies.data.MovieContract;
import com.fcasado.popularmovies.sync.FavoriteQueryHandler;
import com.fcasado.popularmovies.views.EllipzisingTextView;
import com.squareup.picasso.Picasso;

import timber.log.Timber;

/**
 * Shows movie details UI. Received movie URI in arguments.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        FavoriteQueryHandler.OnInsertCompleteListener {

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

    private static final int COL_REVIEW_AUTHOR = 0;
    private static final int COL_REVIEW_CONTENT = 1;
    private static final String[] REVIEW_COLUMNS = {
            MovieContract.ReviewEntry.COLUMN_AUTHOR, MovieContract.ReviewEntry.COLUMN_CONTENT
    };

    private static final int DETAIL_LOADER = 0;
    private static final int TRAILER_LOADER = 1;
    private static final int REVIEW_LOADER = 2;

    @Bind(R.id.help_textview)
    TextView mHelpView;
    @Bind(R.id.detail_card_view)
    CardView mMovieCardView;
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
    @Bind(R.id.trailer_header)
    LinearLayout mTrailerHeader;
    @Bind(R.id.trailer_container)
    LinearLayout mTrailerContainer;
    @Bind(R.id.review_header)
    LinearLayout mReviewHeader;
    @Bind(R.id.reviews_container)
    LinearLayout mReviewContainer;

    private MenuItem mFavItem;

    private Uri mUri;
    private View.OnClickListener mTrailerOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent("android.intent.action.VIEW",
                    Uri.parse((String) v.getTag()));
            startActivity(intent);
        }
    };
    private View.OnClickListener mReviewViewOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), ReviewsActivity.class);
            intent.putExtra(DetailFragment.DETAIL_URI, mUri);
            startActivity(intent);
        }
    };

    private boolean isFav;
    private FavoriteQueryHandler mFavoriteQueryHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

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
            mMovieCardView.setVisibility(View.INVISIBLE);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        mFavoriteQueryHandler = new FavoriteQueryHandler(getActivity().getContentResolver(), this);

        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        getLoaderManager().initLoader(TRAILER_LOADER, null, this);
        getLoaderManager().initLoader(REVIEW_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detail_fragment, menu);

        mFavItem = menu.findItem(R.id.action_favorite);
        mFavItem.setVisible(mUri != null);

        // For simplicity, and since its a short call, we are calling this here, but it should
        // me
        // moved to FavoriteQueryHandler or similar
        if (mFavItem.isVisible()) {
            Cursor favoriteCursor = getActivity().getContentResolver()
                    .query(MovieContract.FavoriteEntry.CONTENT_URI, new String[] {
                            MovieContract.FavoriteEntry._ID
            }, MovieContract.FavoriteEntry._ID + " = ?", new String[] {
                    String.valueOf(ContentUris.parseId(mUri))
            }, null);
            if (favoriteCursor.getCount() > 0) {
                mFavItem.setIcon(R.drawable.ic_fav_on);
                isFav = true;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_favorite) {
            Timber.d("Fav clicked");
            if (isFav) {
                mFavoriteQueryHandler.removeMovieFromFavorites(ContentUris.parseId(mUri));
                mFavItem.setIcon(R.drawable.ic_fav_off);
            } else {
                mFavoriteQueryHandler.addMovieToFavorites(ContentUris.parseId(mUri));
                mFavItem.setIcon(R.drawable.ic_fav_on);
            }

            isFav = !isFav;

            return true;
        }

        return false;
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
                            TRAILER_COLUMNS, MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " =?",
                            new String[] {
                                    String.valueOf(movieId)
                    }, null);
                }
                break;
            case REVIEW_LOADER:
                if (null != mUri) {
                    long movieId = ContentUris.parseId(mUri);
                    return new CursorLoader(getActivity(), MovieContract.ReviewEntry.CONTENT_URI,
                            REVIEW_COLUMNS, MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " =?",
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
                case REVIEW_LOADER:
                    getReviewDataFromCursor(data);
                    break;
                default:
                    Timber.d("Invalid loader finished with id: " + loader.getId());
                    break;
            }

        }
    }

    private void getDetailDataFromCursor(Cursor data) {
        // Hide help and show movie data
        if (mMovieCardView.getVisibility() == View.INVISIBLE) {
            mHelpView.setVisibility(View.GONE);
            mMovieCardView.setVisibility(View.VISIBLE);
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
        // If we already added the views for trailers avoid re-adding.
        if (mTrailerContainer.getChildCount() >= data.getCount()) {
            return;
        }

        mTrailerHeader.setVisibility(View.VISIBLE);
        mTrailerContainer.removeAllViews();

        do {
            String trailerKey = data.getString(COL_TRAILER_VIDEO_KEY);

            View trailerView = LayoutInflater.from(getActivity()).inflate(R.layout.trailer,
                    mTrailerContainer, false);
            TextView titleView = (TextView) trailerView.findViewById(R.id.trailer_textview);
            titleView.setText("Trailer " + (data.getPosition() + 1));

            if (data.isLast()) {
                View dividerView = trailerView.findViewById(R.id.trailer_divider);
                dividerView.setVisibility(View.GONE);
            }

            trailerView.setTag("http://www.youtube.com/watch?v=".concat(trailerKey));
            trailerView.setOnClickListener(mTrailerOnClickListener);
            mTrailerContainer.addView(trailerView);
        } while (data.moveToNext());
    }

    private void getReviewDataFromCursor(Cursor data) {
        // If we already added the views for reviews avoid re-adding.
        if (mReviewContainer.getChildCount() >= data.getCount()) {
            return;
        }

        mReviewHeader.setVisibility(View.VISIBLE);
        mReviewContainer.removeAllViews();

        do {
            View reviewView = LayoutInflater.from(getActivity()).inflate(R.layout.review,
                    mReviewContainer, false);
            TextView authorView = (TextView) reviewView.findViewById(R.id.review_author_textview);
            EllipzisingTextView contentView = (EllipzisingTextView) reviewView
                    .findViewById(R.id.review_content_textview);
            contentView.setEllipsize(TextUtils.TruncateAt.END);
            contentView.setMaxLines(4);

            authorView.setText(data.getString(COL_REVIEW_AUTHOR));

            // We remove html tags from content just in case
            String content = data.getString(COL_REVIEW_CONTENT);
            content = content.replaceAll("<[^>]*>", "");
            contentView.setText(content);

            if (data.isLast()) {
                View dividerView = reviewView.findViewById(R.id.review_divider);
                dividerView.setVisibility(View.GONE);
            }

            reviewView.setOnClickListener(mReviewViewOnClickListener);
            mReviewContainer.addView(reviewView);
        } while (data.moveToNext());

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onInsertComplete(Uri uri) {
        Timber.d("Added movie as favorite: " + uri);
    }
}
