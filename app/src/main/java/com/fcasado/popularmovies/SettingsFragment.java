
package com.fcasado.popularmovies;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * Created by fcasado on 10/11/2015.
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        setPreferenceSummary(getString(R.string.pref_sort_key));
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setPreferenceSummary(key);
    }

    private void setPreferenceSummary(String key) {
        Preference preference = getPreferenceScreen().findPreference(key);
        int keyTitleRes = preference.getTitleRes();
        switch (keyTitleRes) {
            case R.string.pref_sort_title:
                ListPreference listPref = (ListPreference) preference;
                int valueIndex = listPref.findIndexOfValue(listPref.getValue());
                CharSequence[] entryValues = listPref.getEntries();
                listPref.setSummary(entryValues[valueIndex]);
                break;
            default:
                break;
        }
    }
}
