
package com.fcasado.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fcasado.popularmovies.data.MovieContract.MovieEntry;

/**
 * Created by fcasado on 04/11/2015.
 */
public class MovieDbHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "movie.db";
    private static final int DATABASE_VERSION = 1;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " ("
                + MovieEntry._ID + " INTEGER PRIMARY KEY," + MovieEntry.COLUMN_TITLE
                + " TEXT NOT NULL, " + MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, "
                + MovieEntry.COLUMN_OVERVIEW + " TEXT, " + MovieEntry.COLUMN_POSTER_PATH + " TEXT, "
                + MovieEntry.COLUMN_RELEASE_DATE + " TEXT, " + MovieEntry.COLUMN_USER_RATING
                + " REAL, " + MovieEntry.COLUMN_POPULARITY + " REAL " + ");";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so simply discard the data and start over
        // on each upgrade.
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
