
package com.fcasado.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity implements MovieFragment.OnMovieItemSelected {

    private static final String TAG_MOVIE_DETAIL = "tagMovieDetail";
    private static final String SELECTED_URI = "selectedUri";

    private boolean mTwoPane;
    private Uri mSelectedUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Restored saved values if available
        if (savedInstanceState != null) {
            mSelectedUri = savedInstanceState.getParcelable(SELECTED_URI);
        }

        // If we are in large-screen layouts, we will use two pane mode to show two fragments at
        // same time and create better UI/UX.
        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.movie_detail_container,
                        new DetailFragment(), TAG_MOVIE_DETAIL).commit();
            }

        } else {
            mTwoPane = false;
        }

        // If we are in two pane view, we want to scroll to last selected item to have gridview
        // showing it always. In one pane view we avoid this because it may cause bad UX at times.
        MovieFragment movieFrag = (MovieFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_movie);
        if (movieFrag != null) {
            movieFrag.setShouldScrollToSelectedItem(mTwoPane);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(SELECTED_URI, mSelectedUri);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onMovieItemSelected(Uri contentUri, View sharedView) {
        if (mTwoPane) {
            // If we are already showing the selected movie, we avoid adding new fragment with same
            // info
            if (mSelectedUri != null && mSelectedUri.compareTo(contentUri) == 0) {
                return;
            }

            // Update selected uri and item position
            mSelectedUri = contentUri;

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            ft.replace(R.id.movie_detail_container, fragment, TAG_MOVIE_DETAIL).commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class).setData(contentUri);

            if (sharedView != null) {
                String transitionName = getString(R.string.transition_image);
                Bundle bundle = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(this, sharedView, transitionName).toBundle();
                startActivity(intent, bundle);
            } else {
                startActivity(intent);
            }
        }

    }
}
