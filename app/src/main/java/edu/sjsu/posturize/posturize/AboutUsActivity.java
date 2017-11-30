package edu.sjsu.posturize.posturize;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import edu.sjsu.posturize.posturize.sidenav.SideNavDrawer;

/**
 * Created by markbragg on 11/29/17.
 */

public class AboutUsActivity extends AppCompatActivity {

    private TextView aboutUsTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        aboutUsTextView = ((TextView)findViewById(R.id.about_us_text_view));
        aboutUsTextView.setTextColor(Color.BLACK);
        aboutUsTextView.setTextSize(24);
    }
}
