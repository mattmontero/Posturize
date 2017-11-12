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
        setmConnectButton();
        mConnectionStatusTextView = (TextView)findViewById(R.id.connectionStatus);
        mBluetoothConnection = BluetoothConnection.getInstance();
        mBluetoothConnection.setActivity(this);
    }

    private void setmConnectButton(){
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
            mBluetoothConnection.kill();
        } else {
            mConnectionStatusTextView.setText("Connecting...");
            if(connectBLE()){
                mConnectionStatusTextView.setText("Connected!");
            } else {
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

        //3. Get the Bluetooth module device
        Set<BluetoothDevice> pairedDevices = mBluetoothConnection.getBluetoothAdapter().getBondedDevices();
        //mDevice should end up being HC-06
        BluetoothDevice mDevice = null;
        if(pairedDevices.size() > 0){
            for(BluetoothDevice device : pairedDevices) {
                if(device.getName().equals("HC-06")){
                    //This is our bluetooth device.
                    mDevice = device;
                    Log.d(BLUETOOTH, device.getName());
                    Log.d(BLUETOOTH, device.toString());
                    break;
                }
            }
        }
        if(mDevice == null){
            Log.d(BLUETOOTH, "No device found");
            mConnectButton.setText("Connect");
            updateUI();
            return false;
        }
        Log.d(BLUETOOTH, mDevice.getName());
        Log.d(BLUETOOTH, mDevice.toString());

        //4. Create the connection thread
        mBluetoothConnection.connectThread(mDevice);
        Log.d("ConnectThread", "created");
        mBluetoothConnection.startConnectThread();
        Log.d("ConnectThread", "Running...");
        mConnectButton.setText("Disconnect"); //TODO: move to onConnected method when Bt replies with confirmtion
        updateUI();
        return true;
    }

    public void updateUI(){
        if(mBluetoothConnection.isConnected()){
            mConnectButton.setText("Disconnect");
        } else {
            mConnectButton.setText("Connect");
        }
    }
}
