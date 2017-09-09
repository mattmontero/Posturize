package edu.sjsu.posturize.posturize.SexyData;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Matthew on 8/28/2017.
 */

public class DailyPosture {
    private ArrayList<PostureMeasurement> measurements;

    DailyPosture(){
        measurements = new ArrayList<PostureMeasurement>();
    }

    public void addMeasurement(float distance){
        measurements.add(new PostureMeasurement(new Date(), distance));
    }
}
