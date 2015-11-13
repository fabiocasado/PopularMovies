
package com.fcasado.popularmovies.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Special {@link ImageView} to better resemble posters aspect ratio 6:9
 */
public class PosterImageView extends ImageView {
    public PosterImageView(Context context) {
        super(context);
    }

    public PosterImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PosterImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), (int) (getMeasuredWidth() * 1.5f)); // Snap to
                                                                                     // width
    }
}
