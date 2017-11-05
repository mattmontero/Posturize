package edu.sjsu.posturize.posturize;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import edu.sjsu.posturize.posturize.users.PosturizeUserInfo;

public class HomeActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener,
                    View.OnClickListener{

    //DATE PICKER THINGS
    DatePicker datePicker;
    TextView displayDate;
    Button changeDate;
    int month;

    TextView mTextMessage;
    String tab;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            mTextMessage = (TextView)findViewById(R.id.test);
            System.out.println(item.getItemId());
            System.out.println(R.id.navigation_daily);
            System.out.println(R.id.navigation_weekly);
            System.out.println(R.id.navigation_monthly);

            switch (item.getItemId()) {
                case R.id.navigation_daily:
                    ((Button) findViewById(R.id.picDate)).setText(getString(R.string.selectDaily));
                    mTextMessage.setText(getString(R.string.selectDaily));
                    setDataView(100);
                    return true;
                case R.id.navigation_weekly:
                    ((Button) findViewById(R.id.picDate)).setText(getString(R.string.selectWeekly));
                    mTextMessage.setText((R.string.selectWeekly));
                    setDataView(500);
                    return true;
                case R.id.navigation_monthly:
                    ((Button) findViewById(R.id.picDate)).setText(getString(R.string.selectMonthly));
                    mTextMessage.setText((R.string.selectMonthly));
                    setDataView(1000);
                    return true;
            }
            return false;
        }
    };

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
        graph.getViewport().setMinY(-5);
        graph.getViewport().setMaxY(0);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(4);
        graph.getViewport().setMaxX(80);

        // enable scaling and scrolling
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(false);

        graph.addSeries(series);

        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(getApplicationContext(), "Series1: On Data Point clicked: "+dataPoint, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViewById(R.id.settings_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.test_view).setOnClickListener(this);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_daily);
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
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.settings_button:
                startActivity((new Intent(this, SettingsActivity.class)));
                break;
            case R.id.sign_out_button:
                PosturizeUserInfo.getInstance().signOut();
                finish();
                break;
            case R.id.test_view:
                startActivity((new Intent(this, TestActivity.class)));
                break;
        }
    }

    @Override
    public void onBackPressed(){
        PosturizeUserInfo.getInstance().signOut();
        finish();
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
}