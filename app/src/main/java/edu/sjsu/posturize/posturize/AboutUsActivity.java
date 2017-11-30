package edu.sjsu.posturize.posturize;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import edu.sjsu.posturize.posturize.sidenav.SideNavDrawer;

/**
 * Created by markbragg on 11/29/17.
 */

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
        SideNavDrawer.create(this); //Add SideNavDrawer to activity
    }
}
