
package com.fcasado.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fcasado.popularmovies.data.MovieAPI;
import com.fcasado.popularmovies.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Queries theMovieDB API and saves received data to database. Takes into account the "sort by" app
 * setting.
 */
public class FetchMovieTask extends AsyncTask<Void, Void, Void> {
    private static final String LOG_TAG = FetchMovieTask.class.getSimpleName();
    private OnMovieDataFetchFinished mCallback;
    private Context mContext;

    public FetchMovieTask(Context context, OnMovieDataFetchFinished callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(LOG_TAG, "Starting work");
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr;

        try {
            // Initialize uri builder
            Uri.Builder uriBuilder = Uri.parse(MovieAPI.buildEndpointUri(MovieAPI.DISCOVER_MOVIE))
                    .buildUpon();

            // Get the sorting criteria for the api call. By default we will use "Most Popular"
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            String sortValue = preferences.getString(mContext.getString(R.string.pref_sort_key),
                    mContext.getString(R.string.pref_sort_default));
            if (sortValue.equalsIgnoreCase(mContext.getString(R.string.sort_most_popular))) {
                uriBuilder.appendQueryParameter(MovieAPI.API_SORT_BY_PARAM,
                        MovieAPI.MOVIE_POPULARITY + ".desc");
            } else {
                // In the case of sorting by vote_average, we will add an extra check to remove
                // movies with high scores but very little votes
                uriBuilder.appendQueryParameter(MovieAPI.API_SORT_BY_PARAM,
                        MovieAPI.MOVIE_USER_RATING + ".desc");
                uriBuilder.appendQueryParameter(MovieAPI.API_VOTE_COUNT_GTE_PARAM,
                        MovieAPI.API_VOTE_COUNT_GTE_MINIMUM);
            }

            // Now append key and build URL for the MovieDbApi query
            Uri builtUri = uriBuilder
                    .appendQueryParameter(MovieAPI.API_KEY_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                    .build();
            System.out.println("Uri: " + builtUri);
            URL url = new URL(builtUri.toString());

            // Create the request to MovieDbAPI, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty. No point in parsing.
                return null;
            }
            movieJsonStr = buffer.toString();
            getMovieDataFromJson(movieJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the movie data, there's no point in
            // attempting
            // to parse it.
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return null;
    }

    private void getMovieDataFromJson(String movieJsonStr) {
        try {
            JSONObject resultsObject = new JSONObject(movieJsonStr);
            JSONArray movieArray = resultsObject.optJSONArray(MovieAPI.DISCOVER_MOVIE_RESULTS);
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
            int deleted = mContext.getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                    null, null);
            Log.d(LOG_TAG, "Deleting old data before insert. " + deleted + " deleted");

            int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = mContext.getContentResolver()
                        .bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "FetchMovieTask Complete. " + inserted + " Inserted");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        mCallback.onMovieDataFetchFinished();
    }

    public interface OnMovieDataFetchFinished {
        void onMovieDataFetchFinished();
    }
}
