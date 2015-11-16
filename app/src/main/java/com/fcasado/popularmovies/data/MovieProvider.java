
package com.fcasado.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Movie {@link ContentProvider}. Allows quering for whole table and also single records when
 * appending id.
 */
public class MovieProvider extends ContentProvider {
    private static final int MOVIE = 100;
    private static final int MOVIE_WITH_ID = 101;
    private static final int TRAILER = 200;
    private static final int TRAILER_WITH_ID = 201;

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    // We only have one URI type at the moment, but this may change in the future
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", MOVIE_WITH_ID);
        matcher.addURI(authority, MovieContract.PATH_TRAILER, TRAILER);
        matcher.addURI(authority, MovieContract.PATH_TRAILER + "/#", TRAILER);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE_WITH_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case TRAILER_WITH_ID:
                return MovieContract.TrailerEntry.CONTENT_ITEM_TYPE;
            case TRAILER:
                return MovieContract.TrailerEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "movie"
            case MOVIE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            }
                // "movie/#"
            case MOVIE_WITH_ID: {
                long id = ContentUris.parseId(uri);

                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME, projection,
                        MovieContract.MovieEntry._ID + " = ?", new String[] {
                                String.valueOf(id)
                }, null, null, null);
                break;
            }
                // "trailer"
            case TRAILER: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.TrailerEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            }
                // "trailer/#"
            case TRAILER_WITH_ID: {
                long id = ContentUris.parseId(uri);

                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.TrailerEntry.TABLE_NAME, projection,
                        MovieContract.TrailerEntry._ID + " = ?", new String[] {
                                String.valueOf(id)
                }, null, null, null);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {
                long _id = db.insertWithOnConflict(MovieContract.MovieEntry.TABLE_NAME, null,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                if (_id > 0)
                    returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRAILER: {
                long _id = db.insertWithOnConflict(MovieContract.TrailerEntry.TABLE_NAME, null,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                if (_id > 0)
                    returnUri = MovieContract.TrailerEntry.buildTrailerUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        String tableName = null;
        switch (match) {
            case MOVIE:
                tableName = MovieContract.MovieEntry.TABLE_NAME;
                break;
            case TRAILER:
                tableName = MovieContract.TrailerEntry.TABLE_NAME;
                break;

            default:
                return super.bulkInsert(uri, values);
        }

        // Just double check, though it should not happen
        if (tableName == null)
            return 0;

        // Insert values
        db.beginTransaction();
        int returnCount = 0;
        try {
            for (ContentValues value : values) {
                long _id = db.insert(tableName, null, value);
                if (_id != -1) {
                    returnCount++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int rowsDeleted;
        String tableName = null;

        switch (match) {
            case MOVIE:
                tableName = MovieContract.MovieEntry.TABLE_NAME;
                break;
            case TRAILER:
                tableName = MovieContract.TrailerEntry.TABLE_NAME;
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Just double check, though it should not happen
        if (tableName == null)
            return 0;

        rowsDeleted = db.delete(tableName, selection, selectionArgs);
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int rowsUpdated;
        String tableName = null;

        switch (match) {
            case MOVIE:
                tableName = MovieContract.MovieEntry.TABLE_NAME;
                break;
            case TRAILER:
                tableName = MovieContract.TrailerEntry.TABLE_NAME;
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Just double check, though it should not happen
        if (tableName == null)
            return 0;

        rowsUpdated = db.update(tableName, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    // This method is implemented specifically for testing purposes
    // @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
