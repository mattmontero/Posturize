package edu.sjsu.posturize.posturize.reminder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import edu.sjsu.posturize.posturize.R;

/**
 * Created by markbragg on 10/18/17.
 */

public class DailyUpdateActivity extends AppCompatActivity {
    private String progress;
    private final String reminder = "Don't forget to Posturize! der der";

    protected void onCreate(Bundle savedInstanceState) {
        Log.d("onCreate", "Starting");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_update);
    }

    /**
     * get yesterday's data, compare it with the data from the day before
     * tell user of any improvements or happy constructive criticism
     */
    private void setProgress() {
        // get data
        String data = getDataPastTwoDays();

        String updateAnalysis = getTwoDayAnalysis(data);
    }

    /**
     * perform call to DB to get the data from the day before and yesterday
     * @return data pulled from the database
     */
    private String getDataPastTwoDays() {
        // connect

        // make the call, store the data

        // close connection


        return "datadatadatadata";
    }

    /**
     * break the data into two chunks, compare them, and explain
     * @param data
     * @return
     */
    private String getTwoDayAnalysis(String data) {
        String[] daysData = splitDataIntoDays(data);
        // Firebase stuff
        return data;
    }

    /**
     * currently splits the data in half
     * @param data to be split in half
     * @return data in half
     */
    private String[] splitDataIntoDays(String data) {
        return new String[]{data.substring(0, data.length() / 2), data.substring(data.length() / 2, data.length() - 1)};
    }
}
