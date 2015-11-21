package com.fcasado.popularmovies.data;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.util.Pair;

import com.fcasado.popularmovies.data.FavoriteContract.MovieEntry;
import com.fcasado.popularmovies.data.FavoriteContract.ReviewEntry;
import com.fcasado.popularmovies.data.FavoriteContract.TrailerEntry;
import com.fcasado.popularmovies.datatypes.Movie;
import com.fcasado.popularmovies.datatypes.Review;
import com.fcasado.popularmovies.datatypes.Trailer;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * AsyncQueryHandler to add/remove favorite movies outside main thread
 */
public class FavoriteQueryHandler extends AsyncQueryHandler {
    private static final int INSERT_MOVIE_TOKEN = 100;
    private static final int INSERT_TRAILER_TOKEN = 101;
    private static final int INSERT_REVIEW_TOKEN = 102;
    private static final int DELETE_MOVIE_TOKEN = 200;
    private static final int DELETE_TRAILER_TOKEN = 201;
    private static final int DELETE_REVIEW_TOKEN = 202;

    private WeakReference<OnInsertCompleteListener> mInsertListener;
    private WeakReference<OnDeleteCompleteListener> mDeleteListener;

    public FavoriteQueryHandler(ContentResolver cr, OnInsertCompleteListener insertListener, OnDeleteCompleteListener deleteListener) {
        super(cr);
        mInsertListener = new WeakReference<OnInsertCompleteListener>(insertListener);
        mDeleteListener = new WeakReference<OnDeleteCompleteListener>(deleteListener);
    }

    public void addMovieToFavorites(Movie movie, Pair<List<Trailer>, List<Review>> extras) {
        // Add movie to db
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieEntry._ID, movie.getId());
        movieValues.put(MovieEntry.COLUMN_TITLE, movie.getTitle());
        movieValues.put(MovieEntry.COLUMN_ORIGINAL_TITLE, movie.getOriginalTitle());
        movieValues.put(MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
        movieValues.put(MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
        movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        movieValues.put(MovieEntry.COLUMN_USER_RATING, movie.getPopularity());

        startInsert(INSERT_MOVIE_TOKEN, movie, MovieEntry.CONTENT_URI, movieValues);

        // Process extras
        if (extras != null) {
            // Add trailers to db
            List<Trailer> trailers = extras.first;
            if (trailers != null) {
                for (Trailer trailer : trailers) {
                    ContentValues trailerValues = new ContentValues();
                    trailerValues.put(TrailerEntry.COLUMN_MOVIE_ID, movie.getId());
                    trailerValues.put(TrailerEntry.COLUMN_KEY, trailer.getKey());
                    startInsert(INSERT_TRAILER_TOKEN, trailer, TrailerEntry.CONTENT_URI, trailerValues);
                }
            }

            // Add reviews to db
            List<Review> reviews = extras.second;
            if (reviews != null) {
                for (Review review : reviews) {
                    ContentValues reviewValues = new ContentValues();
                    reviewValues.put(ReviewEntry.COLUMN_MOVIE_ID, movie.getId());
                    reviewValues.put(ReviewEntry.COLUMN_AUTHOR, review.getAuthor());
                    reviewValues.put(ReviewEntry.COLUMN_CONTENT, review.getContent());
                    startInsert(INSERT_REVIEW_TOKEN, review, ReviewEntry.CONTENT_URI, reviewValues);
                }
            }
        }
    }

    public void removeMovieFromFavorites(long movieId) {
        // Remove trailers from db
        Uri uri = TrailerEntry.CONTENT_URI;
        String selection = TrailerEntry.COLUMN_MOVIE_ID + " =?";
        String[] selectionArgs = {String.valueOf(movieId)};
        startDelete(DELETE_TRAILER_TOKEN, null, uri, selection, selectionArgs);

        // Remove reviews from db
        uri = ReviewEntry.CONTENT_URI;
        selection = ReviewEntry.COLUMN_MOVIE_ID + " =?";
        startDelete(DELETE_REVIEW_TOKEN, null, uri, selection, selectionArgs);

        // Remove movie from db
        uri = MovieEntry.CONTENT_URI;
        selection = MovieEntry._ID + " =?";
        startDelete(DELETE_MOVIE_TOKEN, movieId, uri, selection, selectionArgs);
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
        super.onInsertComplete(token, cookie, uri);

        if (token == INSERT_MOVIE_TOKEN) {
            if (mInsertListener != null && mInsertListener.get() != null) {
                mInsertListener.get().onInsertComplete(((Movie) cookie).getId());
            }
        }
    }

    @Override
    protected void onDeleteComplete(int token, Object cookie, int result) {
        super.onDeleteComplete(token, cookie, result);

        if (token == DELETE_MOVIE_TOKEN) {
            if (mDeleteListener != null && mDeleteListener.get() != null) {
                mDeleteListener.get().onDeleteComplete((long) cookie);
            }
        }
    }

    public interface OnInsertCompleteListener {
        void onInsertComplete(long movieId);
    }

    public interface OnDeleteCompleteListener {
        void onDeleteComplete(long movieId);
    }

}
