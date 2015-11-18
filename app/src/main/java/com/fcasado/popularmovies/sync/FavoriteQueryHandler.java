
package com.fcasado.popularmovies.sync;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.fcasado.popularmovies.data.MovieContract;

import timber.log.Timber;

import java.lang.ref.WeakReference;

/**
 * Created by fcasado on 18/11/2015.
 */
public class FavoriteQueryHandler extends AsyncQueryHandler {
    private static final int QUERY_TOKEN = 1;
    private static final int INSERT_TOKEN = 2;
    private static final int DELETE_TOKEN = 3;

    private WeakReference<OnInsertCompleteListener> mListener;

    public FavoriteQueryHandler(ContentResolver cr, OnInsertCompleteListener listener) {
        super(cr);
        this.mListener = new WeakReference<OnInsertCompleteListener>(listener);
    }

    public void addMovieToFavorites(long movieId) {
        Timber.d("addMovieToFavorites: " + movieId);
        startQuery(QUERY_TOKEN, movieId, MovieContract.MovieEntry.CONTENT_URI, null,
                MovieContract.MovieEntry._ID + " =?", new String[] {
                        String.valueOf(movieId)
        }, null);
    }

    public void removeMovieFromFavorites(long movieId) {
        Timber.d("removeMovieFromFavorites: " + movieId);
        startDelete(DELETE_TOKEN, movieId, MovieContract.FavoriteEntry.CONTENT_URI,
                MovieContract.FavoriteEntry._ID + " =?", new String[] {
                        String.valueOf(movieId)
        });
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        super.onQueryComplete(token, cookie, cursor);
        if (token == QUERY_TOKEN) {
            if (cursor != null && cursor.moveToFirst()) {
                ContentValues values = new ContentValues();
                values.put(MovieContract.FavoriteEntry._ID,
                        cursor.getLong(cursor.getColumnIndex(MovieContract.MovieEntry._ID)));
                values.put(MovieContract.FavoriteEntry.COLUMN_TITLE, cursor
                        .getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE)));
                values.put(MovieContract.FavoriteEntry.COLUMN_ORIGINAL_TITLE, cursor.getString(
                        cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE)));
                values.put(MovieContract.FavoriteEntry.COLUMN_OVERVIEW, cursor.getString(
                        cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_OVERVIEW)));
                values.put(MovieContract.FavoriteEntry.COLUMN_POPULARITY, cursor.getFloat(
                        cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POPULARITY)));
                values.put(MovieContract.FavoriteEntry.COLUMN_POSTER_PATH, cursor.getString(
                        cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH)));
                values.put(MovieContract.FavoriteEntry.COLUMN_RELEASE_DATE, cursor.getString(
                        cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE)));
                values.put(MovieContract.FavoriteEntry.COLUMN_USER_RATING, cursor.getFloat(
                        cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_USER_RATING)));
                startInsert(INSERT_TOKEN, cookie, MovieContract.FavoriteEntry.CONTENT_URI, values);
            }
        }

    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
        super.onInsertComplete(token, cookie, uri);

        if (token == INSERT_TOKEN) {
            Timber.d("Finished insert");
            if (mListener != null && mListener.get() != null) {
                mListener.get().onInsertComplete(uri);
            }
        }
    }

    public interface OnInsertCompleteListener {
        void onInsertComplete(Uri uri);
    }

}
