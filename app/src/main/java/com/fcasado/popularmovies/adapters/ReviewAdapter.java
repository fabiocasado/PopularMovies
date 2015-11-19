
package com.fcasado.popularmovies.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.fcasado.popularmovies.R;
import com.fcasado.popularmovies.ReviewsFragment;

/**
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder> {

    private Cursor mCursor;

    @Override
    public ReviewAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_review,
                parent, false);
        view.setFocusable(true);

        return new ReviewAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        String author = mCursor.getString(ReviewsFragment.COL_AUTHOR);
        String content = mCursor.getString(ReviewsFragment.COL_CONTENT);

        holder.authorView.setText(author);
        holder.contentView.setText(content);
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
    }

    public Cursor getCursor() {
        return mCursor;
    }

    /**
     * Cache of the children views for a movie list item.
     */
    public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.review_author_textview)
        TextView authorView;
        @Bind(R.id.review_content_textview)
        TextView contentView;

        public ReviewAdapterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
