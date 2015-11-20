package com.fcasado.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Movie {@link ContentProvider}. Allows quering for whole table and also single records when
 * appending id.
 */
public class FavoriteProvider extends ContentProvider {
    private static final int MOVIE = 100;
    private static final int TRAILER = 200;
    private static final int REVIEW = 300;

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private FavoriteDbHelper mOpenHelper;

    // We only have one URI type at the moment, but this may change in the future
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FavoriteContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, FavoriteContract.PATH_MOVIE, MOVIE);
        matcher.addURI(authority, FavoriteContract.PATH_TRAILER, TRAILER);
        matcher.addURI(authority, FavoriteContract.PATH_REVIEW, REVIEW);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new FavoriteDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE:
                return FavoriteContract.MovieEntry.CONTENT_TYPE;
            case TRAILER:
                return FavoriteContract.TrailerEntry.CONTENT_TYPE;
            case REVIEW:
                return FavoriteContract.ReviewEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        String tableName;
        switch (sUriMatcher.match(uri)) {
            case MOVIE: {
                tableName = FavoriteContract.MovieEntry.TABLE_NAME;
                break;
            }
            case TRAILER: {
                tableName = FavoriteContract.TrailerEntry.TABLE_NAME;
                break;
            }
            case REVIEW: {
                tableName = FavoriteContract.ReviewEntry.TABLE_NAME;
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor = mOpenHelper.getReadableDatabase().query(tableName, projection, selection, selectionArgs,
                null, null, sortOrder);
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
                long _id = db.insertWithOnConflict(FavoriteContract.MovieEntry.TABLE_NAME, null,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                if (_id > 0)
                    returnUri = FavoriteContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRAILER: {
                long _id = db.insertWithOnConflict(FavoriteContract.TrailerEntry.TABLE_NAME, null,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                if (_id > 0)
                    returnUri = FavoriteContract.TrailerEntry.buildTrailerUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case REVIEW: {
                long _id = db.insertWithOnConflict(FavoriteContract.ReviewEntry.TABLE_NAME, null,
                        values, SQLiteDatabase.CONFLICT_REPLACE);
                if (_id > 0)
                    returnUri = FavoriteContract.ReviewEntry.buildReviewUri(_id);
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
        String tableName;
        switch (match) {
            case MOVIE: {
                tableName = FavoriteContract.MovieEntry.TABLE_NAME;
                break;
            }
            case TRAILER: {
                tableName = FavoriteContract.TrailerEntry.TABLE_NAME;
                break;
            }
            case REVIEW: {
                tableName = FavoriteContract.ReviewEntry.TABLE_NAME;
                break;
            }

            default:
                return super.bulkInsert(uri, values);
        }

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
        String tableName;

        switch (match) {
            case MOVIE:
                tableName = FavoriteContract.MovieEntry.TABLE_NAME;
                break;
            case TRAILER:
                tableName = FavoriteContract.TrailerEntry.TABLE_NAME;
                break;
            case REVIEW:
                tableName = FavoriteContract.ReviewEntry.TABLE_NAME;
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }


        rowsDeleted = db.delete(tableName, selection,
                selectionArgs);

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        String tableName;
        int rowsUpdated;

        switch (match) {
            case MOVIE:
                tableName = FavoriteContract.MovieEntry.TABLE_NAME;
                break;
            case TRAILER:
                tableName = FavoriteContract.TrailerEntry.TABLE_NAME;
                break;
            case REVIEW:
                tableName = FavoriteContract.ReviewEntry.TABLE_NAME;
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        rowsUpdated = db.update(tableName, values, selection,
                selectionArgs);

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
