package com.fcasado.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.fcasado.popularmovies.data.Movie;

public class MainActivity extends AppCompatActivity
        implements MovieFragment.OnFavoriteItemSelected, MovieFragment.OnMovieItemSelected {

    private static final String TAG_MOVIE_DETAIL = "tagMovieDetail";
    private static final String SELECTED_MOVIE = "selectedMovie";

    private boolean mTwoPane;
    private Movie mSelectedMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Restored saved values if available
        if (savedInstanceState != null) {
            mSelectedMovie = savedInstanceState.getParcelable(SELECTED_MOVIE);
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
        outState.putParcelable(SELECTED_MOVIE, mSelectedMovie);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onFavoriteItemSelected(Uri contentUri, View sharedView) {

    }

    @Override
    public void onMovieItemSelected(Movie movie) {
        if (mTwoPane) {
            // If we are already showing the selected movie, we avoid adding new fragment with same
            // info
            if (mSelectedMovie == movie) {
                return;
            }

            // Update selected id
            mSelectedMovie = movie;

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_MOVIE, mSelectedMovie);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            ft.replace(R.id.movie_detail_container, fragment, TAG_MOVIE_DETAIL).commit();
        } else {
            // Update selected movie
            mSelectedMovie = movie;

            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra(DetailFragment.DETAIL_MOVIE, mSelectedMovie);
            startActivity(intent);
        }
    }
}
