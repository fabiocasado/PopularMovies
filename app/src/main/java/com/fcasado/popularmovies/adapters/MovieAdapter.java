
package com.fcasado.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.fcasado.popularmovies.MovieFragment;
import com.fcasado.popularmovies.R;
import com.squareup.picasso.Picasso;

/**
 * Loads ui content from {@link Cursor} and implements ViewHolder pattern for performance. Loads
 * images with Picasso library.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    final private Context mContext;
    final private View mEmptyView;
    final private MovieAdapterOnClickHandler mClickHandler;
    private Cursor mCursor;

    public MovieAdapter(Context context, MovieAdapterOnClickHandler clickHandler, View emptyView) {
        mContext = context;
        mClickHandler = clickHandler;
        mEmptyView = emptyView;
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_movie,
                parent, false);
        view.setFocusable(true);

        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        String portraitPath = mCursor.getString(MovieFragment.COL_POSTER_PATH);
        if (portraitPath != null && portraitPath.length() > 0) {
            portraitPath = mContext.getString(R.string.movie_poster_uri).concat(portraitPath);
            Picasso.with(mContext).load(portraitPath).placeholder(R.drawable.ic_poster)
                    .error(R.drawable.ic_poster_details_error).into(holder.posterView);
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor == null)
            return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public interface MovieAdapterOnClickHandler {
        void onClick(long movieId, MovieAdapterViewHolder viewHolder);
    }

    /**
     * Cache of the children views for a movie list item.
     */
    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        @Bind(R.id.poster_imageview)
        ImageView posterView;

        public MovieAdapterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            mClickHandler.onClick(mCursor.getLong(MovieFragment.COL_MOVIE_ID), this);
        }

        public ImageView getPosterView() {
            return posterView;
        }
    }

}
