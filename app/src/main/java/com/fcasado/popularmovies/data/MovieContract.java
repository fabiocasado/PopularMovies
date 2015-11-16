
package com.fcasado.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the movie database.
 */
public class MovieContract {
    // Content authority for movie provider
    public static final String CONTENT_AUTHORITY = "com.fcasado.popularmovies";

    // Base URI's for use to contact the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Path (appended to base content URI) for querying movie data
    public static final String PATH_MOVIE = "movie";

    // Path (appended to base content URI) for querying trailer data
    public static final String PATH_TRAILER = "trailer";

    /* Inner class that defines the table contents of the movie table */
    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE)
                .build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        // Table name
        public static final String TABLE_NAME = "movie";

        // Movie title string
        public static final String COLUMN_TITLE = "title";

        // Movie original title
        public static final String COLUMN_ORIGINAL_TITLE = "originalTitle";

        // Movie overview
        public static final String COLUMN_OVERVIEW = "overview";

        // Movie poster path
        public static final String COLUMN_POSTER_PATH = "posterPath";

        // Movie user rating
        public static final String COLUMN_USER_RATING = "voteAverage";

        // Movie release date
        public static final String COLUMN_RELEASE_DATE = "releaseDate";

        // Movie popularity
        public static final String COLUMN_POPULARITY = "popularity";

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the table contents of the trailer table */
    public static final class TrailerEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILER)
                .build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_TRAILER;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_TRAILER;

        // Table name
        public static final String TABLE_NAME = "trailer";

        // Movie id to related trailer with movie
        public static final String COLUMND_MOVIE_ID = "movieId";

        // Trailer movie key for youtube URL
        public static final String COLUMN_VIDEO_KEY = "videKey";

        public static Uri buildTrailerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
