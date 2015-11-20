package com.fcasado.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fcasado.popularmovies.data.FavoriteContract;
import com.fcasado.popularmovies.data.FavoriteQueryHandler;
import com.fcasado.popularmovies.datatypes.Movie;
import com.fcasado.popularmovies.datatypes.Review;
import com.fcasado.popularmovies.datatypes.Trailer;
import com.fcasado.popularmovies.views.EllipzisingTextView;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Shows movie details UI. Received movie URI in arguments.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        FetchExtrasTask.OnMovieExtrasFetchFinished, FavoriteQueryHandler.OnDeleteCompleteListener, FavoriteQueryHandler.OnInsertCompleteListener {

    static final String DETAIL_MOVIE = "DETAIL_MOVIE";

    // Movie columns indices. Must be updated if MOVIE_COLUMNS change.
    private static final int COL_POSTER_PATH = 1;
    private static final int COL_ORIGINAL_TITLE = 2;
    private static final int COL_OVERVIEW = 3;
    private static final int COL_USER_RATING = 4;
    private static final int COL_RELEASE_DATE = 5;

    // On details we only need movie poster.
    private static final String[] MOVIE_COLUMNS = {
            FavoriteContract.MovieEntry._ID, FavoriteContract.MovieEntry.COLUMN_POSTER_PATH,
            FavoriteContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
            FavoriteContract.MovieEntry.COLUMN_OVERVIEW, FavoriteContract.MovieEntry.COLUMN_USER_RATING,
            FavoriteContract.MovieEntry.COLUMN_RELEASE_DATE
    };
    private static final String TAG_FETCH_EXTRAS = "tagFetchExtras";
    private static final int DETAIL_LOADER = 0;
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
    private FavoriteQueryHandler mFavoriteQueryHandler;

    private Movie mMovie;
    private FetchExtrasFragment mFetchExtrasFragment;
    private Pair<List<Trailer>, List<Review>> mExtras;

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
            // Intent intent = new Intent(getActivity(), ReviewsActivity.class);
            // intent.putExtra(ReviewsFragment.MOVIE_ID, ContentUris.parseId(mUri));
            // startActivity(intent);
        }
    };

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
            mMovie = arguments.getParcelable(DetailFragment.DETAIL_MOVIE);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);

        boolean shouldShowHelpView = getResources().getBoolean(R.bool.show_help_view);
        if (shouldShowHelpView) {
            mHelpView.setVisibility(View.VISIBLE);
            mMovieCardView.setVisibility(View.INVISIBLE);
        }

        updateDetailUi();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detail_fragment, menu);

        mFavItem = menu.findItem(R.id.action_favorite);
        mFavItem.setVisible(mMovie != null);

        // For simplicity, and since its a short call, we are calling this here, but it should
        // me moved to FavoriteQueryHandler or similar
        if (mFavItem.isVisible()) {
            Uri uri = FavoriteContract.MovieEntry.CONTENT_URI;
            String[] projection = {FavoriteContract.MovieEntry._ID};
            String selection = FavoriteContract.MovieEntry._ID + " =?";
            String[] selectionArgs = {String.valueOf(mMovie.getId())};
            Cursor favoriteCursor = getActivity().getContentResolver()
                    .query(uri, projection, selection, selectionArgs, null);
            if (favoriteCursor.getCount() > 0) {
                mMovie.setFavorite(true);
            }

            mFavItem.setIcon(mMovie.isFavorite() ? R.drawable.ic_fav_on : R.drawable.ic_fav_off);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_favorite) {
            if (mMovie.isFavorite()) {
                mFavoriteQueryHandler.removeMovieFromFavorites(mMovie.getId());
            } else {
                mFavoriteQueryHandler.addMovieToFavorites(mMovie, mExtras);
            }

            return true;
        }

        return false;
    }

    private void updateDetailUi() {
        if (mMovie != null) {
            // Hide help and show movie data
            if (mMovieCardView.getVisibility() == View.INVISIBLE) {
                mHelpView.setVisibility(View.GONE);
                mMovieCardView.setVisibility(View.VISIBLE);
            }

            mTitleView.setText(mMovie.getTitle());
            mReleaseDateView.setText(mMovie.getReleaseDate());
            mUserRatingView.setText(String.format("%.1f/10", mMovie.getUserRating()));
            mOverviewView.setText(mMovie.getOverview());

            String posterPath = mMovie.getPosterPath();
            if (posterPath != null && posterPath.length() > 0) {
                posterPath = getString(R.string.movie_poster_uri).concat(posterPath);
                Picasso.with(getActivity()).load(posterPath)
                        .placeholder(R.drawable.ic_poster_details).noFade().into(mPosterView);
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mFavoriteQueryHandler = new FavoriteQueryHandler(getActivity().getContentResolver(), this, this);

        FragmentManager fm = getFragmentManager();

        // Check to see if we have retained the movie data fetching fragment.
        mFetchExtrasFragment = (FetchExtrasFragment) fm.findFragmentByTag(TAG_FETCH_EXTRAS);

        // If not retained (or first time running), we need to create it.
        if (mFetchExtrasFragment == null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(DETAIL_MOVIE, mMovie);

            mFetchExtrasFragment = new FetchExtrasFragment();
            mFetchExtrasFragment.setArguments(arguments);
            fm.beginTransaction().add(mFetchExtrasFragment, TAG_FETCH_EXTRAS).commit();
        } else {
            mFetchExtrasFragment.refreshContent(mMovie);
        }

        mFetchExtrasFragment.setTargetFragment(this, 0);
        mFetchExtrasFragment.setOnMovieExtasFetchListener(this);

        mExtras = mFetchExtrasFragment.getMovieExtras();
        updateTrailerlUi();
        updateReviewUi();

        // getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // if (null != mUri) {
        // // Now create and return a CursorLoader that will take care of
        // // creating a Cursor for the data being displayed.
        // return new CursorLoader(getActivity(), mUri, MOVIE_COLUMNS, null, null, null);
        // }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // if (data != null && data.moveToFirst()) {
        // // Hide help and show movie data
        // if (mMovieDetailView.getVisibility() == View.INVISIBLE) {
        // mHelpView.setVisibility(View.GONE);
        // mMovieDetailView.setVisibility(View.VISIBLE);
        // }
        //
        // // Read weather condition ID from cursor
        // mTitleView.setText(data.getString(COL_ORIGINAL_TITLE));
        // mReleaseDateView.setText(String.format("(%s)", data.getString(COL_RELEASE_DATE)));
        // mUserRatingView.setText(String.format("%.1f/10", data.getFloat(COL_USER_RATING)));
        // mOverviewView.setText(data.getString(COL_OVERVIEW));
        //
        // String portraitPath = data.getString(COL_POSTER_PATH);
        // if (portraitPath != null && portraitPath.length() > 0) {
        // portraitPath = getString(R.string.movie_poster_uri).concat(portraitPath);
        // Picasso.with(getActivity()).load(portraitPath)
        // .placeholder(R.drawable.ic_poster_details).noFade().into(mPosterView);
        // }
        // }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onMovieExtrasFetchFinished(Pair<List<Trailer>, List<Review>> movieExtras) {
        mExtras = movieExtras;
        updateTrailerlUi();
        updateReviewUi();
    }

    private void updateTrailerlUi() {
        if (mExtras == null || mExtras.first == null) {
            return;
        }

        List<Trailer> trailers = mExtras.first;

        // If we already added the views for trailers avoid re-adding.
        if (mTrailerContainer.getChildCount() >= trailers.size()) {
            return;
        }

        mTrailerHeader.setVisibility(View.VISIBLE);
        mTrailerContainer.removeAllViews();

        for (int i = 0; i < trailers.size(); i++) {
            Trailer trailer = trailers.get(i);
            View trailerView = LayoutInflater.from(getActivity()).inflate(R.layout.trailer,
                    mTrailerContainer, false);
            TextView titleView = (TextView) trailerView.findViewById(R.id.trailer_textview);
            titleView.setText("Trailer " + i);

            // Remove divider if last
            if (i == trailers.size() - 1) {
                View dividerView = trailerView.findViewById(R.id.trailer_divider);
                dividerView.setVisibility(View.GONE);
            }

            trailerView.setTag("http://www.youtube.com/watch?v=".concat(trailer.getKey()));
            trailerView.setOnClickListener(mTrailerOnClickListener);
            mTrailerContainer.addView(trailerView);
        }
    }

    private void updateReviewUi() {
        if (mExtras == null || mExtras.second == null) {
            return;
        }

        List<Review> reviews = mExtras.second;

        // If we already added the views for reviews avoid re-adding.
        if (mReviewContainer.getChildCount() >= reviews.size()) {
            return;
        }

        mReviewHeader.setVisibility(View.VISIBLE);
        mReviewContainer.removeAllViews();

        for (int i = 0; i < reviews.size(); i++) {
            Review review = reviews.get(i);
            View reviewView = LayoutInflater.from(getActivity()).inflate(R.layout.review,
                    mReviewContainer, false);
            TextView authorView = (TextView) reviewView.findViewById(R.id.review_author_textview);
            EllipzisingTextView contentView = (EllipzisingTextView) reviewView
                    .findViewById(R.id.review_content_textview);
            contentView.setEllipsize(TextUtils.TruncateAt.END);
            contentView.setMaxLines(4);

            authorView.setText(review.getAuthor());

            // We remove inital weird html tags from content just in case
            String content = review.getContent();
            content = content.replaceAll("<[^>]*>", "");
            contentView.setText(content);

            if (i == reviews.size() - 1) {
                View dividerView = reviewView.findViewById(R.id.review_divider);
                dividerView.setVisibility(View.GONE);
            }

            reviewView.setOnClickListener(mReviewViewOnClickListener);
            mReviewContainer.addView(reviewView);
        }

    }

    @Override
    public void onDeleteComplete(long movieId) {
        if (mMovie.getId() == movieId) {
            mMovie.setFavorite(false);
            mFavItem.setIcon(R.drawable.ic_fav_off);
        }
    }

    @Override
    public void onInsertComplete(long movieId) {
        if (mMovie.getId() == movieId) {
            mMovie.setFavorite(true);
            mFavItem.setIcon(R.drawable.ic_fav_on);
        }
    }
}
