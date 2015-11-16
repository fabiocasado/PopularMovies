
package com.fcasado.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fcasado.popularmovies.data.MovieContract.MovieEntry;
import com.fcasado.popularmovies.data.MovieContract.TrailerEntry;

/**
 * Simple DB Helper for app's database.
 */
public class MovieDbHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "movie.db";
    // Remember to change version if schema changes
    private static final int DATABASE_VERSION = 2;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_TRAILER_TABLE = "CREATE TABLE " + TrailerEntry.TABLE_NAME + " ("
                + TrailerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TrailerEntry.COLUMND_MOVIE_ID + " INTEGER NOT NULL, "
                + TrailerEntry.COLUMN_VIDEO_KEY + " TEXT NOT NULL, "

        // Set up the movie id column as a foreign key to movie table.
                + " FOREIGN KEY (" + TrailerEntry.COLUMND_MOVIE_ID + ") REFERENCES "
                + MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + ") " + " );";

        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " ("
                + MovieEntry._ID + " INTEGER PRIMARY KEY," + MovieEntry.COLUMN_TITLE
                + " TEXT NOT NULL, " + MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, "
                + MovieEntry.COLUMN_OVERVIEW + " TEXT, " + MovieEntry.COLUMN_POSTER_PATH + " TEXT, "
                + MovieEntry.COLUMN_RELEASE_DATE + " TEXT, " + MovieEntry.COLUMN_USER_RATING
                + " REAL, " + MovieEntry.COLUMN_POPULARITY + " REAL " + ");";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_TRAILER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so simply discard the data and start over
        // on each upgrade.
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TrailerEntry.TABLE_NAME);
        onCreate(db);
    }
}
