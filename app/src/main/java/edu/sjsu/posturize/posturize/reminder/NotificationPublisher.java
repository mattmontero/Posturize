package edu.sjsu.posturize.posturize.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;

import java.util.Date;

/**
 * Created by markbragg on 10/26/17.
 */

public class NotificationPublisher extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            Calendar calendar = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                calendar = Calendar.getInstance();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                calendar.setTimeInMillis(System.currentTimeMillis());
            }
            //// TODO: use calendar.add(Calendar.SECOND,MINUTE,HOUR, int);
            //calendar.add(Calendar.SECOND, 10);

            //ALWAYS recompute the calendar after using add, set, roll
            Date date = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                date = calendar.getTime();
            } else {
                date = new Date();
            }

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, date.getTime(), alarmIntent);
        }
    }
}
