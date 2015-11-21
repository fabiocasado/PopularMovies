
package com.fcasado.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.fcasado.popularmovies.datatypes.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Loads ui content. Loads images with Picasso
 * library.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private Context mContext;
    private TextView mEmptyView;
    private List<Movie> mMovies;
    private MovieAdapterOnClickListener mMovieAdapterOnClickListener;

    public MovieAdapter(Context context, TextView emptyView, List<Movie> movies,
                        MovieAdapterOnClickListener onClickListener) {
        mContext = context;
        mEmptyView = emptyView;
        mMovieAdapterOnClickListener = onClickListener;

        setMovies(movies);
    }

    public void setMovies(List<Movie> movies) {
        mMovies = movies;
        notifyDataSetChanged();

        if (mMovies == null || mMovies.size() == 0) {
            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            String prefSortBy = preferences.getString(mContext.getString(R.string.pref_sort_key),
                    mContext.getString(R.string.sort_most_popular));
            if (prefSortBy.compareTo(mContext.getString(R.string.sort_favorite)) == 0) {
                mEmptyView.setText(mContext.getString(R.string.no_favorite_movies));
            } else {
                mEmptyView.setText(mContext.getString(R.string.no_movie_data_available));
            }
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_movie,
                parent, false);
        view.setFocusable(true);

        return new MovieAdapterViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mMovies == null ? 0 : mMovies.size();
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position) {
        String portraitPath = mMovies.get(position).getPosterPath();
        if (portraitPath != null && portraitPath.length() > 0) {
            portraitPath = mContext.getString(R.string.movie_poster_uri).concat(portraitPath);
            Picasso.with(mContext).load(portraitPath).placeholder(R.drawable.ic_poster)
                    .error(R.drawable.ic_poster_error)
                    .into(holder.posterView);
        }
    }

    public interface MovieAdapterOnClickListener {
        void onClick(Movie movie, int position);
    }

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        @Bind(R.id.poster_imageview)
        ImageView posterView;

        public MovieAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mMovieAdapterOnClickListener.onClick(mMovies.get(position), position);
        }
    }
}
