package edu.sjsu.posturize.posturize.PostureData;

/**
 * Created by Matthew on 8/28/2017.
 *
 *  TODO:
 *  DONE - Create PostureObject to manage daily/monthly reports
 *  Create write method to track measurements to daily object
 *      ->Requires getDate from DailyPosture
 *  Create getter for current days measurements
 *      ->Requires getter for PostureMeasurements from DailyPosture
 *  Create getter for specified days measurements
 *      ->Requires getDate from DailyPosture
 *  Create getter for specified month measurements
 *
 */

import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

/**
 * PotureObject manages all activity coming in, and updates data visualizations.
 */
public class PostureObject {
    private DailyPosture mDailyPosture;

    public PostureObject(){
        mDailyPosture = new DailyPosture();
    }

    /**
     * Writes a new value to daily object
     * @param distance distance received from wearables
     */
    public void writeDistance(float distance) {
        mDailyPosture.addMeasurement(distance);
    }

    /**
     * Get todays posture data
     * @return ArrayList<PostureMeasurement> of measurements
     */
    public ArrayList<PostureMeasurement> getTodaysPosture(){
        //return mDailyPosture.getMeasurements(); //return arraylist of postureMeasurements
        return null;
    }

    /**
     *
     * @param date get specific date posture data Format "MM-dd-yyy"
     * @return DailyPosture
     */
    public DailyPosture getDayPostureData(String date){
        return null;
    }
}
