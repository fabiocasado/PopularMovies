
package com.fcasado.popularmovies;

import android.os.Bundle;

import com.fcasado.popularmovies.utils.UpBugFixAppCompatActivity;

/**
 * Used to show all reviews for specific movie, since the reviews can be quite extensive
 */
public class ReviewsActivity extends UpBugFixAppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);
    }
}
