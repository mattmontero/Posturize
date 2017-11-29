package edu.sjsu.posturize.posturize;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import com.google.common.collect.ImmutableMap;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.Series;

import edu.sjsu.posturize.posturize.bluetooth.BluetoothConnection;
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
    GraphManager gm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        SideNavDrawer.create(this); //Add SideNavDrawer to activity

        gm = new GraphManager(this);
        //TODO: Remove?
        //BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        //navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //navigation.setSelectedItemId(R.id.navigation_daily);
    }

    @Override
    protected void onStart(){
        super.onStart();
        gm.startObserving();
        gm.updateGraph();
    }

    @Override
    protected void onPause(){
        super.onPause();
        gm.stopObserving();
    }

    //TODO: Remove?
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
                    //gm.updateGraph();
                    //setDataView(100);
                    return true;
                case R.id.navigation_weekly:
                    ((Button) findViewById(R.id.picDate)).setText(getString(R.string.selectWeekly));
                    //setDataView(500);
                    return true;
                case R.id.navigation_monthly:
                    ((Button) findViewById(R.id.picDate)).setText(getString(R.string.selectMonthly));
                    //setDataView(1000);
                    return true;
            }
            return false;
        }
    };

    //TODO: Remove?
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

    //TODO: Remove? We only have Daily view for now.
    /**
     * This callback method, call DatePickerFragment class,
     * DatePickerFragment class returns calendar view.
     * @param view
     */
    public void datePicker(View view){
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.show(getSupportFragmentManager(), "date");
    }

    //TODO: Remove? We only have Daily view for now.
    /**
     * To set date on TextView
     * @param calendar
     */
    private void setDate(final Calendar calendar) {
        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

        ((TextView) findViewById(R.id.showDate))
                .setText(dateFormat.format(calendar.getTime()));

    }

    //TODO: Remove?
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

    //TODO: Add JAVADOCS
    public static class GraphManager implements Observer{

        private PostureManager mmPostureManager;
        private ArrayList<DataPoint> mmDatapoints;
        private Activity mmActivity;
        private GraphView mmGraphView;

        private static final Map<Integer, String> lableFormatter = ImmutableMap.<Integer, String>builder()
                .put(0, "12\nAM").put(1, "1").put(2, "2").put(3, "3").put(4, "4").put(5, "5").put(6, "6").put(7, "7")
                .put(8, "8").put(9, "9").put(10, "10").put(11, "11").put(12, "12\nPM").put(13, "1").put(14, "2").put(15, "3")
                .put(16, "4").put(17, "5").put(18, "6").put(19, "7").put(20, "8").put(21, "9").put(22, "10").put(23, "11")
                .build();

        GraphManager(Activity activity){
            mmActivity = activity;
            mmPostureManager = BluetoothConnection.getInstance().getPostureManager();
            mmPostureManager.openDB();
            mmDatapoints = mmPostureManager.get(Calendar.getInstance());
            mmPostureManager.closeDB();
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
                        case R.id.fake_data_button:
                            populateSQLite();
                            break;
                    }
                }
            };
            mmActivity.findViewById(R.id.add_point).setOnClickListener(temp);
            mmActivity.findViewById(R.id.delete_points).setOnClickListener(temp);
            mmActivity.findViewById(R.id.fake_data_button).setOnClickListener(temp);
        }

        private void constructGraph(){
            mmGraphView = (GraphView) mmActivity.findViewById(R.id.graph);

            //Manual Bounds
            mmGraphView.getViewport().setXAxisBoundsManual(true);
            mmGraphView.getViewport().setYAxisBoundsManual(true);

            //Set Y min and max
            mmGraphView.getViewport().setMinY(-40); //percent off
            mmGraphView.getViewport().setMaxY(1);   //on track
            //Set X min and max
            viewportX(0,1000); //Just some default values to construct
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

                    Toast.makeText(mmActivity.getApplicationContext(), "DataPoint info: " + time, Toast.LENGTH_SHORT).show();
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
                //updateGraph();
            }
        }
        /********onClick methods********/
        /*******************************/

        /********************************/
        /********Observer Methods********/
        private void startObserving(){
            mmPostureManager.addObserver(this);
            Log.d("GraphManager", "Observing PostureManager");
        }

        private void stopObserving(){
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
}
