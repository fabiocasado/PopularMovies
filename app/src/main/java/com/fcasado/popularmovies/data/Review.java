
package com.fcasado.popularmovies.data;

/**
 * Created by fcasado on 19/11/2015.
 */
public class Review {
    private String mAuthor;
    private String mContent;

    public Review(String author, String content) {
        mAuthor = author;
        mContent = content;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getContent() {
        return mContent;
    }
}