
package com.fcasado.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

/*
    Students: These are functions and some test data to make it easier to test your database and
    Content Provider.  Note that you'll want your WeatherContract class to exactly match the one
    in our solution to use these as-given.
 */
public class TestUtilities extends AndroidTestCase {
    static final String TEST_TRAILER_ID = "1";
    static final String TEST_TRAILER_KEY = "BOVriTeIypQ";

    static final String TEST_MOVIE_ID = "206647";
    static final String TEST_MOVIE_TITLE = "Spectre";
    static final String TEST_MOVIE_ORIGINAL_TITLE = "Spectre";
    static final String TEST_MOVIE_RELEASE_DATE = "2015-11-06";
    static final String TEST_MOVIE_POSTER_PATH = "/1n9D32o30XOHMdMWuIT4AaA5ruI.jpg";
    static final String TEST_MOVIE_USER_RATING = "6.5";
    static final String TEST_MOVIE_OVERVIEW = "A cryptic message from Bond\'s past sends him on a trail to uncover a sinister organization. While M battles political forces to keep the secret service alive, Bond peels back the layers of deceit to reveal the terrible truth behind SPECTRE.";

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor,
            ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(
                    "Value '" + entry.getValue().toString() + "' did not match the expected value '"
                            + expectedValue + "'. " + error,
                    expectedValue, valueCursor.getString(idx));
        }
    }

    static ContentValues createSpectreMovieValues() {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry._ID, TEST_MOVIE_ID);
        movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, TEST_MOVIE_TITLE);
        movieValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, TEST_MOVIE_ORIGINAL_TITLE);
        movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, TEST_MOVIE_OVERVIEW);
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, TEST_MOVIE_RELEASE_DATE);
        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, TEST_MOVIE_POSTER_PATH);
        movieValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, TEST_MOVIE_USER_RATING);

        return movieValues;
    }

    static ContentValues createTrailerValues(long movieId) {
        ContentValues trailerValues = new ContentValues();
        trailerValues.put(MovieContract.TrailerEntry._ID, TEST_TRAILER_ID);
        trailerValues.put(MovieContract.TrailerEntry.COLUMN_VIDEO_KEY, TEST_TRAILER_KEY);
        trailerValues.put(MovieContract.TrailerEntry.COLUMN_MOVIE_ID, movieId);

        return trailerValues;
    }

    static long insertSpectreMovieValues(Context context) {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createSpectreMovieValues();

        long locationRowId;
        locationRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert Spectre Movie Values", locationRowId != -1);

        return locationRowId;
    }

    // static ContentValues createSpectreTrailerValues() {
    // // Create a new map of values, where column names are the keys
    // ContentValues testValues = new ContentValues();
    // testValues.put(MovieContract.TrailerEntry.COLUMN_MOVIE_ID, TEST_MOVIE_ID);
    // testValues.put(MovieContract.TrailerEntry.COLUMN_VIDEO_KEY, TEST_TRAILER_KEY);
    //
    // return testValues;
    // }

    // static long insertSpectreTrailerValues(Context context) {
    // // insert our test records into the database
    // MovieDbHelper dbHelper = new MovieDbHelper(context);
    // SQLiteDatabase db = dbHelper.getWritableDatabase();
    // ContentValues testValues = TestUtilities.createSpectreTrailerValues();
    //
    // long locationRowId;
    // locationRowId = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, testValues);
    //
    // // Verify we got a row back.
    // assertTrue("Error: Failure to insert North Pole Location Values", locationRowId != -1);
    //
    // return locationRowId;
    // }

}
