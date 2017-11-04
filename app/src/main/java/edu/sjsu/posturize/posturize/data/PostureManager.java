package edu.sjsu.posturize.posturize.data;

/**
 * Created by Matthew on 8/28/2017.
 *
 *  TODO:
 *  DONE - Create PostureManager to manage daily/monthly reports
 *  Create write method to track measurements to daily object
 *      ->Requires getDate from DailyPosture
 *  Create getter for current days measurements
 *      ->Requires getter for PostureMeasurements from DailyPosture
 *  Create getter for specified days measurements
 *      ->Requires getDate from DailyPosture
 *  Create getter for specified month measurements
 *
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import edu.sjsu.posturize.posturize.notifications.SlouchNotification;
import edu.sjsu.posturize.posturize.SignInActivity;

/**
 * PostureManager manages all activity coming in, and updates data visualizations.
 */
public class PostureManager {
    private HashMap<String, DailyPosture> dailyPostureMap;
    private int slouchCounter = 0; //Tracks consecutive slouches
    private float threshold = 65.0f; //fixed value for PoC //Represents %error from target calibration/
    private int consistentSlouchThreshold = 5; //Some value that designates how long the user can slouch

    public PostureManager(){
        dailyPostureMap = new HashMap<>();
    }

    public static PostureManager getManager() {
        Gson gson = new Gson();
        Context context = SignInActivity.getAppContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences("USER_DATA", context.MODE_PRIVATE);

        String json = sharedPreferences.getString(sharedPreferences.getString("current_user", ""), ""); //sharedPreferences current_user : email

        return gson.fromJson(json, PostureManager.class);
    }

    public void commit(){
        Context context = SignInActivity.getAppContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences("USER_DATA", context.MODE_PRIVATE);
        String json = new Gson().toJson(this);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        spEditor.putString(sharedPreferences.getString("current_user", ""), json);
        spEditor.commit();
        //Log.d("Wrote to PostureManager", this.toString(new SimpleDateFormat("MM/dd/yyy").format(new Date())));
    }

    /**
     * Writes a new value to daily object
     * @param distance distance received from wearables
     */
    public void writeDistance(float distance) {
        //DateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        String simpleDate = new SimpleDateFormat("MM/dd/yyyy").format(new Date());

        if(!dailyPostureMap.containsKey(simpleDate)){
            dailyPostureMap.put(simpleDate, new DailyPosture());
        }
        dailyPostureMap.get(simpleDate).addMeasurement(distance);
        //Log.d("PostureManager", "after writeDistance: " + dailyPostureMap.get(simpleDate).toString());

        report(distance);
    }

    private void report(float value){
        if(value < threshold){
            slouchCounter++;
            Log.d("SLOUCH DETECTED", "slouchCounter : " + Integer.toString(slouchCounter));
            //Log.d("SLOUCH DETECTED", "YOU BITCH");
            if(slouchCounter >= consistentSlouchThreshold){
                sendNotification();
            }
            return;
        }
        slouchCounter = 0;
    }

    private void sendNotification(){
        SlouchNotification sn = new SlouchNotification(SignInActivity.getAppContext());
        sn.notify(1001);
    }

    public String toString(String simpleDate){
        //Log.d("PostureManger", "contains " + simpleDate + ": " + dailyPostureMap.contain);
        if(dailyPostureMap.containsKey(simpleDate)){
            return simpleDate + ": " + dailyPostureMap.get(simpleDate).toString();
        }
        return simpleDate + ": EMPTY";
    }

    /**
     *
     * @param date get specific date posture data Format "MM/dd/yyyy"
     * @return DailyPosture
     */
    public DailyPosture getDayPostureData(String date){
        return null;
    }
}
