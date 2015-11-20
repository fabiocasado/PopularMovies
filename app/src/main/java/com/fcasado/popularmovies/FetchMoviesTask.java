package com.fcasado.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.fcasado.popularmovies.data.FavoriteContract;
import com.fcasado.popularmovies.data.MovieAPI;
import com.fcasado.popularmovies.datatypes.Movie;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Queries theMovieDB API and saves received data to database. Takes into account the "sort by" app
 * setting.
 */
public class FetchMoviesTask extends AsyncTask<Void, Void, List<Movie>> {
    private OnMovieDataFetchFinished mCallback;
    private Context mContext;

    public FetchMoviesTask(Context context, OnMovieDataFetchFinished callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    protected List<Movie> doInBackground(Void... params) {
        System.out.println("doInBackground");
        // Will contain the raw JSON response as a string.
        String movieJsonStr;
        List<Movie> movies = null;

        movies = getMoviesFromServer();
        if (movies != null) {
            updateMoviesFavoriteStatus(movies);
        }

        return movies;
    }

    private List<Movie> getMoviesFromServer() {
        String movieJsonStr;
        try {
            // Initialize uri builder
            Uri.Builder uriBuilder = Uri.parse(MovieAPI.buildDiscoverMovieEndpointUri())
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
            URL url = new URL(builtUri.toString());

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();

            Response response = client.newCall(request).execute();
            movieJsonStr = response.body().string();

            return getMovieDataFromJson(movieJsonStr);
        } catch (IOException e) {
            // If the code didn't successfully get the movie data, there's no point in
            // attempting
            // to parse it.
        }
        return null;
    }

    private void updateMoviesFavoriteStatus(List<Movie> movies) {
        Uri uri = FavoriteContract.MovieEntry.CONTENT_URI;
        String[] projection = {FavoriteContract.MovieEntry._ID};
        Cursor cursor = mContext.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Movie movie = new Movie(cursor.getLong(0));
                int index = movies.indexOf(movie);
                if (index != -1) {
                    movies.get(index).setFavorite(true);
                }
            }
        }
    }

    private List<Movie> getMovieDataFromJson(String movieJsonStr) {
        List<Movie> movies = new ArrayList<Movie>();

        try {
            JSONObject resultsObject = new JSONObject(movieJsonStr);
            JSONArray movieArray = resultsObject.optJSONArray(MovieAPI.RESULTS_DISCOVER_MOVIE);
            if (movieArray == null)
                return null;

            int moviesCount = movieArray.length();
            for (int i = 0; i < moviesCount; i++) {
                JSONObject movieJson = movieArray.getJSONObject(i);
                long id = movieJson.getLong(MovieAPI.MOVIE_ID);
                String title = movieJson.getString(MovieAPI.MOVIE_TITLE);
                String originalTitle = movieJson.getString(MovieAPI.MOVIE_ORIGINAL_TITLE);
                String overview = movieJson.optString(MovieAPI.MOVIE_OVERVIEW);
                String posterPath = movieJson.optString(MovieAPI.MOVIE_POSTER_PATH);
                String releaseDate = movieJson.optString(MovieAPI.MOVIE_RELEASE_dATE);
                double popularity = movieJson.optDouble(MovieAPI.MOVIE_POPULARITY, 0);
                double userRating = movieJson.optDouble(MovieAPI.MOVIE_USER_RATING, 0);

                Movie movie = new Movie(id, title, originalTitle, overview, posterPath, releaseDate,
                        popularity, userRating);

                movies.add(movie);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return movies;
    }

    @Override
    protected void onPostExecute(List<Movie> movies) {
        super.onPostExecute(movies);

        if (mCallback != null) {
            mCallback.onMovieDataFetchFinished(movies);
        }
    }

    public interface OnMovieDataFetchFinished {
        void onMovieDataFetchFinished(List<Movie> movies);
    }
}
