package edu.sjsu.posturize.posturize.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import edu.sjsu.posturize.posturize.MainContentActivity;
import edu.sjsu.posturize.posturize.R;

/**
 * Created by Matt on 10/29/2017.
 * SlouchNotification sends a push notification to the app
 */
public class SlouchNotification extends ContextWrapper {
    private NotificationManager mNotificationManager;
    private Context context; //app context
    private CharSequence title = "Looks like you are slouching!";
    private String text = "Tap to view today's progress.";

    public SlouchNotification(Context context){
        super(context);
        this.context = context;
        mNotificationManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
    }

    public void notify(int notificationId){//Right now notificationId does nothing
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.app_icon)
                .setContentTitle(title)
                .setContentText(text);

        //Creates an explicit intent for an activity in your app
        Intent resultIntent = new Intent(context, MainContentActivity.class);
        //The stack builder object will contain an artificial back stack for the started Activity
        //This ensures that navigating backward from the Activity leads out of your app to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        //Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainContentActivity.class);
        //Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        //mNotificationId is a unique integer your app uses to identify the notification.
        //For example, to cancel the notification, you can pass its ID number to NotifactionManager.cancel().
        int mNotificationId = 1001;
        mNotificationManager.notify(mNotificationId, mBuilder.build());
        vibrate();
    }

    private void vibrate(){
        Vibrator vibrator = (Vibrator)context.getSystemService(VIBRATOR_SERVICE);
        if(vibrator.hasVibrator()){
            vibrator.vibrate(800);
        }
    }
}
