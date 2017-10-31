package edu.sjsu.posturize.posturize.PostureData;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Date;

import edu.sjsu.posturize.posturize.SignInActivity;

/**
 * Created by Matthew on 8/28/2017.
 */

/**
 * DailyPosture tracks the posture measurements for a given day.
 */
public class DailyPosture {
    private ArrayList<PostureMeasurement> measurements;

    public DailyPosture(){
        measurements = new ArrayList<PostureMeasurement>();
    }

    public void addMeasurement(float distance){
        /*if((new Date()).compareTo(date) != 0){ //different day, reset daily object
            date = new Date();
            measurements.clear();
        }*/
        measurements.add(new PostureMeasurement(new Date(), distance));
    }

    @Override
    public String toString(){
        return measurements.toString();
    }

}
