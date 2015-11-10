
package com.fcasado.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fcasado.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * Created by fcasado on 10/11/2015.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    static final String DETAIL_URI = "URI";

    private static final int DETAIL_LOADER = 0;

    private Uri mUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        System.out.println("Uri: " + mUri);

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(getActivity(), mUri, null, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            // Read weather condition ID from cursor
            int titleIndex = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE);
            int posterIndex = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH);

            TextView titleView = (TextView) getView().findViewById(R.id.title_textview);
            ImageView posterView = (ImageView) getView().findViewById(R.id.poster_imageview);

            titleView.setText(data.getString(titleIndex));

            String portraitPath = data.getString(posterIndex);
            if (portraitPath != null && portraitPath.length() > 0) {
                portraitPath = "http://image.tmdb.org/t/p/w185/".concat(portraitPath);
                Picasso.with(getActivity()).load(portraitPath).placeholder(R.drawable.ic_poster)
                        .into(posterView);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
