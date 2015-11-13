
package com.fcasado.popularmovies.data;

/**
 * Constants related with theMovieDB.org API.
 */
public class MovieAPI {
    public static final String BASE_URI = "http://api.themoviedb.org/3/";
    public static final String DISCOVER_MOVIE = "discover/movie?";
    public static final String API_SORT_BY_PARAM = "sort_by";
    public static final String API_VOTE_COUNT_GTE_PARAM = "vote_count.gte";
    public static final String API_KEY_PARAM = "api_key";

    public static final String API_VOTE_COUNT_GTE_MINIMUM = "100";
    public static final String DISCOVER_MOVIE_RESULTS = "results";

    public static final String MOVIE_ID = "id";
    public static final String MOVIE_TITLE = "title";
    public static final String MOVIE_ORIGINAL_TITLE = "original_title";
    public static final String MOVIE_OVERVIEW = "overview";
    public static final String MOVIE_POSTER_PATH = "poster_path";
    public static final String MOVIE_USER_RATING = "vote_average";
    public static final String MOVIE_RELEASE_dATE = "release_date";
    public static final String MOVIE_POPULARITY = "popularity";

    /**
     * Right now we are only using one endpoint, but if its easy to prepare for future changes then
     * we should.
     * 
     * @param endpoint
     *            Endpoint to append to base Movie API url
     * @return String pointing to the web address of the endpoint
     */
    public static String buildEndpointUri(String endpoint) {
        return BASE_URI.concat(endpoint);
    }
}
