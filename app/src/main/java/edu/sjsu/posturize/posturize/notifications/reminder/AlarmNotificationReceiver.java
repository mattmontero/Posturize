package edu.sjsu.posturize.posturize.notifications.reminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import javax.security.auth.callback.Callback;

import edu.sjsu.posturize.posturize.R;
import edu.sjsu.posturize.posturize.SignInActivity;
import edu.sjsu.posturize.posturize.data.FirebaseHelper;

/**
 * Created by markbragg on 10/26/17.
 */

public class AlarmNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        Notification notification = builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Don't forget to Posturize today!")
//                .setContentText("Tap to view your daily analysis")
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
//                .setContentInfo("Info")
                .setAutoCancel(true)
                .build();
        NotificationManager nm = (NotificationManager) context.getSystemService((Context.NOTIFICATION_SERVICE));
        nm.notify(1, notification);
    }
}
