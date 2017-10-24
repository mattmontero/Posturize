package edu.sjsu.posturize.posturize.PostureData;

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

import android.util.Log;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * PostureManager manages all activity coming in, and updates data visualizations.
 */
public class PostureManager {
    private HashMap<String, DailyPosture> dailyPostureMap;

    public PostureManager(){
        dailyPostureMap = new HashMap<>();
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
        Log.d("PostureManager", "after writeDistance: " + dailyPostureMap.get(simpleDate).toString());
    }

    /**
     * Get todays posture data
     * @return ArrayList<PostureMeasurement> of measurements
     */
    public ArrayList<PostureMeasurement> getTodaysPosture(){
        //return mDailyPosture.getMeasurements(); //return arraylist of postureMeasurements
        return null;
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
