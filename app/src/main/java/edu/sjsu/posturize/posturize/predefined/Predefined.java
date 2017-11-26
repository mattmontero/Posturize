package edu.sjsu.posturize.posturize.predefined;

import com.jjoe64.graphview.series.DataPoint;

import java.text.DecimalFormat;
import java.util.Calendar;

/**
 * Created by Matt on 11/25/2017.
 */

public class Predefined {
    private static Calendar calendar = resetDay(Calendar.getInstance());
    public static DataPoint[] SQLiteEntries = {
                    randDataPoint(), randDataPoint(), randDataPoint(),
                    randDataPoint(), randDataPoint(), randDataPoint(),
                    randDataPoint(), randDataPoint(), randDataPoint(),
                    randDataPoint(), randDataPoint(), randDataPoint(),
                    randDataPoint(), randDataPoint(), randDataPoint()
    };

    private static Calendar resetDay(Calendar c){
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);
        return c;
    }

    private static DataPoint randDataPoint(){
        advance(calendar);
        advance(calendar);
        return new DataPoint(advance(calendar), randDouble());
    }

    private static double randDouble(){
        DecimalFormat df = new DecimalFormat("#.00");
        return Double.parseDouble(df.format(((Math.random() * (5 - 3) + 3) * -1)));
    }

    private static double advance(Calendar c){
        if(c.get(Calendar.MINUTE) < 30) {
            c.set(Calendar.MINUTE, 30);
        } else {
            c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY) + 1);
            c.set(Calendar.MINUTE, 0);
        }
        return c.getTimeInMillis();
    }
}
