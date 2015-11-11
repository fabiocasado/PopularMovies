
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
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements MovieFragment.OnMovieItemSelected {

    private static final String TAG_MOVIE_DETAIL = "tagMovieDetail";
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            getSupportActionBar().setElevation(0f);
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
    public void onMovieItemSelected(Uri contentUri, View sharedView) {
        if (mTwoPane) {
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
            ImageView imageView = (ImageView) sharedView;

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
