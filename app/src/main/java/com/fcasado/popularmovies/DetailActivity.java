
package com.fcasado.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.fcasado.popularmovies.data.MovieContract;
import com.fcasado.popularmovies.utils.UpBugFixAppCompatActivity;
import com.squareup.picasso.Picasso;

/**
 * Created by fcasado on 07/11/2015.
 */
public class DetailActivity extends UpBugFixAppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ImageView test = (ImageView) findViewById(R.id.grid_item_poster_imageview);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            Uri contentUri = getIntent().getData();

            Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
            if (cursor.moveToFirst()) {
                String portraitPath = cursor.getString(
                        cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH));
                if (portraitPath != null && portraitPath.length() > 0) {
                    portraitPath = "http://image.tmdb.org/t/p/w185/".concat(portraitPath);
                    Picasso.with(this).load(portraitPath).placeholder(R.drawable.ic_poster).noFade()
                            .into(test);
                }

                TextView titleView = (TextView) findViewById(R.id.title_textview);
                titleView.setText(cursor
                        .getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE)));
            }

            cursor.close();
        }
    }
}
