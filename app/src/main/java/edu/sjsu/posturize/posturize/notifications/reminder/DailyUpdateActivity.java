package edu.sjsu.posturize.posturize.notifications.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import edu.sjsu.posturize.posturize.R;

/**
 * Created by markbragg on 10/18/17.
 */

public class DailyUpdateActivity extends AppCompatActivity {
    private String progress;
    private final String reminder = "Don't forget to Posturize! der der";

    protected void onCreate(Bundle savedInstanceState) {
        Log.d("onCreate", "Starting");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_update);
    }

    /**
     * get yesterday's data, compare it with the data from the day before
     * tell user of any improvements or happy constructive criticism
     */
    private void setProgress() {
        // get data
        String data = getDataPastTwoDays();

        String updateAnalysis = getTwoDayAnalysis(data);
    }

    /**
     * perform call to DB to get the data from the day before and yesterday
     * @return data pulled from the database
     */
    private String getDataPastTwoDays() {
        // connect

        // make the call, store the data

        // close connection


        return "datadatadatadata";
    }

    /**
     * break the data into two chunks, compare them, and explain
     * @param data
     * @return
     */
    private String getTwoDayAnalysis(String data) {
        String[] daysData = splitDataIntoDays(data);
        // Firebase stuff
        return data;
    }

    /**
     * currently splits the data in half
     * @param data to be split in half
     * @return data in half
     */
    private String[] splitDataIntoDays(String data) {
        return new String[]{data.substring(0, data.length() / 2), data.substring(data.length() / 2, data.length() - 1)};
    }

    /**
     * sets the daily update on and off depending on the shared preferences value for "pref_key_daily_update"
     * @param context the activity calling the function
     */
    public static void setDailyUpdate(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(context.getApplicationContext().ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,intent,0);
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_daily_update", true)) {
            // TODO: ALLOW FOR SPECIFIC TIME SETTING
            manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 5000/*AlarmManager.INTERVAL_DAY*/, 5000/*AlarmManager.INTERVAL_DAY*/, pendingIntent);
        } else {
            manager.cancel(pendingIntent);
        }
    }
}
