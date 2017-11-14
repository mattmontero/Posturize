package edu.sjsu.posturize.posturize.notifications.reminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import javax.security.auth.callback.Callback;

import edu.sjsu.posturize.posturize.R;
import edu.sjsu.posturize.posturize.data.FirebaseHelper;

/**
 * Created by markbragg on 10/26/17.
 */

public class AlarmNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Posturize Daily Update")
                .setContentText(FirebaseHelper.getInstance().getDailyAnalysis())
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentInfo("Info");
        NotificationManager nm = (NotificationManager) context.getSystemService((Context.NOTIFICATION_SERVICE));
        nm.notify(1, builder.build());
    }
}
