package edu.sjsu.posturize.posturize.data;

import android.content.Context;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by markbragg on 10/19/17.
 */

public class FirebaseHelper {
    private static FirebaseHelper instance;
    private FirebaseAnalytics sFirebaseAnalytics;

    private FirebaseHelper(Context context) {
        sFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public static FirebaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseHelper(context);
        }
        return instance;
    }
}
