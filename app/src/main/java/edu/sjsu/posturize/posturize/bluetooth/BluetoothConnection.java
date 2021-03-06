package edu.sjsu.posturize.posturize.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import edu.sjsu.posturize.posturize.SignInActivity;
import edu.sjsu.posturize.posturize.data.localdb.PostureManager;
import edu.sjsu.posturize.posturize.sidenav.sidenavmodals.BluetoothSideNavModal;
import edu.sjsu.posturize.posturize.sidenav.sidenavmodals.CalibrateSideNavModal;
import edu.sjsu.posturize.posturize.users.GoogleAccountInfo;

/**
 * Created by matthewmontero on 8/6/17.
 * BluetoothConnection manages the communication between the wearable and phone.
 */

public class BluetoothConnection {
    private static BluetoothConnection singleBluetoothConnection;

    private BluetoothAdapter mBluetoothAdapter;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private PostureManager mPostureManager;
    private boolean isKilling;

    private static final String BLUETOOTH = "Connection Setup";
    private final String CONNECT_CHAR = "c";

    private BluetoothConnection(){
        mPostureManager = new PostureManager(SignInActivity.getAppContext());
        isKilling = false;
    }

    public static BluetoothConnection getInstance(){
        if (singleBluetoothConnection == null){
            singleBluetoothConnection = new BluetoothConnection();
        }
        return singleBluetoothConnection;
    }

    /**
     * @return PostureManager instance
     */
    public PostureManager getPostureManager(){
        return mPostureManager;
    }

    /**
     * @param btAdapter sets mBluetoothAdapter
     */
    public void setBluetoothAdapter(BluetoothAdapter btAdapter){
        mBluetoothAdapter = btAdapter;
    }

    /**
     * @return BluetoothAdapter instance
     */
    public BluetoothAdapter getBluetoothAdapter(){
        return mBluetoothAdapter;
    }

    /**
     * Attemps to connect to bluetooth device, HC-06
     * Device should already be paired.
     * @return true if connection is successful
     *         false if connection fails
     */
    public boolean connect(){
        //3. Get the Bluetooth module device
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
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
            WearableState.getInstance().setIsConnected(false);
            return false;
        }
        Log.d(BLUETOOTH, mDevice.getName());
        Log.d(BLUETOOTH, mDevice.toString());

        //4. Create the connection thread
        mConnectThread = new ConnectThread(mDevice);
        Log.d(BLUETOOTH, "ConnectThread: Created: ");
        mConnectThread.start();
        Log.d(BLUETOOTH, "ConnectThread: Running...");
        return true;
    }

    /**
     * Disconnects bluetooth
     * Interrupts ConnectThread and ConnectedThread
     */
    public void kill() {
        isKilling = true;
        mConnectThread.interrupt();
        mConnectThread.cancel();
        mConnectThread = null;
        mBluetoothAdapter.startDiscovery();
        isKilling = false;

        runOnUiThread(updateWearableState);
    }

    /**
     * Queues kill() on main thread
     * @param r
     */
    private void runOnUiThread(Runnable r){
        new Handler(Looper.getMainLooper()).post(r);
    }

    /**
     * Runnable to update ui connection/calibration to false;
     */
    private Runnable updateWearableState = new Runnable() {
        @Override
        public void run() {
            WearableState.getInstance().setIsCalibrated(false);
            WearableState.getInstance().setIsConnected(false);
        }
    };

    /**
     * Takes a string and writes to wearable
     * @param valueToWrite
     */
    public void write(String valueToWrite){
        //Send * to arduino
        Log.d("sending message", valueToWrite);

        try {
            for(int i = 0; i < valueToWrite.length(); i++){
                Log.d("Sending", valueToWrite.substring(i,i+1));
                mConnectedThread.write(valueToWrite.substring(i,i+1).getBytes());
            }
            Log.d("ConnectedThread.Write", valueToWrite);
        } catch (NullPointerException e) {
            Log.d("Null ConnectedThread", e.toString());
        }
    }

    /**
     * ConnectThread creates establishes the connection
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            try{
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                Log.d("socket", tmp.toString());
            } catch(IOException e){
                Log.d("tmp Socket", e.toString());
            }
            mmSocket = tmp;
            Log.d(BLUETOOTH, "Socket established: " + mmSocket.toString());
        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();
            try{
                mmSocket.connect();
            } catch(IOException connectException) {
                Log.d("Connect Exception", connectException.toString());
                try{
                    mmSocket.close();
                    runOnUiThread(updateWearableState);
                }catch (IOException closeException){
                    Log.d("Close Exception", closeException.toString());
                }
                return;
            }

            mConnectedThread = new ConnectedThread(mmSocket);
            Log.d("Connected Thread", "Created");
            mConnectedThread.start();
            try {
                Thread.sleep(1000);
                Log.d("Sleep before write", "Writing: " + CONNECT_CHAR.getBytes());
            } catch (Exception e){}
            mConnectedThread.write("q".getBytes());
            mConnectedThread.write(CONNECT_CHAR.getBytes());
            Log.d("Connected Thread", "Running...");
        }

        public void cancel() {
            if (this.mmSocket != null) {
                try {
                    Log.d(BLUETOOTH, "ConnectedThread: " + mConnectedThread.getState().toString());
                    mConnectedThread.interrupt();
                    Log.d(BLUETOOTH, "ConnectedThread: " + mConnectedThread.getState().toString());
                    mConnectedThread.cancel();
                    this.mmSocket.close();
                    this.mmSocket = null;
                } catch (IOException closeException) {
                    Log.d(BLUETOOTH, "Cancel : " + closeException.toString());
                }
            }
        }
    }

    /**
     * ConnectedThread reads and write from the in and out stream.
     */
    private class ConnectedThread extends Thread{
        private BluetoothSocket mmSocket;
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){
            this.mmSocket = socket;
            InputStream inTmp = null;
            OutputStream outTemp = null;

            try {
                inTmp = this.mmSocket.getInputStream();
                outTemp = this.mmSocket.getOutputStream();
            } catch(IOException streamException) {
                //
            }

            mmInStream = inTmp;
            mmOutStream = outTemp;
            Log.d("Bluetooth created", "in and out stream set");
        }

        /**
         * Reset input and output streams and make sure socket is closed.
         * This method will be used during shutdown() to ensure that the connection is properly closed during a shutdown.
         * @return
         */
        private void cancel() {
            Log.d(BLUETOOTH, "ConnectedThread breakdown");
            if (mmInStream != null && mmOutStream != null && mmSocket != null) {
                try {
                    mmInStream.close();
                    mmOutStream.flush();
                    mmOutStream.close();
                    this.mmSocket.close();
                } catch (Exception e) {}
                mmInStream = null;
                mmOutStream = null;
                this.mmSocket = null;
            }
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int begin = 0;
            int bytes = 0;

            while(true) {
                if(mmInStream != null) {
                    try {
                        bytes += mmInStream.read(buffer, bytes, buffer.length - bytes);
                        for (int i = 0; i < bytes; i++) {
                            if (buffer[i] == "#".getBytes()[0]) {
                                mHandler.obtainMessage(1, 0, i, buffer).sendToTarget();

                                //create new buffer/remove processed message
                                byte[] newBuffer = new byte[1024];
                                for(int a = i+1; a < bytes; a++){
                                    newBuffer[a-i-1] = buffer[a];
                                }
                                buffer = newBuffer;
                                bytes -= i;
                            }
                        }

                    } catch (IOException e) {
                        if(!isKilling){ //Connection dropped
                            Log.d("ConnectedThread run()", e.toString());
                            kill();
                            break;
                        }
                    }
                }
            }
        }
        public void write(byte[] bytes){
            try{
                mmOutStream.write(bytes);
            } catch (IOException e) {
                //
            }
        }
    }

    /**
     * Handles the incoming message.
     */
    private Handler mHandler = new Handler() {
        String HANDLER_TAG = "Message Received";
        final int ARDUINO_COLLECTION_INTERVAL = 1000;
        @Override
        public void handleMessage(Message msg) {
            byte[] writeBuf = (byte[]) msg.obj;
            int begin = (int) msg.arg1;
            int end = (int) msg.arg2;
            //Log.d(HANDLER_TAG, msg.toString());
            switch(msg.what){
                case 1:
                    String writeMessage = new String(writeBuf);
                    writeMessage = writeMessage.substring(begin, end);
                    String lastChar = "";
                    //Kill connection if sign out
                    if(GoogleAccountInfo.getInstance().getEmail() == null){
                        kill();
                        Log.d("BLUETOOTH CONNECTION", "ERROR: No user found, disconnecting...");
                    } else {
                        //Log.d(HANDLER_TAG, writeMessage);
                        lastChar = writeMessage.substring(writeMessage.length()-1);
                        //Log.d(HANDLER_TAG, "lastChar: " + lastChar);
                        switch (lastChar){
                            case ",":
                                //Log.d(HANDLER_TAG, ", = " + lastChar);
                                float[] values = parseMessage(writeMessage);
                                mPostureManager.openDB();
                                //long time = Calendar.getInstance().getTimeInMillis() - values.length*ARDUINO_COLLECTION_INTERVAL;
                                for(int i = 0; i < values.length; i++){
                                    mPostureManager.insert(values[i]);
                                    //time += ARDUINO_COLLECTION_INTERVAL;
                                }
                                mPostureManager.closeDB();
                                break;
                            case "*": //calibrate
                                WearableState.getInstance().setIsCalibrated(true);
                                break;
                            case "c": //connect
                                WearableState.getInstance().setIsConnected(true);
                                break;
                        }
                    }
                    break;
            }
        }
    };

    /**
     * Used to parse messaged into array of floats
     * @param message incoming message
     * @return float[] of values received from wearables
     */
    private float[] parseMessage(String message){
        String[] strValues;
        if(message.endsWith(",")){
            message = message.substring(0, message.length()-1); //chop last comma
            strValues = message.split(",");
            float[] fvalues = new float[strValues.length];
            for(int i = 0; i < strValues.length; i++){
                if(isNumeric(strValues[i])){
                    fvalues[i] = Float.parseFloat(strValues[i]);
                } else {
                    //something went wrong, dump?
                }
            }
            return fvalues;
        }
        return null;
    }

    /**
     * Verifies a string is numeric
     * @param str to be checked
     * @return true if str is numeric, false otherwise
     */
    private boolean isNumeric(String str){
        try{
            float value = Float.parseFloat(str);
        } catch(NumberFormatException e){
            return false;
        }
        return true;
    }
}
