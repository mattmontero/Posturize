package edu.sjsu.posturize.posturize.predefined;

import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;

import java.text.DecimalFormat;
import java.util.Calendar;

/**
 * Created by Matt on 11/25/2017.
 * Used to popoulate fake data
 */

public class Predefined {
    private static Calendar calendar = resetDay(Calendar.getInstance());
    public static DataPoint[] SQLiteEntries = {
                    //randDataPoint(), randDataPoint(), randDataPoint(),
                    //randDataPoint(), randDataPoint(), randDataPoint(),
                    randDataPoint(), randDataPoint(), randDataPoint(),
                    //randDataPoint(),
            randDataPoint(),
            randDataPoint(),
                    randDataPoint(), randDataPoint(), randDataPoint()
    };

    private static Calendar resetDay(Calendar c){
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 1);
        return c;
    }

    private static DataPoint randDataPoint(){
        advance(calendar);
        //advance(calendar);
        //advance(calendar);
        Log.d("Predefined", "Adding: Hours: " + calendar.get(Calendar.HOUR_OF_DAY) + " Minutes: " + calendar.get(Calendar.MINUTE));
        Log.d("Predefined", "Time: " + calendar.getTime());
        return new DataPoint(calendar.getTimeInMillis(), randDouble());
    }

    private static double randDouble(){
        DecimalFormat df = new DecimalFormat("#.00");
        return Double.parseDouble(df.format((float)((Math.random() * (5 - 3) + 3) * -1)));
    }

    private static double advance(Calendar c){
        //Log.d("Predefined", "Before advance: Hours: " + c.get(Calendar.HOUR_OF_DAY) + " Minutes: " + c.get(Calendar.MINUTE));
        if(c.get(Calendar.MINUTE) < 30) {
            c.set(Calendar.MINUTE, 30);
        } else {
            c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY) + 1);
            c.set(Calendar.MINUTE, 0);
        }
        //Log.d("Predefined", "After advance: Hours: " + c.get(Calendar.HOUR_OF_DAY) + " Minutes: " + c.get(Calendar.MINUTE));
        return c.getTimeInMillis();
    }
}
