package edu.sjsu.posturize.posturize;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.Series;

import edu.sjsu.posturize.posturize.data.PostureMeasurement;
import edu.sjsu.posturize.posturize.data.localdb.PostureManager;
import edu.sjsu.posturize.posturize.sidenav.SideNavDrawer;
import edu.sjsu.posturize.posturize.users.GoogleAccountInfo;

public class HomeActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener//,
                    //NavigationView.OnNavigationItemSelectedListener
        {

    //DATE PICKER THINGS
    DatePicker datePicker;
    TextView displayDate;
    Button changeDate;
    int month;

    String tab;
    private static PostureManager tempPm; //TODO: Remove Temp
    ArrayList<DataPoint> points; //TODO: Remove Temp

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        SideNavDrawer.create(this); //Add SideNavDrawer to activity

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_daily);


        //GraphManager gm = new GraphManager(this);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            System.out.println(item.getItemId());
            System.out.println(R.id.navigation_daily);
            System.out.println(R.id.navigation_weekly);
            System.out.println(R.id.navigation_monthly);

            switch (item.getItemId()) {
                case R.id.navigation_daily:
                    ((Button) findViewById(R.id.picDate)).setText(getString(R.string.selectDaily));
                    setDataView(100);
                    return true;
                case R.id.navigation_weekly:
                    ((Button) findViewById(R.id.picDate)).setText(getString(R.string.selectWeekly));
                    setDataView(500);
                    return true;
                case R.id.navigation_monthly:
                    ((Button) findViewById(R.id.picDate)).setText(getString(R.string.selectMonthly));
                    setDataView(1000);
                    return true;
            }
            return false;
        }
    };

    //FIXME: JAVADOCS
    /**
     *
     * @param numPoints
     */
    private void setDataView(int numPoints){

        //DATA GRAPH THINGS
        GraphView graph = (GraphView) findViewById(R.id.graph);
        DataPoint[] points = new DataPoint[numPoints];
        for (int i = 0; i < points.length; i++) {
            points[i] = new DataPoint(i, -1 * Math.abs(5 *(Math.random())));
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(points);
        graph.removeAllSeries();
        // set manual X bounds
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-15); //percent off
        graph.getViewport().setMaxY(0); //on track

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(4);
        graph.getViewport().setMaxX(numPoints);

        // enable scaling and scrolling
        graph.getViewport().setScalable(false);
        graph.getViewport().setScrollable(false);

        graph.addSeries(series);

        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(getApplicationContext(), "Series1: On Data Point clicked: "+dataPoint, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This callback method, call DatePickerFragment class,
     * DatePickerFragment class returns calendar view.
     * @param view
     */
    public void datePicker(View view){
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.show(getSupportFragmentManager(), "date");
    }

    /**
     * To set date on TextView
     * @param calendar
     */
    private void setDate(final Calendar calendar) {
        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

        ((TextView) findViewById(R.id.showDate))
                .setText(dateFormat.format(calendar.getTime()));

    }

    /**
     * To receive a callback when the user sets the date.
     * @param view
     * @param year
     * @param month
     * @param day
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar cal = new GregorianCalendar(year, month, day);
        setDate(cal);
    }

    @Override
    public void onBackPressed(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else {
            GoogleAccountInfo.getInstance().signOut();
            finish();
        }
    }

    /**
     * Create a DatePickerFragment class that extends DialogFragment.
     * Define the onCreateDialog() method to return an instance of DatePickerDialog
     */
    public static class DatePickerFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(),
                    (DatePickerDialog.OnDateSetListener)
                            getActivity(), year, month, day);
        }
    }

    public static class GraphManager{

        private PostureManager mmPostureManager;
        private ArrayList<DataPoint> mmDatapoints;
        private Activity mmActivity;
        private GraphView mmGraphView;

        GraphManager(Activity activity){
            mmActivity = activity;
            mmPostureManager = new PostureManager(activity.getApplicationContext());
            mmDatapoints = new ArrayList<>();
            constructGraph();

            View.OnClickListener temp = new View.OnClickListener() {
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
            mmActivity.findViewById(R.id.add_point).setOnClickListener(temp);
            mmActivity.findViewById(R.id.delete_points).setOnClickListener(temp);
        }

        private void constructGraph(){
            mmGraphView = (GraphView) mmActivity.findViewById(R.id.graph);

            //Manual Bounds
            mmGraphView.getViewport().setXAxisBoundsManual(true);
            mmGraphView.getViewport().setYAxisBoundsManual(true);

            // Disable scaling and scrolling
            mmGraphView.getViewport().setScalable(false);
            mmGraphView.getViewport().setScrollable(false);

            //Set Y min and max
            mmGraphView.getViewport().setMinY(-10); //percent off
            mmGraphView.getViewport().setMaxY(0);   //on track
            //Set X min and max
            viewportX(0,10); //Just some default values to construct
        }

        private void viewportX(int min, int max){
            mmGraphView.getViewport().setMinX(min);
            mmGraphView.getViewport().setMaxX(max);
        }

        public void addPoint(DataPoint dp){
            mmDatapoints.add(dp);
            updateGraph();
        }

        public void updateGraph(){
            mmPostureManager.openDB();
            ArrayList<DataPoint> dp = new ArrayList<>();
            ArrayList<PostureMeasurement> postureMeasurements = mmPostureManager.get(Calendar.getInstance());
            Calendar c = new GregorianCalendar();
            for(PostureMeasurement pm : postureMeasurements){
                c.setTime(new Date(pm.timestamp));
                dp.add(new DataPoint((double) timeInSeconds(c), -pm.distance));
            }
            mmPostureManager.closeDB();
            c.setTime(new Date( postureMeasurements.get(0).timestamp));
            modifyData(c, dp);
        }

        public void modifyData(Calendar start, ArrayList<DataPoint> points){
            /*
                remove series from graph
                Add newest datapoint
                reset viewportX
                add new series
             */
            mmGraphView.removeAllSeries();
            PointsGraphSeries<DataPoint> series = new PointsGraphSeries<>(points.toArray(new DataPoint[points.size()]));
            mmGraphView.addSeries(series);
        }

        private long timeInSeconds(Calendar cal){
            Calendar c = (Calendar) cal.clone();
            long now = c.getTimeInMillis();
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            long passed = now - c.getTimeInMillis();
            long secondsPassed = (int)passed / 1000;
            return secondsPassed;
        }

        private int timeInMinutes(Calendar c){
            long now = c.getTimeInMillis();
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            long passed = now - c.getTimeInMillis();
            int minutesPassed = (int)passed / (1000*60);
            return minutesPassed;
        }

        private void deleteUserRecords(){
            mmPostureManager.openDB();
            mmPostureManager.delete(GoogleAccountInfo.getInstance().getId());
            mmPostureManager.closeDB();
        }

        private  void addRecord() {
            mmPostureManager.openDB();
            DecimalFormat df = new DecimalFormat("#.00");
            float value = Float.parseFloat(df.format((float)(Math.random() * (5 - 3) + 3)));
            mmPostureManager.insert(value);
            mmPostureManager.closeDB();
            updateGraph();
        }
    }
}
