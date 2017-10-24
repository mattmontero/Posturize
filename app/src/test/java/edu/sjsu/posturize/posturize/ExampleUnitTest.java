package edu.sjsu.posturize.posturize;

import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import edu.sjsu.posturize.posturize.PostureData.PostureManager;
import edu.sjsu.posturize.posturize.PostureData.PostureMeasurement;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void posturizeObject_isGud() throws Exception {
        int userDefinedReadDelay = 1; //arduino sends reading every second
        float numbers[] = {1.02f, 1.05f, 1.07f, 1.1f, 1.09f, 1.12f};
        PostureManager po = new PostureManager();
        for(int i = 0; i < numbers.length; i++){
            po.writeDistance(numbers[i]);
            TimeUnit.SECONDS.sleep(userDefinedReadDelay);
        }
        ArrayList<PostureMeasurement> tempMeasurements = po.getTodaysPosture();

        /*
          for i=0 to tmpMeasurments.lenght-1
             if (i + 1) > isOutOfbounds
                brk;
             assertLessThan(tmpMeasuremnts.get(i).date, tmpMeasurements.get(i+1).date)
         */
    }
}