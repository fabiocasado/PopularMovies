
package com.fcasado.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

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
 * Created by fcasado on 04/11/2015.
 */
public class FetchMovieTask extends AsyncTask<Void, Void, Void> {
    private static final String LOG_TAG = FetchMovieTask.class.getSimpleName();

    private Context mContext;

    public FetchMovieTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;

        try {
            // Construct the URL for the MovieDbApi query

            Uri builtUri = Uri.parse(MovieAPI.buildEndpointUri(MovieAPI.DISCOVER_MOVIE)).buildUpon()
                    .appendQueryParameter(MovieAPI.SORT_BY_PARAM,
                            MovieAPI.MOVIE_POPULARITY.concat(".desc"))
                    .appendQueryParameter(MovieAPI.API_KEY_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                    .build();

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
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty. No point in parsing.
                return null;
            }
            movieJsonStr = buffer.toString();
            getMovieDataFromJson(movieJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the movie data, there's no point in attempting
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
}
