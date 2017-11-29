package edu.sjsu.posturize.posturize;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import edu.sjsu.posturize.posturize.sidenav.SideNavDrawer;
import edu.sjsu.posturize.posturize.users.GoogleAccountInfo;
import edu.sjsu.posturize.posturize.visualizations.GraphManager;

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
    //GraphManager gm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        SideNavDrawer.create(this); //Add SideNavDrawer to activity

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
                    return true;
                case R.id.navigation_weekly:
                    ((Button) findViewById(R.id.picDate)).setText(getString(R.string.selectWeekly));
                    return true;
                case R.id.navigation_monthly:
                    ((Button) findViewById(R.id.picDate)).setText(getString(R.string.selectMonthly));
                    return true;
            }
            return false;
        }
    };

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
}
