
package com.fcasado.popularmovies.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;

/**
 * This class whole purpose is to make sure we use it to extend AppCompatActivity indirectly and
 * thus, add the fix for the up parent bug that causes the parent activity to recreate instead of
 * come to top when pressing the ActionBar up button.
 */
@SuppressLint("Registered")
public class UpBugFixAppCompatActivity extends AppCompatActivity {
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
