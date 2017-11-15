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
    private static String analysis = "Nothing Yet";

    protected void onCreate(Bundle savedInstanceState) {
        Log.d("onCreate", "Starting");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_update);
    }

    public static void setAnalysis(String analysis) {
        DailyUpdateActivity.analysis = analysis;
    }

}
