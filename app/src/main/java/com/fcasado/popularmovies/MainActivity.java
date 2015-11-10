
package com.fcasado.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements MovieFragment.OnMovieItemSelected {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
