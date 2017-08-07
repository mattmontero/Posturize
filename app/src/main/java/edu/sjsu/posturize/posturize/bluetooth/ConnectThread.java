package edu.sjsu.posturize.posturize.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by matthewmontero on 8/6/17.
 */

public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    public ConnectThread(BluetoothDevice device) {
        BluetoothSocket tmp = null;
        try{
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            Log.d("UUID", MY_UUID.toString());
            Log.d("socket", tmp.toString());
        } catch(IOException e){
            Log.d("tmp Socket", e.toString());
        }
        mmSocket = tmp;
    }
}
