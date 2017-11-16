package edu.sjsu.posturize.posturize.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Set;

import edu.sjsu.posturize.posturize.R;
import edu.sjsu.posturize.posturize.bluetooth.BluetoothActivity;

/**
 * Created by markbragg on 11/8/17.
 */

public class BluetoothActivity extends AppCompatActivity
implements View.OnClickListener {

    private Button mConnectButton;
    private TextView mConnectionStatusTextView;
    private BluetoothConnection mBluetoothConnection;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_bluetooth);
        setViews();
        mBluetoothConnection = BluetoothConnection.getInstance();
        mBluetoothConnection.setActivity(this);
    }

    @Override
    protected  void onStart(){
        super.onStart();
        updateUI();
    }

    private void setViews(){
        mConnectionStatusTextView = (TextView)findViewById(R.id.connectionStatus);
        mConnectButton = (Button) findViewById(R.id.connectButton);
        mConnectButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.connectButton)
            connectButtonPressed();
    }

    private void connectButtonPressed() {
        if(mBluetoothConnection.isConnected()){
            mBluetoothConnection.kill(false);
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

    public void updateUI(){
        mConnectButton.setEnabled(true);
        if(mBluetoothConnection.isConnected()){
            mConnectButton.setText("Disconnect");
            mConnectionStatusTextView.setText("Connected!");
        } else {
            mConnectButton.setText("Connect");
        }
    }
}
