package edu.sjsu.posturize.posturize.data;

import java.util.ArrayList;
import java.util.Date;

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
        measurements.add(new PostureMeasurement(new Date(), distance));
    }

    @Override
    public String toString(){
        return measurements.toString();
    }

}
