package edu.sjsu.posturize.posturize.visualizations;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.common.collect.ImmutableMap;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.Series;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import edu.sjsu.posturize.posturize.R;
import edu.sjsu.posturize.posturize.SignInActivity;
import edu.sjsu.posturize.posturize.bluetooth.BluetoothConnection;
import edu.sjsu.posturize.posturize.data.localdb.PostureManager;
import edu.sjsu.posturize.posturize.users.GoogleAccountInfo;

/**
 * Created by Matt on 11/28/2017.
 * GraphManager handles interaction and modification of the GraphView.
 */

public class GraphManager implements Observer {
    private PostureManager mPostureManager;
    private ArrayList<DataPoint> mDataPoints;
    private View mView;
    private GraphView mGraphView;
    private Context context;

    private static final Map<Integer, String> lableFormatter = ImmutableMap.<Integer, String>builder()
            .put(0, "12\nAM").put(1, "1").put(2, "2").put(3, "3").put(4, "4").put(5, "5").put(6, "6").put(7, "7")
            .put(8, "8").put(9, "9").put(10, "10").put(11, "11").put(12, "12\nPM").put(13, "1").put(14, "2").put(15, "3")
            .put(16, "4").put(17, "5").put(18, "6").put(19, "7").put(20, "8").put(21, "9").put(22, "10").put(23, "11")
            .build();

    public GraphManager(View activity){
        mView = activity;
        mPostureManager = BluetoothConnection.getInstance().getPostureManager();
        mDataPoints = new ArrayList<>();
        context = SignInActivity.getAppContext();
        mGraphView = (GraphView) mView.findViewById(R.id.graphView);

        //Manual Bounds
        mGraphView.getViewport().setXAxisBoundsManual(true);
        mGraphView.getViewport().setYAxisBoundsManual(true);

        //Set Y min and max
        mGraphView.getViewport().setMinY(-40); //percent off
        mGraphView.getViewport().setMaxY(1);   //on track

        setInteractionListeners();
        updateDataPoints();
        hide();
    }

    /**
     * Sets graph visibility to GONE
     */
    public void hide(){
        mGraphView.setVisibility(View.GONE);
    }

    /**
     * Sets graph visibility to VISIBLE
     */
    public void show(){
        mGraphView.setVisibility(View.VISIBLE);
    }

    /**
     * Sets gesture recognition for graph
     */
    public void setInteractionListeners(){
        final GestureDetector sgd = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e){
                addRecord();
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e){
                populateSQLite();
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY){
                deleteUserRecords();
                return true;
            }
        });

        mView.findViewById(R.id.graphView).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                sgd.onTouchEvent(motionEvent);
                return false;
            }
        });
    }

    /**
     * Sets the new max and min for x axis
     * @param min
     * @param max
     */
    private void viewportX(double min, double max){
        mGraphView.getViewport().setMinX(min);
        mGraphView.getViewport().setMaxX(max);
    }

    /**
     * Sets the new min and max for y axis
     * @param min
     * @param max
     */
    private void viewportY(double min, double max){
        mGraphView.getViewport().setMinY(min);
        mGraphView.getViewport().setMaxY(max);
    }

    /**
     * Updates graph DataPoints
     */
    public void updateDataPoints(){
        if(!mPostureManager.isDBopen()) {
            mPostureManager.openDB();
            mDataPoints = mPostureManager.get(Calendar.getInstance());
            mPostureManager.closeDB();
        } else {
            mDataPoints = mPostureManager.get(Calendar.getInstance());
        }

        if(!mDataPoints.isEmpty()) {
            modifyGraphData(mDataPoints);
        } else {
            mGraphView.removeAllSeries();
            mGraphView.getGridLabelRenderer().reloadStyles();
        }
    }

    /**
     * Modifies the data series in the graph and redraws
     * @param points new set of datapoints
     */
    private void modifyGraphData(ArrayList<DataPoint> points){
        mGraphView.removeAllSeries();

        PointsGraphSeries<DataPoint> series = new PointsGraphSeries<>();
        Calendar c = Calendar.getInstance();
        double minY = -10.0;
        for(DataPoint dp : points){
            if (dp.getY() < minY){
                minY = dp.getY()-5;
            }
            c.setTime(new Date((long) dp.getX()));
            series.appendData(new DataPoint(timeInHours(c), dp.getY()), false, 80000);
        }

        series.setColor(Color.BLUE);
        c.setTime(new Date((long) points.get(0).getX())); //use Calendar c as start time.
        setHorizontalLabels(c , Calendar.getInstance());
        viewportY(minY, 1);
        mGraphView.addSeries(series);

        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                double minute = dataPoint.getX() % 1;
                double hour = dataPoint.getX() - minute;
                minute = 60 * minute;

                String ampm;
                if(hour > 12){
                    hour -= 12;
                    ampm = "PM";
                } else {
                    ampm = "AM";
                }
                String time = (int) hour + ":" + String.format("%02d",(int) minute) + ampm;

                Toast.makeText(context, "DataPoint info: " + time, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double timeInHours(Calendar c){
        long now = c.getTimeInMillis();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long passed = now - c.getTimeInMillis();
        double hoursPassed = (double)passed / (double)(1000*60*60);
        return hoursPassed;
    }

    /**
     * Resets the calendar to the beginning of the hour current hour, or to the beginning of the next hour
     * @param cal Calendar to reset the time
     * @param nextHour True if set to next hour, false if set to current hour
     * @return new Calendar of time reset to current or next hour.
     */
    private Calendar setHour(Calendar cal, boolean nextHour){
        Calendar c = (Calendar) cal.clone();
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        if(nextHour){
            c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY)+1);
        }
        return c;
    }

    private void setHorizontalLabels(Calendar start, Calendar end){
        double min = timeInHours(setHour(start, false));
        double max = timeInHours(setHour(Calendar.getInstance(), true));

        if(min%2 != 0){
            min -= 1;
            start.set(Calendar.HOUR_OF_DAY, start.get(Calendar.HOUR_OF_DAY) - 1);
        }
        if(max%2 != 0 && max != 23){
            max += 1;
            end.set(Calendar.HOUR_OF_DAY, end.get(Calendar.HOUR_OF_DAY) + 1);
        }

        viewportX(min, max);

        int startTime = (int) timeInHours(setHour(start, false));
        int endTime = (int) timeInHours(setHour(end, true));
        int numOfHorLabels = (endTime - startTime + 1);

        mGraphView.getGridLabelRenderer().setNumHorizontalLabels(numOfHorLabels+1);

        int hourStep = 1;
        if(numOfHorLabels > 6){ //If used longer than 6 hours, use 2 hour steps
            hourStep = 2;
            numOfHorLabels /= 2;
        }


        ArrayList<String> timeFrame = new ArrayList<>();
        for(int i = startTime; i <= endTime; i+=hourStep){
            if(i == startTime && !lableFormatter.get(i).contains("M")){
                String firstLabel = lableFormatter.get(i);
                firstLabel = (i<12) ? firstLabel.concat("AM") : firstLabel.concat("PM");
                timeFrame.add(firstLabel);
            } else {
                timeFrame.add(lableFormatter.get(i));
            }
        }

        StaticLabelsFormatter slf = new StaticLabelsFormatter(mGraphView);
        slf.setHorizontalLabels(timeFrame.toArray(new String[numOfHorLabels]));
        mGraphView.getGridLabelRenderer().setLabelFormatter(slf);
        mGraphView.getGridLabelRenderer().setTextSize(40f);
        mGraphView.getGridLabelRenderer().reloadStyles();
    }

    /*******************************/
    /********onTouch methods********/
    private void deleteUserRecords(){
        mPostureManager.openDB();
        mPostureManager.delete(GoogleAccountInfo.getInstance().getId());
        mPostureManager.closeDB();
    }

    private void addRecord() {
        mPostureManager.openDB();
        DecimalFormat df = new DecimalFormat("#.00");
        float value = Float.parseFloat(df.format((float)(-(Math.random() * (5 - 3) + 3))));
        mPostureManager.insert(value);
        mPostureManager.closeDB();
    }

    private void populateSQLite(){
        if(!mPostureManager.isDBopen()) {
            mPostureManager.fakeIt();
        }
    }
    /********onTouch methods********/
    /*******************************/

    /********************************/
    /********Observer Methods********/
    public void startObserving(){
        mPostureManager.addObserver(this);
        //Log.d("GraphManager", "Observing PostureManager");
    }

    public void stopObserving(){
        mPostureManager.deleteObserver(this);
        //Log.d("GraphManager", "Stopped observing PostureManager");
    }

    @Override
    public void update(Observable observable, Object o) {
        Log.d("GraphManager", "Notified");
        updateDataPoints();
    }
    /********Observer Methods********/
    /********************************/
}
