package edu.sjsu.posturize.posturize.reminder;

import android.app.AlarmManager;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.Calendar;


/**
 * Created by markbragg on 10/12/17.
 */

public class DailyUpdateFragment extends Fragment {
    // This value is defined and consumed by app code, so any value will work.
    // There's no significance to this sample using 0.
    public static final int REQUEST_CODE = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // BEGIN_INCLUDE (intent_fired_by_alarm)
        // First create an intent for the alarm to activate.
        // This code simply starts an Activity, or brings it to the front if it has already
        // been created.
        Intent intent = new Intent(getActivity(), DailyUpdateActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        // END_INCLUDE (intent_fired_by_alarm)

        // BEGIN_INCLUDE (pending_intent_for_alarm)
        // Because the intent must be fired by a system service from outside the application,
        // it's necessary to wrap it in a PendingIntent.  Providing a different process with
        // a PendingIntent gives that other process permission to fire the intent that this
        // application has created.
        // Also, this code creates a PendingIntent to start an Activity.  To create a
        // BroadcastIntent instead, simply call getBroadcast instead of getIntent.
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), REQUEST_CODE,
                intent, 0);

        // END_INCLUDE (pending_intent_for_alarm)

        // BEGIN_INCLUDE (configure_alarm_manager)
        // There are two clock types for alarms, ELAPSED_REALTIME and RTC.
        // ELAPSED_REALTIME uses time since system boot as a reference, and RTC uses UTC (wall
        // clock) time.  This means ELAPSED_REALTIME is suited to setting an alarm according to
        // passage of time (every 15 seconds, 15 minutes, etc), since it isn't affected by
        // timezone/locale.  RTC is better suited for alarms that should be dependant on current
        // locale.

        // Set the alarm to start at approximately 2:00 p.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 14);

        // The AlarmManager, like most system services, isn't created by application code, but
        // requested from the system.
        AlarmManager alarmManager = (AlarmManager)
                getActivity().getSystemService(getActivity().ALARM_SERVICE);

        // setRepeating takes a start delay and period between alarms as arguments.
//        alarmManager.setInexactRepeating(alarmType, timeInMillis,
//                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
        // END_INCLUDE (configure_alarm_manager);
        Log.i("RepeatingAlarmFragment", "Alarm set.");
    }
}
