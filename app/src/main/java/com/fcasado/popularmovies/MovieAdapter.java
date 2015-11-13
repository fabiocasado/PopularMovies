
package com.fcasado.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Loads ui content from {@link Cursor} and implements ViewHolder pattern for performance. Loads
 * images with Picasso library.
 */
public class MovieAdapter extends CursorAdapter {

    public MovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item_movie, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String portraitPath = cursor.getString(MovieFragment.COL_POSTER_PATH);
        if (portraitPath != null && portraitPath.length() > 0) {
            portraitPath = context.getString(R.string.movie_poster_uri).concat(portraitPath);
            Picasso.with(context).load(portraitPath).placeholder(R.drawable.ic_poster)
                    .into(viewHolder.posterView);
        }
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    static class ViewHolder {
        @Bind(R.id.poster_imageview) ImageView posterView;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
