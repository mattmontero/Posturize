package edu.sjsu.posturize.posturize.sidenav.sidenavmodals;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

import edu.sjsu.posturize.posturize.R;
import edu.sjsu.posturize.posturize.bluetooth.BluetoothConnection;
import edu.sjsu.posturize.posturize.bluetooth.WearableState;

/**
 * Created by Matt on 11/18/2017.
 */

public class CalibrateSideNavModal extends DialogFragment
    implements Observer{

    private static BluetoothConnection mBluetoothConnection;
    private Button mCalibrateButton;
    private Button mCancelButton;
    private TextView mCalibrationStatusTextView;

    public static CalibrateSideNavModal newInstance(){
        CalibrateSideNavModal calibrateModal = new CalibrateSideNavModal();
        Bundle args = new Bundle();
        mBluetoothConnection = BluetoothConnection.getInstance();
        calibrateModal.setArguments(args);
        return calibrateModal;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        WearableState.getInstance().addObserver(this);

        AlertDialog.Builder builder;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme);
        } else {
            builder = new AlertDialog.Builder(getActivity());
        }
        builder.setView(R.layout.alert_dialog_calibrate)
                //Setting onClickListeners to null. Handling it in setOnShowListener
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Calibrate",null)
                .setNeutralButton("Close", null);
        final Dialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            final String TAG = "CalibrateModal";
            @Override
            public void onShow(DialogInterface dialogInterface) {
                final Button close = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);
                setUI(dialog);

                if(WearableState.getInstance().isConnected()){
                    mCalibrateButton.setEnabled(true);
                    mCancelButton.setEnabled(true);
                } else {
                    mCalibrateButton.setEnabled(false);
                    mCancelButton.setEnabled(false);
                }

                View.OnClickListener btModalOnClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (view == mCalibrateButton){
                            Log.d(TAG, "Calibrate Pressed");
                            calibrate();
                        } else if (view == mCancelButton){
                            Log.d(TAG, "Cancel Pressed");
                        } else if (view == close){
                            Log.d(TAG, "Close Pressed");
                            dialog.dismiss();
                        }
                    }
                };
                mCalibrateButton.setOnClickListener(btModalOnClickListener);
                mCancelButton.setOnClickListener(btModalOnClickListener);
                close.setOnClickListener(btModalOnClickListener);
            }
        });
        ModalWindowManager.format(dialog);
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface){
        WearableState.getInstance().deleteObserver(this);
        super.dismiss();
    }

    private void calibrate(){
        mCalibrationStatusTextView.setText("Calibrating...");
        BluetoothConnection.getInstance().write("*");
    }

    private void setUI(Dialog dialog){
        mCalibrateButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
        mCancelButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
        mCalibrationStatusTextView = (TextView)(dialog).findViewById(R.id.bt_calibration_status_modal);

        if(WearableState.getInstance().isCalibrated()){
            mCalibrationStatusTextView.setText(R.string.calibrated);
        } else {
            mCalibrationStatusTextView.setText(R.string.not_calibrated);
        }
    }

    @Override
    public void update(Observable observable, Object o) {
        switch (o.toString()){
            case WearableState.CALIBRATED:
                mCalibrationStatusTextView.setText(R.string.calibrated);
                break;
            case WearableState.NOT_CALIBRATED:
                mCalibrationStatusTextView.setText(R.string.not_calibrated);
                break;
        }
    }
}
