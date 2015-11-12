
package com.fcasado.popularmovies;

import android.os.Bundle;
import android.view.MenuItem;

import com.fcasado.popularmovies.utils.UpBugFixAppCompatActivity;

/**
 * Created by fcasado on 07/11/2015.
 */
public class DetailActivity extends UpBugFixAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Right now up button doesn't call supportFinishAfterTransition, so we lose the
                // movie poster animation when returning to app, so we intercept this and call the
                // onBackPressed method, which does call supportFinishAfterTransition when
                // supported.
                onBackPressed();
                break;
        }
        return true;
    }

}
