package com.fcasado.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fcasado.popularmovies.data.FavoriteContract.MovieEntry;
import com.fcasado.popularmovies.data.FavoriteContract.ReviewEntry;
import com.fcasado.popularmovies.data.FavoriteContract.TrailerEntry;

/**
 * Simple DB Helper for app's database.
 */
public class FavoriteDbHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "movie.db";
    private static final int DATABASE_VERSION = 1;

    public FavoriteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_FAVORITE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " ("
                + MovieEntry._ID + " INTEGER PRIMARY KEY," + MovieEntry.COLUMN_TITLE
                + " TEXT NOT NULL, " + MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, "
                + MovieEntry.COLUMN_OVERVIEW + " TEXT, " + MovieEntry.COLUMN_POSTER_PATH + " TEXT, "
                + MovieEntry.COLUMN_RELEASE_DATE + " TEXT, " + MovieEntry.COLUMN_USER_RATING
                + " REAL, " + MovieEntry.COLUMN_POPULARITY + " REAL " + ");";

        final String SQL_CREATE_TRAILER_TABLE = "CREATE TABLE " + TrailerEntry.TABLE_NAME + " ("
                + TrailerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + TrailerEntry.COLUMN_MOVIE_ID
                + " INTEGER NOT NULL, " + TrailerEntry.COLUMN_KEY + " TEXT NOT NULL " + ");";

        final String SQL_CREATE_REVIEW_TABLE = "CREATE TABLE " + ReviewEntry.TABLE_NAME + " ("
                + ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + ReviewEntry.COLUMN_MOVIE_ID
                + " INTEGER NOT NULL, " + ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL, "
                + ReviewEntry.COLUMN_CONTENT + " TEXT NOT NULL " + ");";

        db.execSQL(SQL_CREATE_FAVORITE_TABLE);
        db.execSQL(SQL_CREATE_TRAILER_TABLE);
        db.execSQL(SQL_CREATE_REVIEW_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so simply discard the data and start over
        // on each upgrade.
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TrailerEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ReviewEntry.TABLE_NAME);
        onCreate(db);
    }
}
