package edu.sjsu.posturize.posturize.data;

import java.util.Date;

/**
 * Created by Matthew on 8/28/2017.
 */

/**
 * PostureMeasurement stores a distant measurement (float) and with a timestamp.
 */
public class PostureMeasurement {
    public final long timestamp;
    public final float distance;

    public PostureMeasurement(long date, float distance){
        this.timestamp = date;
        this.distance = distance;
    }

    @Override
    public String toString(){
        return "[" + distance + ", " + timestamp + "]";
    }

}
