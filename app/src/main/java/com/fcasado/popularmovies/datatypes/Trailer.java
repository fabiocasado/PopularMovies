
package com.fcasado.popularmovies.datatypes;

/**
 * Datatype to easily represent movie Trailer
 */
public class Trailer {
    private String mKey;

    public Trailer(String key) {
        mKey = key;
    }

    public String getKey() {
        return mKey;
    }
}
