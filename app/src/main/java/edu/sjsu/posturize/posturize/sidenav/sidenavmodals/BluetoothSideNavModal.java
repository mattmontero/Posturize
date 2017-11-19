package edu.sjsu.posturize.posturize.sidenav.sidenavmodals;

import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
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
 * Created by Matt on 11/17/2017.
 */

public class BluetoothSideNavModal extends DialogFragment
    implements Observer{

    private static BluetoothConnection mBluetoothConnection;
    private Button mConnectButton;
    private Button mCancelButton;
    private TextView mConnectionStatusTextView;

    public static BluetoothSideNavModal newInstance(){
        BluetoothSideNavModal btModal = new BluetoothSideNavModal();
        Bundle args = new Bundle();
        mBluetoothConnection = BluetoothConnection.getInstance();
        btModal.setArguments(args);
        return btModal;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme);
        } else {
            builder = new AlertDialog.Builder(getActivity());
        }
        builder.setView(R.layout.alert_dialog_bluetooth)
                //Setting onClickListeners to null. Handling it in setOnShowListener
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Connect",null)
                .setNeutralButton("Close", null);
        final Dialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            final String TAG = "BluetoothModal";
            @Override
            public void onShow(DialogInterface dialogInterface) {
                final Button close = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);
                setUI(dialog);

                View.OnClickListener btModalOnClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (view == mConnectButton){
                            connectButtonPressed();
                            Log.d(TAG, "Connect Pressed");
                        } else if (view == mCancelButton){
                            Log.d(TAG, "Cancel Pressed");
                        } else if (view == close){
                            Log.d(TAG, "Close Pressed");
                            dialog.dismiss();
                        }
                    }
                };
                mConnectButton.setOnClickListener(btModalOnClickListener);
                mCancelButton.setOnClickListener(btModalOnClickListener);
                close.setOnClickListener(btModalOnClickListener);
            }
        });
        ModalWindowManager.format(dialog);
        WearableState.getInstance().addObserver(this);
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface){
        WearableState.getInstance().deleteObserver(this);
        super.dismiss();
    }

    private void setUI(Dialog dialog){
        mConnectButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
        mCancelButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
        mConnectionStatusTextView = (TextView)(dialog).findViewById(R.id.bt_connection_status_modal);

        if(WearableState.getInstance().isConnected()){
            mConnectButton.setText(R.string.disconnect);
            mConnectionStatusTextView.setText(R.string.connected);
        } else {
            mConnectButton.setText(R.string.connect);
        }
    }

    private void connectButtonPressed() {
        if(WearableState.getInstance().isConnected()){
            mBluetoothConnection.kill();
        } else {
            mConnectionStatusTextView.setText("Connecting...");
            if(!connectBLE()){
                mConnectionStatusTextView.setText("Something bad happened.");
            }
        }
    }

    private boolean connectBLE(){
        final String BLUETOOTH = "Bluetooth_Setup";
        //1. Check if device has bluetooth.
        if(mBluetoothConnection.getBluetoothAdapter() == null){
            //Device does not support Bluetooth.
            Log.d(BLUETOOTH, "Bluetooth is not supported");
            return false;
        } else {
            Log.d(BLUETOOTH, "Bluetooth is supported");
        }
        //2. Check if bluetooth is enabled
        if(!mBluetoothConnection.getBluetoothAdapter().isEnabled()){
            Log.d(BLUETOOTH, "Bluetooth is not enabled");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 6969);
        } else {
            Log.d(BLUETOOTH, "Bluetooth is enabled");
        }
        if(mBluetoothConnection.connect()){
            mConnectButton.setEnabled(false);
            return true;
        }
        return false;
    }

    @Override
    public void update(Observable observable, Object o) {
        switch (o.toString()){
            case WearableState.CONNECTED:
                mConnectButton.setEnabled(true);
                mConnectionStatusTextView.setText(R.string.connected);
                mConnectButton.setText(R.string.disconnect);
                break;
            case WearableState.DISCONNECTED:
                mConnectButton.setEnabled(true);
                mConnectionStatusTextView.setText(R.string.disconnected);
                mConnectButton.setText(R.string.connect);
                break;
        }
    }
}
