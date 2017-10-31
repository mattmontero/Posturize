package edu.sjsu.posturize.posturize.PostureData;

import java.util.Date;

/**
 * Created by Matthew on 8/28/2017.
 */

/**
 * PostureMeasurement stores a distant measurement (float) and with a timestamp.
 */
public class PostureMeasurement {
    private Date timestamp;
    private float distance;

    public PostureMeasurement(Date date, float distance){
        this.timestamp = date;
        this.distance = distance;
    }

    @Override
    public String toString(){
        return "[" + distance + ", " + timestamp.toString() + "]";
    }

}
