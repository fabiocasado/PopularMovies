
package com.fcasado.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by fcasado on 05/11/2015.
 */
public class FetchMovieFragment extends Fragment {
    private static final String LOG_TAG = FetchMovieFragment.class.getSimpleName();
    private FetchMovieTask mTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mTask == null) {
            mTask = new FetchMovieTask(getActivity());
            mTask.execute();
        }
    }
}
