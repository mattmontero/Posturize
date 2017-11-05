package edu.sjsu.posturize.posturize;

import edu.sjsu.posturize.posturize.bluetooth.*;
import edu.sjsu.posturize.posturize.data.localdb.PostureManager;
import edu.sjsu.posturize.posturize.users.PosturizeUserInfo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity
        implements View.OnClickListener{

    private  BluetoothConnection mBluetoothConnection;
    private SharedPreferences sharedPreferences;
    private TextView mTextView;
    private Button mConnectButton;
    private PostureManager tempPm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreate: Starting");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setViewsAndListeners();

        mBluetoothConnection = BluetoothConnection.getInstance();
        mBluetoothConnection.setTextView(mTextView);

        tempPm = new PostureManager(this.getApplicationContext());

        updateUI();
    }

    private void updateUI(){
        if(mBluetoothConnection.isConnected()){
            findViewById(R.id.calibrateButton).setEnabled(true);
            mConnectButton.setText("Disconnect");
        } else {
            findViewById(R.id.calibrateButton).setEnabled(false);
            mConnectButton.setText("Connect");
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

    private void setViewsAndListeners(){
        this.setTitle(getString(R.string.signed_in_greeting, PosturizeUserInfo.getInstance().getFirstName()));
        mTextView = (TextView)findViewById(R.id.numberViewer);
        mConnectButton = (Button) findViewById(R.id.connectButton);
        mConnectButton.setOnClickListener(this);
        findViewById(R.id.calibrateButton).setOnClickListener(this);
        findViewById(R.id.add_record).setOnClickListener(this);
        findViewById(R.id.delete_records).setOnClickListener(this);
        findViewById(R.id.display_records).setOnClickListener(this);
    }

    private void calibrate(){
        mTextView.setText("Calibrating...");
        mBluetoothConnection.write("*");
    }

    private void connectButtonPressed() {
        if(mBluetoothConnection.isConnected()){
            mBluetoothConnection.kill();
            updateUI();
            mTextView.setText("Disconnected");
            ((Button)findViewById(R.id.connectButton)).setText("Connect");
        } else {
            mTextView.setText("Connecting...");
            if(connectBLE()){
                mTextView.setText("Connected!");
                ((Button)findViewById(R.id.connectButton)).setText("Disconnect");//TODO: Move to onConnect method when Bt replies with confrimation
            } else {
                mTextView.setText("Something bad happened.");
            }
        }
    }

    @Override
    public void onClick(View view) {
        Log.d("onClick", view.toString());
        switch (view.getId()){
            case R.id.calibrateButton:
                calibrate();
                break;
            case R.id.connectButton:
                connectButtonPressed();
                break;
            //TODO: temp buttons
            case R.id.add_record:
                addRecord();
                break;
            case R.id.delete_records:
                deleteUserRecords();
                break;
            case R.id.display_records:
                displayRecords();
                break;
            //TODO: End temp buttons
            default:
                break;
        }
    }

    private void displayRecords(){
        tempPm.openDB();
        ((TextView) findViewById(R.id.textDisplay)).setText(tempPm.get(Calendar.getInstance()).toString());
        tempPm.closeDB();
    }

    private void deleteUserRecords(){
        tempPm.openDB();
        tempPm.delete(PosturizeUserInfo.getInstance().getEmail());
        tempPm.closeDB();
        displayRecords();
    }

    private  void addRecord() {
        tempPm.openDB();
        tempPm.insert((float)(Math.random() * (70 - 65) + 65));
        ((TextView) findViewById(R.id.textDisplay)).setText(tempPm.get(Calendar.getInstance()).toString());
        tempPm.closeDB();
    }
}
