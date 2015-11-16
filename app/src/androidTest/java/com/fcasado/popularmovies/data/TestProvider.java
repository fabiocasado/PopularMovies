/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fcasado.popularmovies.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;

import timber.log.Timber;

/*
    Note: This is not a complete set of tests of the Popular Movies ContentProvider, but it does test
    that at least the basic functionality has been implemented correctly.
 */
public class TestProvider extends AndroidTestCase {

    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;

    static ContentValues[] createBulkInsertMovieValues() {
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        long movieId = Long.valueOf(TestUtilities.TEST_MOVIE_ID);
        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, movieId++) {
            ContentValues movieValues = new ContentValues();
            movieValues.put(MovieContract.MovieEntry._ID, movieId);
            movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, TestUtilities.TEST_MOVIE_TITLE);
            movieValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
                    TestUtilities.TEST_MOVIE_ORIGINAL_TITLE);
            movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW,
                    TestUtilities.TEST_MOVIE_OVERVIEW);
            movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
                    TestUtilities.TEST_MOVIE_RELEASE_DATE);
            movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH,
                    TestUtilities.TEST_MOVIE_POSTER_PATH);
            movieValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING,
                    TestUtilities.TEST_MOVIE_USER_RATING);
            returnContentValues[i] = movieValues;
        }
        return returnContentValues;
    }

    /*
     * This helper function deletes all records from database tables using the ContentProvider. It
     * also queries the ContentProvider to make sure that the database has been successfully
     * deleted, so it cannot be used until the Query and Delete functions have been written in the
     * ContentProvider.
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI, null, null);
        mContext.getContentResolver().delete(MovieContract.TrailerEntry.CONTENT_URI, null, null);

        Cursor cursor = mContext.getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                null, null, null, null);
        assertEquals("Error: Records not deleted from Movie table during delete", 0,
                cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(MovieContract.TrailerEntry.CONTENT_URI, null,
                null, null, null);
        assertEquals("Error: Records not deleted from Trailer table during delete", 0,
                cursor.getCount());
        cursor.close();
    }

    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
     * This test checks to make sure that the content provider is registered correctly.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // MovieProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MovieProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals(
                    "Error: MovieProvider registered with authority: " + providerInfo.authority
                            + " instead of authority: " + MovieContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MovieContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: MovieProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
     * This test doesn't touch the database. It verifies that the ContentProvider returns the
     * correct type for each type of URI that it can handle.
     */
    public void testGetType() {
        // content://com.fcasado.popularmovies/Movie/
        String type = mContext.getContentResolver().getType(MovieContract.MovieEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.fcasado.popularmovies/Movie
        assertEquals("Error: the MovieEntry CONTENT_URI should return MovieEntry.CONTENT_TYPE",
                MovieContract.MovieEntry.CONTENT_TYPE, type);

        long movieId = 111111L;
        // content://com.fcasado.popularmovies/Movie/111111
        type = mContext.getContentResolver()
                .getType(MovieContract.MovieEntry.buildMovieUri(movieId));
        // vnd.android.cursor.dir/com.fcasado.popularmovies/Movie
        assertEquals(
                "Error: the MovieEntry CONTENT_URI with Trailer should return MovieEntry.CONTENT_TYPE",
                MovieContract.MovieEntry.CONTENT_ITEM_TYPE, type);

        // content://com.fcasado.popularmovies/Trailer/
        type = mContext.getContentResolver().getType(MovieContract.TrailerEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.fcasado.popularmovies/Trailer
        assertEquals("Error: the TrailerEntry CONTENT_URI should return TrailerEntry.CONTENT_TYPE",
                MovieContract.TrailerEntry.CONTENT_TYPE, type);

        // content://com.fcasado.popularmovies/Trailer/1
        long trailerId = 1L;
        type = mContext.getContentResolver()
                .getType(MovieContract.TrailerEntry.buildTrailerUri(trailerId));
        // vnd.android.cursor.dir/com.fcasado.popularmovies/Trailer
        assertEquals("Error: the TrailerEntry CONTENT_URI should return TrailerEntry.CONTENT_TYPE",
                MovieContract.TrailerEntry.CONTENT_TYPE, type);
    }

    /*
     * This test uses the database directly to insert and then uses the ContentProvider to read out
     * the data.
     */
    public void testBasicMovieQuery() {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues movieValues = TestUtilities.createSpectreMovieValues();

        long MovieRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, movieValues);
        assertTrue("Unable to Insert MovieEntry into the Database", MovieRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor MovieCursor = mContext.getContentResolver()
                .query(MovieContract.MovieEntry.CONTENT_URI, null, null, null, null);

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicMovieQuery", MovieCursor, movieValues);
    }

    /*
     * This test uses the database directly to insert and then uses the ContentProvider to read out
     * the data.
     */
    public void testBasicTrailerQueries() {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues movieValues = TestUtilities.createSpectreMovieValues();
        long movieRowId = TestUtilities.insertSpectreMovieValues(mContext);

        ContentValues trailerValues = TestUtilities.createTrailerValues(movieRowId);
        long trailerRowId = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, trailerValues);
        assertTrue("Unable to Insert TrailerEntry into the Database", trailerRowId != -1);

        // Test the basic content provider query
        Cursor trailerCursor = mContext.getContentResolver()
                .query(MovieContract.TrailerEntry.CONTENT_URI, null, null, null, null);

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicTrailerQueries, Trailer query", trailerCursor,
                trailerValues);

        // Has the NotificationUri been set correctly? --- we can only test this easily against API
        // level 19 or greater because getNotificationUri was added in API level 19.
        if (Build.VERSION.SDK_INT >= 19) {
            assertEquals("Error: Trailer Query did not properly set NotificationUri",
                    trailerCursor.getNotificationUri(), MovieContract.TrailerEntry.CONTENT_URI);
        }
    }

    /*
     * This test uses the provider to insert and then update the data.
     */
    public void testUpdateTrailer() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities
                .createTrailerValues(Long.valueOf(TestUtilities.TEST_MOVIE_ID));

        Uri TrailerUri = mContext.getContentResolver()
                .insert(MovieContract.TrailerEntry.CONTENT_URI, values);
        long trailerRowId = ContentUris.parseId(TrailerUri);

        // Verify we got a row back.
        assertTrue(trailerRowId != -1);
        Timber.d("New row id: " + trailerRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(MovieContract.TrailerEntry._ID, trailerRowId);
        updatedValues.put(MovieContract.TrailerEntry.COLUMN_VIDEO_KEY, "ANY_KEY");

        int count = mContext.getContentResolver().update(MovieContract.TrailerEntry.CONTENT_URI,
                updatedValues, MovieContract.TrailerEntry._ID + "= ?", new String[] {
                        Long.toString(trailerRowId)
        });
        assertEquals(count, 1);

        Cursor cursor = mContext.getContentResolver().query(MovieContract.TrailerEntry.CONTENT_URI,
                null, MovieContract.TrailerEntry._ID + " = " + trailerRowId, null, null);

        TestUtilities.validateCursor("testUpdateTrailer. Error validating Trailer entry update.",
                cursor, updatedValues);

        cursor.close();
    }

    // Make sure we can still delete after adding/updating stuff
    public void testInsertReadProvider() {
        ContentValues testValues = TestUtilities.createSpectreMovieValues();

        Uri movieUri = mContext.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI,
                testValues);

        long movieRowId = ContentUris.parseId(movieUri);

        // Verify we got a row back.
        assertTrue(movieRowId != -1);

        // Data's inserted. IN THEORY. Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                null, null, // cols for "where" clause
                null, // values for "where" clause
                null // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating MovieEntry.", cursor,
                testValues);

        // Fantastic. Now that we have a Movie, add some Trailer!
        ContentValues trailerValues = TestUtilities.createTrailerValues(movieRowId);

        Uri MovieInsertUri = mContext.getContentResolver()
                .insert(MovieContract.TrailerEntry.CONTENT_URI, trailerValues);
        assertTrue(MovieInsertUri != null);

        // A cursor is your primary interface to the query results.
        Cursor trailerCursor = mContext.getContentResolver().query(
                MovieContract.TrailerEntry.CONTENT_URI, null, // leaving "columns" null just returns
                                                              // all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor(
                "testInsertReadProvider. Error validating TrailerEntry insert.", trailerCursor,
                trailerValues);

    }

    // Make sure we can still delete after adding/updating stuff
    public void testDeleteRecords() {
        testInsertReadProvider();

        deleteAllRecordsFromProvider();
    }

    public void testBulkInsert() {
        // Now we can bulkInsert some Movie. In fact, we only implement BulkInsert for Movie
        // entries.
        ContentValues[] bulkInsertContentValues = createBulkInsertMovieValues();

        int insertCount = mContext.getContentResolver()
                .bulkInsert(MovieContract.MovieEntry.CONTENT_URI, bulkInsertContentValues);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                null, // leaving
                null, null, null);

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext()) {
            TestUtilities.validateCurrentRecord("testBulkInsert. Error validating MovieEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}
