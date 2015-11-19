
package com.fcasado.popularmovies.data;

/**
 * Constants related with theMovieDB.org API.
 */
public class MovieAPI {
    public static final String API_SORT_BY_PARAM = "sort_by";
    public static final String API_VOTE_COUNT_GTE_PARAM = "vote_count.gte";
    public static final String API_KEY_PARAM = "api_key";


    public static final String API_VOTE_COUNT_GTE_MINIMUM = "100";
    public static final String RESULTS_DISCOVER_MOVIE = "results";
    public static final String RESULTS_MOVIE_TRAILER = "results";
    public static final String RESULTS_MOVIE_REVIEW = "results";

    public static final String BASE_URI = "http://api.themoviedb.org/3/";
    public static final String MOVIE_ID = "id";
    public static final String MOVIE_TITLE = "title";
    public static final String MOVIE_ORIGINAL_TITLE = "original_title";
    public static final String MOVIE_OVERVIEW = "overview";
    public static final String MOVIE_POSTER_PATH = "poster_path";
    public static final String MOVIE_USER_RATING = "vote_average";
    public static final String MOVIE_RELEASE_dATE = "release_date";
    public static final String MOVIE_POPULARITY = "popularity";
    // Trailer endpoint json fields
    public static final String TRAILER_SITE = "site";
    public static final String TRAILER_KEY = "key";
    public static final String TRAILER_VALID_SITE = "YouTube";
    // Review endpoint json fields
    public static final String REVIEW_AUTHOR = "author";
    public static final String REVIEW_CONTENT = "content";
    private static final String ENDPOINT_DISCOVER_MOVIE = "discover/movie?";
    private static final String ENDPOINT_MOVIE_TRAILER = "movie/%d/videos?";
    private static final String ENDPOINT_MOVIE_REVIEW = "movie/%d/reviews?";

    /**
     * Right now we are only using one endpoint, but if its easy to prepare for future changes then
     * we should.
     * 
     * @return String pointing to the web address of the endpoint
     */
    public static String buildDiscoverMovieEndpointUri() {
        return BASE_URI.concat(ENDPOINT_DISCOVER_MOVIE);
    }

    public static String buildMovieTrailerEndpointUri(long movieId) {
        return BASE_URI.concat(String.format(ENDPOINT_MOVIE_TRAILER, movieId));
    }

    public static String buildMovieReviewEndpointUri(long movieId) {
        return BASE_URI.concat(String.format(ENDPOINT_MOVIE_REVIEW, movieId));
    }
}
