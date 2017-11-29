package edu.sjsu.posturize.posturize.visualizations;

import android.app.Activity;
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
 */

public class GraphManager implements Observer {
    private PostureManager mmPostureManager;
    private ArrayList<DataPoint> mmDatapoints;
    private View mView;
    private GraphView mmGraphView;
    private Context context;

    private View.OnLongClickListener onLongClickListener = new View.OnLongClickListener(){
        @Override
        public boolean onLongClick(View view) {
            if (view.getId() == R.id.graphView){
                populateSQLite();
                return true;
            }
            return  false;
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.add_point:
                    addRecord();
                    break;
                case R.id.delete_points:
                    deleteUserRecords();
                    break;
            }
        }
    };

    private static final Map<Integer, String> lableFormatter = ImmutableMap.<Integer, String>builder()
            .put(0, "12\nAM").put(1, "1").put(2, "2").put(3, "3").put(4, "4").put(5, "5").put(6, "6").put(7, "7")
            .put(8, "8").put(9, "9").put(10, "10").put(11, "11").put(12, "12\nPM").put(13, "1").put(14, "2").put(15, "3")
            .put(16, "4").put(17, "5").put(18, "6").put(19, "7").put(20, "8").put(21, "9").put(22, "10").put(23, "11")
            .build();

    public GraphManager(View activity){
        mView = activity;
        mmPostureManager = BluetoothConnection.getInstance().getPostureManager();
        mmPostureManager.openDB();
        mmDatapoints = mmPostureManager.get(Calendar.getInstance());
        mmPostureManager.closeDB();
        context = SignInActivity.getAppContext();
        constructGraph();
    }

    private void constructGraph(){
        mmGraphView = (GraphView) mView.findViewById(R.id.graphView);

        //Manual Bounds
        mmGraphView.getViewport().setXAxisBoundsManual(true);
        mmGraphView.getViewport().setYAxisBoundsManual(true);

        //Set Y min and max
        mmGraphView.getViewport().setMinY(-40); //percent off
        mmGraphView.getViewport().setMaxY(1);   //on track
        //Set X min and max
        viewportX(0,1000); //Just some default values to construct
        startObserving();
        hide();
    }

    public void hide(){
        mmGraphView.setVisibility(View.GONE);
    }

    public void show(){
        mmGraphView.setVisibility(View.VISIBLE);
        setOnClickListeners();
    }

    public void setOnClickListeners(){
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

    private void viewportX(double min, double max){
        mmGraphView.getViewport().setMinX(min);
        mmGraphView.getViewport().setMaxX(max);
    }

    public void updateGraph(){
        if(!mmPostureManager.isDBopen()) {
            mmPostureManager.openDB();
            mmDatapoints = mmPostureManager.get(Calendar.getInstance());
            mmPostureManager.closeDB();
        } else {
            mmDatapoints = mmPostureManager.get(Calendar.getInstance());
        }

        if(!mmDatapoints.isEmpty()) {
            modifyGraphData(mmDatapoints);
        } else {
            mmGraphView.removeAllSeries();
            mmGraphView.getGridLabelRenderer().reloadStyles();
        }
    }

    private void modifyGraphData(ArrayList<DataPoint> points){
        mmGraphView.removeAllSeries();

        PointsGraphSeries<DataPoint> series = new PointsGraphSeries<>();
        Calendar c = Calendar.getInstance();
        for(DataPoint dp : points){
            c.setTime(new Date((long) dp.getX()));
            Log.d("modifyGarphData", "Time: " + c.getTime());
            series.appendData(new DataPoint(timeInHours(c), dp.getY()), false, 80000);
        }

        series.setColor(Color.BLUE);
        c.setTime(new Date((long) points.get(0).getX())); //use Calendar c as start time.
        setHorizontalLabels(c , Calendar.getInstance());
        mmGraphView.addSeries(series);

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

        mmGraphView.getGridLabelRenderer().setNumHorizontalLabels(numOfHorLabels+1);

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
            Log.d("labelFormatter", "i: " + i + " label: " + lableFormatter.get(i) + " size:" + timeFrame.size());
        }

        StaticLabelsFormatter slf = new StaticLabelsFormatter(mmGraphView);
        slf.setHorizontalLabels(timeFrame.toArray(new String[numOfHorLabels]));
        mmGraphView.getGridLabelRenderer().setLabelFormatter(slf);
        mmGraphView.getGridLabelRenderer().setTextSize(40f);
        mmGraphView.getGridLabelRenderer().reloadStyles();
    }

    /*******************************/
    /********onClick methods********/
    private void deleteUserRecords(){
        mmPostureManager.openDB();
        mmPostureManager.delete(GoogleAccountInfo.getInstance().getId());
        mmPostureManager.closeDB();
    }

    private void addRecord() {
        mmPostureManager.openDB();
        DecimalFormat df = new DecimalFormat("#.00");
        float value = Float.parseFloat(df.format((float)(-(Math.random() * (5 - 3) + 3))));
        mmPostureManager.insert(value);
        mmPostureManager.closeDB();
    }

    private void populateSQLite(){
        if(!mmPostureManager.isDBopen()) {
            mmPostureManager.fakeIt();
        }
    }
    /********onClick methods********/
    /*******************************/

    /********************************/
    /********Observer Methods********/
    public void startObserving(){
        mmPostureManager.addObserver(this);
        Log.d("GraphManager", "Observing PostureManager");
    }

    public void stopObserving(){
        mmPostureManager.deleteObserver(this);
        Log.d("GraphManager", "Stopped observing PostureManager");
    }

    @Override
    public void update(Observable observable, Object o) {
        Log.d("GraphManager", "Notified");
        updateGraph();
    }
    /********Observer Methods********/
    /********************************/
}
