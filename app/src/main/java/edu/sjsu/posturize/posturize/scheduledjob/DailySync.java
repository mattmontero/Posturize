package edu.sjsu.posturize.posturize.scheduledjob;

import android.util.Log;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import edu.sjsu.posturize.posturize.data.FirebaseHelper;
import edu.sjsu.posturize.posturize.data.localdb.PostureManager;
import edu.sjsu.posturize.posturize.users.GoogleAccountInfo;

/**
 * move yesterday's slouch data from sqlite to firestore
 */
public class DailySync extends JobService {

    private GoogleAccountInfo sUserInfo = GoogleAccountInfo.getInstance();

    @Override
    public boolean onStartJob(JobParameters job) {
        Log.i("DailySync","scheduler job started");
        FirebaseHelper firestoreHelper = FirebaseHelper.getInstance();
        PostureManager postureManager = new PostureManager(this);

        Log.i("DailySync", "accessing sqlite");
        postureManager.openDB();

        for(String id : postureManager.getAllUser()){
            Log.i("DailySync", id);

            ArrayList<Date> times = new ArrayList<>();
            ArrayList<Double> slouches = new ArrayList<>();

            Calendar yesterday = Calendar.getInstance();
            yesterday.setTimeInMillis(new Date().getTime()-3600000);
            ArrayList<DataPoint> postures = postureManager.get(id, yesterday);
            for(DataPoint p : postures){
                times.add(new Date((long) p.getX()));
                slouches.add(p.getY());
            }
            HashMap<String, Object> data = new HashMap<>();
            data.put("times", times);
            data.put("slouches", slouches);

            Log.i("DailySync", "updated firestore");
            firestoreHelper.addSlouchesToFirestoreForUser(id, data);

            //delete local SQLite data
            postureManager.delete(id);

            Log.i("Daily Sync","scheduler job finished");
        }

        postureManager.closeDB();
        return true; // No more work to do
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false; // No more work to do
    }
}