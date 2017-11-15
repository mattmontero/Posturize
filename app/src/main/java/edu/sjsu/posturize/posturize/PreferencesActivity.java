package edu.sjsu.posturize.posturize;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import edu.sjsu.posturize.posturize.notifications.reminder.AlarmNotificationReceiver;

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
        AlarmManager manager = (AlarmManager) getSystemService(getApplicationContext().ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,intent,0);
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_key_daily_update", true)) {
            // TODO: ALLOW FOR SPECIFIC TIME SETTING
            manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 5000/*AlarmManager.INTERVAL_DAY*/, 5000/*AlarmManager.INTERVAL_DAY*/, pendingIntent);
        } else {
            manager.cancel(pendingIntent);
        }
    }
}
