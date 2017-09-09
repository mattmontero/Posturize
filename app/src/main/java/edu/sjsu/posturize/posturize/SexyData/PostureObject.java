package edu.sjsu.posturize.posturize.SexyData;

/**
 * Created by Matthew on 8/28/2017.
 */

import java.util.ArrayList;
import java.util.Date;

/**
 * PotureObject stores 1 month of data, after that, push to DB?
 */
public class PostureObject {
    private DailyPosture mDailyPosture;
    private MonthlyPosture mMonthlyPosture;

    public void writeDistance(float distance) {
        //if currentDate != mDailyPosture.getDate()){ //reset dailyPosture for new day
        //  mMonthlyPosture.add(mDailyPosture);
        //  mDailyPosture.clear()                     //Starts a fresh daily model
        //}
        mDailyPosture.addMeasurement(distance);
    }

    public ArrayList<PostureMeasurement> getTodaysPosture(){
        //return mDailyPosture.getMeasurements(); //return arraylist of postureMeasurements
        return null;
    }

    public DailyPosture getDayPostureData(Date date){
        /*
         *  if date.month == mMonthlyPosture.month
         */
        return null;
    }

    public MonthlyPosture getMonthData(Date date){
        /*
         *  if date == this month && date != future month
         *      return mMonthlyPosture;
         *  else
         *      return new MonthlyPosture(Monthly data from DB);
         */
        return null;
    }
}
