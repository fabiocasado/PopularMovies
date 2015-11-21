package com.fcasado.popularmovies;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;

import com.fcasado.popularmovies.datatypes.Review;
import com.fcasado.popularmovies.utils.UpBugFixAppCompatActivity;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Review activity, so we can easily show long reviews using {@link android.support.v7.widget.RecyclerView}
 */
public class ReviewActivity extends UpBugFixAppCompatActivity {
    public static final String REVIEW_LIST_EXTRA = " reviewListExtra";
    private static final String LOG_TAG = ReviewActivity.class.getSimpleName();
    @Bind(R.id.recyclerview)
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        ButterKnife.bind(this);

        if (!getIntent().hasExtra(REVIEW_LIST_EXTRA)) {
            Log.d(LOG_TAG, "No reviews to show. Should not happen. Finishing activity.");
            finish();
            return;
        }

        ArrayList<Review> reviews = getIntent().getParcelableArrayListExtra(REVIEW_LIST_EXTRA);
        ReviewAdapter adapter = new ReviewAdapter(reviews);

        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(getResources().getInteger(R.integer.review_columns), StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(adapter);
    }
}
