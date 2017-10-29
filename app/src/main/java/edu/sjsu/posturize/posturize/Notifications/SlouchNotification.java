package edu.sjsu.posturize.posturize.Notifications;

import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;

/**
 * Created by Matt on 10/29/2017.
 */

public class SlouchNotification extends ContextWrapper {
    private NotificationManager mManager;

    public SlouchNotification(Context context){
        super(context);
    }
}
