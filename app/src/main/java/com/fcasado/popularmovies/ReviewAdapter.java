package com.fcasado.popularmovies;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fcasado.popularmovies.datatypes.Review;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Loads review ui content.
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder> {

    private List<Review> mReviews;

    public ReviewAdapter(List<Review> reviews) {
        mReviews = reviews;
    }

    @Override
    public ReviewAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_review,
                parent, false);
        view.setFocusable(true);

        return new ReviewAdapterViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mReviews == null ? 0 : mReviews.size();
    }

    @Override
    public void onBindViewHolder(ReviewAdapter.ReviewAdapterViewHolder holder, int position) {
        holder.authorView.setText(mReviews.get(position).getAuthor());
        holder.contentView.setText(mReviews.get(position).getContent());
    }

    public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.review_author_textview)
        TextView authorView;
        @Bind(R.id.review_content_textview)
        TextView contentView;

        public ReviewAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
