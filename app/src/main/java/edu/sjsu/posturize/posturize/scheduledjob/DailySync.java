package edu.sjsu.posturize.posturize.scheduledjob;

import android.os.Bundle;
import android.util.Log;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import edu.sjsu.posturize.posturize.data.FirebaseHelper;
import edu.sjsu.posturize.posturize.data.PostureMeasurement;
import edu.sjsu.posturize.posturize.data.localdb.PostureManager;
import edu.sjsu.posturize.posturize.users.PosturizeUserInfo;

/** A very simple JobService that merely stores its result and immediately finishes. */
public class DailySync extends JobService {
    @Override
    public boolean onStartJob(JobParameters job) {
        Log.i("DailySync","scheduler job started");
        FirebaseHelper firestoreHelper = FirebaseHelper.getInstance();
        PostureManager postureManager = new PostureManager(this);

        ArrayList<Date> times = new ArrayList<>();
        ArrayList<Double> slouches = new ArrayList<>();

        Log.i("DailySync", "accessing sqlite");
        postureManager.openDB();
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTimeInMillis(new Date().getTime()-3600000);
        ArrayList<PostureMeasurement> postures = postureManager.get(yesterday);
        for(PostureMeasurement p : postures){
            times.add(new Date(p.timestamp/1000));
            slouches.add(new Double(p.distance));
        }
        HashMap<String, Object> data = new HashMap<>();
        data.put("times", times);
        data.put("slouches", slouches);

        Log.i("DailySync", "updated firestore");
        firestoreHelper.addSlouchesToFirestoreForUser(data);


        Log.i("DailySync", "task finished");
        return true; // No more work to do
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        Log.i("Daily Sync","scheduler job finished");
        return false; // No more work to do
    }
}