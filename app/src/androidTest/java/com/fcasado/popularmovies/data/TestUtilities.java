
package com.fcasado.popularmovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

/*
    Students: These are functions and some test data to make it easier to test your database and
    Content Provider.  Note that you'll want your WeatherContract class to exactly match the one
    in our solution to use these as-given.
 */
public class TestUtilities extends AndroidTestCase {
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

    static ContentValues createMovieValues() {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry._ID, "111111");
        movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, "Back to the Future");
        movieValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, "Back to the Future");
        movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW,
                "A young man is accidentally sent 30 years into the past in a time-traveling DeLorean invented by his friend, Dr. Emmett Brown, and must make sure his high-school-age parents unite in order to save his own existence.");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, "1985-07-03");
        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, "PosterPathDummyData");
        movieValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, "8.5");

        return movieValues;
    }
}
