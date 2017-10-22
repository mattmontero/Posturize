package edu.sjsu.posturize.posturize.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.icu.util.Output;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.UUID;

import edu.sjsu.posturize.posturize.SexyData.PostureMeasurement;

/**
 * Created by matthewmontero on 8/6/17.
 */

public class BluetoothConnection {
    private BluetoothAdapter mBluetoothAdapter;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private TextView mTextView;

    private final String BLUETOOTH = "Connection Setup";

    public BluetoothConnection(BluetoothAdapter btAdapter){
        mBluetoothAdapter = btAdapter;
    }

    public void connectThread(BluetoothDevice device){
        Log.d(BLUETOOTH, device.getName());
        mConnectThread = new ConnectThread(device);
    }

    public void write(String valueToWrite){
        //Send * to arduino
        Log.d("sending message", valueToWrite);

        //Log.d("byte[]", msg.getBytes().toString());
        try {
            for(int i = 0; i < valueToWrite.length(); i++){
                Log.d("Sending", valueToWrite.substring(i,i+1));
                mConnectedThread.write(valueToWrite.substring(i,i+1).getBytes());
            }
            Log.d("ConnectedThread.Write", valueToWrite.getBytes().toString());
        } catch (NullPointerException e) {
            Log.d("Null ConnectedThread", e.toString());
        }
    }

    public void setTextView(TextView tv){
        mTextView = tv;
        Log.d("tv", tv.toString());
        Log.d("Set TextView", mTextView.toString());
    }

    public boolean isConnected(){
        if(mConnectThread != null) {
            Log.d("isConnected", "Connected");
            Log.d("isConnected", "Thread State: " + mConnectThread.getState().toString());
        } else {
            Log.d("isConnected", "Not Connnected");
        }

        return mConnectThread != null;
    }

    public void kill() {
        Log.d(BLUETOOTH, "Thread State: " + mConnectThread.getState().toString());

        mConnectThread.interrupt();
        Log.d(BLUETOOTH, "ConnectedThread: " + mConnectThread.getState().toString());

        mConnectThread.cancel();
        mConnectThread = null;
        mBluetoothAdapter.startDiscovery();
    }

    public void startConnectThread(){
        if(mConnectThread != null){
            Log.d(BLUETOOTH, "mConnectThread " + mConnectThread.toString());
            mConnectThread.start();
            Log.d(BLUETOOTH, "Thread Status: " + mConnectThread.getState().toString());
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
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
                }catch (IOException closeException){
                    Log.d("Close Exception", closeException.toString());
                }
                return;
            }

            mConnectedThread = new ConnectedThread(mmSocket);
            Log.d("Connected Thread", "Created");
            mConnectedThread.start();
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
                    Log.d(BLUETOOTH, "InStream : " + mmInStream.toString() );
                    mmInStream.close();
                    Log.d(BLUETOOTH, "OutStream : " + mmOutStream.toString() );
                    mmOutStream.flush();
                    mmOutStream.close();
                    Log.d(BLUETOOTH, "InStream : " + this.mmSocket.toString() );
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
                        //Log.d("bytes", Integer.toString(bytes));
                        bytes += mmInStream.read(buffer, bytes, buffer.length - bytes);
                        for (int i = begin; i < bytes; i++) {
                            if (buffer[i] == "#".getBytes()[0]) {
                                Log.d("Found #", mHandler.toString());
                                mHandler.obtainMessage(1, begin, i, buffer).sendToTarget();
                                Log.d("Message obtained", mHandler.toString());
                                begin++;
                                if (i == bytes - 1) {
                                    bytes = 0;
                                    begin = 0;
                                }
                            }
                        }
                    } catch (IOException e) {
                        Log.d("ConnectedThread run()", e.toString());
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

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            byte[] writeBuf = (byte[]) msg.obj;
            int begin = (int) msg.arg1;
            int end = (int) msg.arg2;
            Log.d("Handler", "Message");
            Log.d("BING handler", msg.toString());
            switch(msg.what){
                case 1:
                    String writeMessage = new String(writeBuf);
                    writeMessage = writeMessage.substring(begin, end);
                    //mTextView.setText(writeMessage);
                    PostureMeasurement current = new PostureMeasurement(new Date(), Float.parseFloat(writeMessage));
                    mTextView.setText(current.toString());
                    Log.d("receiving", writeMessage);

                    break;
            }

        }
    };

}
