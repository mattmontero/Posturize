package edu.sjsu.posturize.posturize.sidenav;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import edu.sjsu.posturize.posturize.AboutUsActivity;
import edu.sjsu.posturize.posturize.instructions.InstructionsActivity;
import edu.sjsu.posturize.posturize.PostureManagerActivity;
import edu.sjsu.posturize.posturize.PreferencesActivity;
import edu.sjsu.posturize.posturize.R;
import edu.sjsu.posturize.posturize.sidenav.sidenavmodals.BluetoothSideNavModal;
import edu.sjsu.posturize.posturize.sidenav.sidenavmodals.CalibrateSideNavModal;
import edu.sjsu.posturize.posturize.users.GoogleAccountInfo;

/**
 * Created by Matt on 11/18/2017.
 * Side Navigation Drawer contains all the sideNav Modals
 */

public class SideNavDrawer
    implements NavigationView.OnNavigationItemSelectedListener{
    private static SideNavDrawer sideNavDrawer;
    private static Activity activity;

    private SideNavDrawer(Activity activity){
        this.activity = activity;
    }

    /**
     * Adds a SideNavDrawer to activity. The activity contentView should contain a NavigationView
     * @param activity - activity to add side nav drawer
     */
    public static void create(Activity activity){
        sideNavDrawer = new SideNavDrawer(activity);
        DrawerLayout drawer = (DrawerLayout) (activity.findViewById(R.id.drawer_layout));
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        Log.d("Toggle", toggle.toString());
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) (activity.findViewById(R.id.nav_view));
        navigationView.setNavigationItemSelectedListener(sideNavDrawer);
        navigationView.setItemBackgroundResource(R.drawable.item_background);
        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_main);

        //Set header
        ((TextView) headerLayout.findViewById(R.id.side_nav_email)).setText(GoogleAccountInfo.getInstance().getEmail());
        ((TextView) headerLayout.findViewById(R.id.side_nav_username))
                .setText(GoogleAccountInfo.getInstance().getFirstName() + " " + GoogleAccountInfo.getInstance().getLastName());
        ((ImageView) headerLayout.findViewById(R.id.imageView)).setImageResource(R.mipmap.app_icon);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        /*if (id == R.id.nav_settings_button) {
            activity.startActivity((new Intent(activity, PostureManagerActivity.class)));
        } else */
        if (id == R.id.nav_preferences_button_button) {
            activity.startActivity((new Intent(activity, PreferencesActivity.class)));
        } else if (id == R.id.nav_calibration_button) {
            CalibrateSideNavModal.newInstance().show(activity.getFragmentManager(), "CalibrationModal");
        } else if (id == R.id.nav_bluetooth_button) {
            BluetoothSideNavModal.newInstance().show(activity.getFragmentManager(), "BluetoothModal");
        } else if (id == R.id.nav_sign_out_button) {
            GoogleAccountInfo.getInstance().signOut();
            activity.finish();
        } else if (id == R.id.nav_about_us) {
            activity.startActivity((new Intent(activity, AboutUsActivity.class)));
        } else if (id == R.id.nav_instructions) {
            activity.startActivity((new Intent(activity, InstructionsActivity.class)));
        }


//        <item
//        android:id="@+id/nav_about_us"
//        android:icon="@drawable/ic_home_black_24dp"
//        android:title="Sign Out" />
//        <item
//        android:id="@+id/nav_instructions"
//        android:icon="@drawable/ic_home_black_24dp"
//        android:title="Sign Out" />

        DrawerLayout drawer = (DrawerLayout) (activity.findViewById(R.id.drawer_layout));
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
