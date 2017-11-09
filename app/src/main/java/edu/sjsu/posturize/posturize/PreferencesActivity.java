package edu.sjsu.posturize.posturize;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import edu.sjsu.posturize.posturize.notifications.reminder.DailyUpdateActivity;

/**
 * Created by markbragg on 11/7/17.
 */

public class PreferencesActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    private final String KEY_PREF_DAILY_UPDATE = "pref_key_daily_update";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.preferences);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_DAILY_UPDATE)) {
            setDailyUpdate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setDailyUpdate() {
        DailyUpdateActivity.setDailyUpdate(this);
    }
}
