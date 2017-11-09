package edu.sjsu.posturize.posturize.bluetooth;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import edu.sjsu.posturize.posturize.R;

/**
 * Created by markbragg on 11/8/17.
 */

public class CalibrateActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView mCalibrationStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_calibrate);
        mCalibrationStatusTextView = (TextView)findViewById(R.id.calibrationStatus);;
        findViewById(R.id.calibrateButton).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.calibrateButton)
            calibrate();
    }

    private void calibrate(){
        mCalibrationStatusTextView.setText("Calibrating...");
        BluetoothConnection.getInstance().write("*");
    }
}
