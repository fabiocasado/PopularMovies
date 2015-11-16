
package com.fcasado.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.fcasado.popularmovies.BuildConfig;
import com.fcasado.popularmovies.R;
import com.fcasado.popularmovies.data.MovieAPI;
import com.fcasado.popularmovies.data.MovieContract;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

import java.io.IOException;
import java.net.URL;
import java.util.Vector;

public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {
    // Interval at which to sync with the weather, in milliseconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 1000 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    public final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();

    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context
     *            The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one if the
     * fake account doesn't exist yet. If we make a new account, we call the onAccountCreated method
     * so we can initialize things.
     *
     * @param context
     *            The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context
                .getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(context.getString(R.string.app_name),
                context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

            /*
             * Add the account and account type, no password or user data If successful, return the
             * Account object, otherwise report an error.
             */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * Provider already set as android:syncable in manifest, so no need to setIsSyncable
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().syncPeriodic(syncInterval, flexTime)
                    .setSyncAdapter(account, authority).setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        MovieSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount,
                context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
            ContentProviderClient provider, SyncResult syncResult) {
        Timber.d("Sync started.");

        syncDiscoverMoviesData();
        syncDetailMoviesData();

        Timber.d("Sync finished.");
    }

    private void syncDiscoverMoviesData() {
        // Will contain the raw JSON response as a string.
        String movieJsonStr;

        try {
            // Initialize uri builder
            Uri.Builder uriBuilder = Uri.parse(MovieAPI.buildDiscoverMovieEndpointUri())
                    .buildUpon();

            // Get the sorting criteria for the api call. By default we will use "Most Popular"
            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(getContext());
            String sortValue = preferences.getString(getContext().getString(R.string.pref_sort_key),
                    getContext().getString(R.string.pref_sort_default));
            if (sortValue.equalsIgnoreCase(getContext().getString(R.string.sort_most_popular))) {
                uriBuilder.appendQueryParameter(MovieAPI.API_PARAM_SORT_BY,
                        MovieAPI.MOVIE_POPULARITY + ".desc");
            } else {
                // In the case of sorting by vote_average, we will add an extra check to remove
                // movies with high scores but very little votes
                uriBuilder.appendQueryParameter(MovieAPI.API_PARAM_SORT_BY,
                        MovieAPI.MOVIE_USER_RATING + ".desc");
                uriBuilder.appendQueryParameter(MovieAPI.API_PARAM_VOTE_COUNT_GTE,
                        MovieAPI.API_VOTE_COUNT_GTE_MINIMUM);
            }

            // Now append key and build URL for the MovieDbApi query
            Uri builtUri = uriBuilder
                    .appendQueryParameter(MovieAPI.API_PARAM_KEY, BuildConfig.MOVIE_DB_API_KEY)
                    .build();
            URL url = new URL(builtUri.toString());

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();

            Response response = client.newCall(request).execute();
            movieJsonStr = response.body().string();

            getMovieDataFromJson(movieJsonStr);
        } catch (IOException e) {
            Timber.e(e, "Error");
            // If the code didn't successfully get the movie data, there's no point in
            // attempting
            // to parse it.
        }
    }

    private void syncDetailMoviesData() {
        // Will contain the raw JSON response as a string.
        String movieJsonStr;

        // First we check to see if we actually have movies
        Cursor movieCursor = getContext().getContentResolver()
                .query(MovieContract.MovieEntry.CONTENT_URI, new String[] {
                        MovieContract.MovieEntry._ID
        }, null, null, null);
        if (movieCursor.getCount() == 0)
            return;

        // If we have movies, loop through them fetching trailers & reviews
        while (movieCursor.moveToNext()) {
            try {
                long movieID = movieCursor.getLong(0);

                // Initialize uri builder
                Uri.Builder uriBuilder = Uri.parse(MovieAPI.buildMovieTrailerEndpointUri(movieID))
                        .buildUpon();

                // Now append key and build URL for the MovieDbApi query
                Uri builtUri = uriBuilder
                        .appendQueryParameter(MovieAPI.API_PARAM_KEY, BuildConfig.MOVIE_DB_API_KEY)
                        .build();
                URL url = new URL(builtUri.toString());

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();

                Response response = client.newCall(request).execute();
                movieJsonStr = response.body().string();

                getTrailerDataFromJson(movieJsonStr, movieID);
            } catch (IOException e) {
                Timber.e(e, "Error");
                // If the code didn't successfully get the trailer data, there's no point in
                // attempting
                // to parse it.
            }
        }

    }

    private void getMovieDataFromJson(String movieJsonStr) {
        try {
            JSONObject resultsObject = new JSONObject(movieJsonStr);
            JSONArray movieArray = resultsObject.optJSONArray(MovieAPI.RESULTS_DISCOVER_MOVIE);
            if (movieArray == null)
                return;

            // Insert the new movies information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(movieArray.length());

            int moviesCount = movieArray.length();
            for (int i = 0; i < moviesCount; i++) {
                JSONObject movie = movieArray.getJSONObject(i);
                String id = movie.getString(MovieAPI.MOVIE_ID);
                String title = movie.getString(MovieAPI.MOVIE_TITLE);
                String originalTitle = movie.getString(MovieAPI.MOVIE_ORIGINAL_TITLE);
                String overview = movie.optString(MovieAPI.MOVIE_OVERVIEW);
                String posterPath = movie.optString(MovieAPI.MOVIE_POSTER_PATH);
                double userRating = movie.optDouble(MovieAPI.MOVIE_USER_RATING, 0);
                String releaseDate = movie.optString(MovieAPI.MOVIE_RELEASE_dATE);
                double popularity = movie.optDouble(MovieAPI.MOVIE_POPULARITY, 0);

                ContentValues movieValues = new ContentValues();

                movieValues.put(MovieContract.MovieEntry._ID, id);
                movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, title);
                movieValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, originalTitle);
                movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, overview);
                movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, posterPath);
                movieValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, userRating);
                movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
                movieValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, popularity);

                cVVector.add(movieValues);
            }

            // Before insertion, we delete old records just in case, since we may end up with old
            // movies which data is never updated
            int deleted = getContext().getContentResolver()
                    .delete(MovieContract.MovieEntry.CONTENT_URI, null, null);
            Timber.d("Deleting old data before insert. " + deleted + " deleted");

            int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = getContext().getContentResolver()
                        .bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getTrailerDataFromJson(String trailerJsonStr, long movieId) {
        try {
            JSONObject resultsObject = new JSONObject(trailerJsonStr);
            JSONArray trailerArray = resultsObject.optJSONArray(MovieAPI.RESULTS_MOVIE_TRAILER);
            if (trailerArray == null)
                return;

            // Insert the new trailer information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(trailerArray.length());

            int trailerCount = trailerArray.length();
            for (int i = 0; i < trailerCount; i++) {
                JSONObject trailer = trailerArray.getJSONObject(i);
                String site = trailer.getString(MovieAPI.TRAILER_SITE);
                String key = trailer.getString(MovieAPI.TRAILER_KEY);

                if (site.compareTo(MovieAPI.TRAILER_VALID_SITE) != 0) {
                    continue;
                }

                ContentValues trailerValues = new ContentValues();
                trailerValues.put(MovieContract.TrailerEntry.COLUMN_VIDEO_KEY, key);
                trailerValues.put(MovieContract.TrailerEntry.COLUMND_MOVIE_ID, movieId);

                cVVector.add(trailerValues);

                Timber.d("Added trailer for movie: " + movieId + ", with key: " + key);
            }

            // Before insertion, we delete old records just in case, since we may end up with
            // old trailers which are no longer available
            int deleted = getContext().getContentResolver().delete(
                    MovieContract.TrailerEntry.CONTENT_URI,
                    MovieContract.TrailerEntry.COLUMND_MOVIE_ID + " =?", new String[] {
                            String.valueOf(movieId)
            });
            Timber.d("Deleting old trailers before insert. " + deleted + " trailers deleted");

            int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = getContext().getContentResolver()
                        .bulkInsert(MovieContract.TrailerEntry.CONTENT_URI, cvArray);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
