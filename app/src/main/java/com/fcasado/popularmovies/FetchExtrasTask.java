
package com.fcasado.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Pair;

import com.fcasado.popularmovies.data.Movie;
import com.fcasado.popularmovies.data.MovieAPI;
import com.fcasado.popularmovies.data.Review;
import com.fcasado.popularmovies.data.Trailer;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Queries theMovieDB API and saves received data to database. Takes into account the "sort by" app
 * setting.
 */
public class FetchExtrasTask extends AsyncTask<Movie, Void, Pair<List<Trailer>, List<Review>>> {
    private OnMovieExtrasFetchFinished mCallback;

    public FetchExtrasTask(OnMovieExtrasFetchFinished callback) {
        mCallback = callback;
    }

    @Override
    protected Pair<List<Trailer>, List<Review>> doInBackground(Movie... params) {
        Timber.d("Starting work");

        if (params.length == 0)
            return null;

        try {
            Movie movie = params[0];

            // First get trailer data json
            Uri.Builder uriBuilder = Uri.parse(MovieAPI.buildMovieTrailerEndpointUri(movie.getId()))
                    .buildUpon();

            Uri builtUri = uriBuilder
                    .appendQueryParameter(MovieAPI.API_KEY_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                    .build();
            URL url = new URL(builtUri.toString());

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();

            Response response = client.newCall(request).execute();
            String trailerDataJson = response.body().string();

            // Now get review data json
            uriBuilder = Uri.parse(MovieAPI.buildMovieReviewEndpointUri(movie.getId())).buildUpon();

            builtUri = uriBuilder
                    .appendQueryParameter(MovieAPI.API_KEY_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                    .build();
            url = new URL(builtUri.toString());

            request = new Request.Builder().url(url).build();
            response = client.newCall(request).execute();
            String reviewDataJson = response.body().string();

            return Pair.create(getTrailerDataFromJson(trailerDataJson),
                    getReviewDataFromJson(reviewDataJson));
        } catch (IOException e) {
            Timber.e(e, "Error");
            // If the code didn't successfully get the movie data, there's no point in
            // attempting
            // to parse it.
        }

        return null;
    }

    private List<Trailer> getTrailerDataFromJson(String trailerJsonStr) {
        List<Trailer> trailers = new ArrayList<Trailer>();
        try {
            JSONObject resultsObject = new JSONObject(trailerJsonStr);
            JSONArray trailerArray = resultsObject.optJSONArray(MovieAPI.RESULTS_MOVIE_TRAILER);
            if (trailerArray == null)
                return null;

            int trailerCount = trailerArray.length();
            for (int i = 0; i < trailerCount; i++) {
                JSONObject trailerJson = trailerArray.getJSONObject(i);
                String site = trailerJson.getString(MovieAPI.TRAILER_SITE);
                String key = trailerJson.getString(MovieAPI.TRAILER_KEY);

                if (site.compareTo(MovieAPI.TRAILER_VALID_SITE) != 0) {
                    continue;
                }

                Trailer trailer = new Trailer(key);
                trailers.add(trailer);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return trailers;
    }

    private List<Review> getReviewDataFromJson(String reviewJsonStr) {
        List<Review> reviews = new ArrayList<Review>();
        try {
            JSONObject resultsObject = new JSONObject(reviewJsonStr);
            JSONArray reviewArray = resultsObject.optJSONArray(MovieAPI.RESULTS_MOVIE_REVIEW);
            if (reviewArray == null)
                return null;

            int reviewCount = reviewArray.length();
            for (int i = 0; i < reviewCount; i++) {
                JSONObject reviewJson = reviewArray.getJSONObject(i);
                String author = reviewJson.getString(MovieAPI.REVIEW_AUTHOR);
                String content = reviewJson.getString(MovieAPI.REVIEW_CONTENT);

                Review review = new Review(author, content);
                reviews.add(review);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return reviews;
    }

    @Override
    protected void onPostExecute(Pair<List<Trailer>, List<Review>> movieExtras) {
        super.onPostExecute(movieExtras);

        if (mCallback != null) {
            mCallback.onMovieExtrasFetchFinished(movieExtras);
        }
    }

    public interface OnMovieExtrasFetchFinished {
        void onMovieExtrasFetchFinished(Pair<List<Trailer>, List<Review>> movieExtras);
    }
}
