
package com.fcasado.popularmovies;

import android.net.Uri;
import android.os.Bundle;

import com.fcasado.popularmovies.utils.UpBugFixAppCompatActivity;

import timber.log.Timber;

/**
 * Used to show all reviews for specific movie, since the reviews can be quite extensive
 */
public class ReviewsActivity extends UpBugFixAppCompatActivity {
    private Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        mUri = getIntent().getParcelableExtra(DetailFragment.DETAIL_URI);
        Timber.d("Uri: " + mUri);

    }
}
