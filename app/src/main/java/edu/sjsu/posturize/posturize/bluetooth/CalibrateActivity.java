package edu.sjsu.posturize.posturize.bluetooth;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import edu.sjsu.posturize.posturize.R;

/**
 * Created by markbragg on 11/8/17.
 */

public class CalibrateActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView mCalibrationStatusTextView;
    private Button mCalibrateButton;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_calibrate);
        setViews();
        BluetoothConnection.getInstance().setActivity(this);
    }

    @Override
    protected void onStart(){
        super.onStart();
        if(BluetoothConnection.getInstance().isConnected()){
            mCalibrateButton.setEnabled(true);
        } else {
            mCalibrateButton.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.calibrateButton)
            calibrate();
    }

    private void setViews(){
        mCalibrationStatusTextView = (TextView)findViewById(R.id.calibrationStatus);
        mCalibrateButton = (Button)findViewById(R.id.calibrateButton);
        mCalibrateButton.setOnClickListener(this);
    }

    private void calibrate(){
        mCalibrationStatusTextView.setText("Calibrating...");
        BluetoothConnection.getInstance().write("*");
    }

    public void updateUI(){
        mCalibrationStatusTextView.setText("Calibrated!");
    }
}
