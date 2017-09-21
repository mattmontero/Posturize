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
import java.util.UUID;

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

    public void setTextView(TextView tv){
        mTextView = tv;
        Log.d("tv", tv.toString());
        Log.d("Set TextView", mTextView.toString());
    }

    public boolean isConnected(){
        if(mConnectThread.isAlive()) {
            Log.d("isConnected", "Connected");
        } else {
            Log.d("isConnected", "Not Connnected");
        }

        return mConnectThread.isAlive();
    }

    public void cancelConnectThread(){
        mConnectThread.cancel();
    }

    public void startConnectThread(){
        if(mConnectThread != null){
            Log.d(BLUETOOTH, "mConnectThread " + mConnectThread.toString());
            mConnectThread.start();
        }
    }

    private class ConnectThread extends Thread {
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
            try{
                mmSocket.close();
            } catch(IOException closeException){
                //
            }
        }
    }

    private class ConnectedThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){
            mmSocket = socket;
            InputStream inTmp = null;
            OutputStream outTemp = null;

            try {
                inTmp = mmSocket.getInputStream();
                outTemp = mmSocket.getOutputStream();
            } catch(IOException streamException) {
                //
            }

            mmInStream = inTmp;
            mmOutStream = outTemp;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int begin = 0;
            int bytes = 0;

            while(true) {
                try {
                    Log.d("bytes", Integer.toString(bytes));
                    bytes += mmInStream.read(buffer, bytes, buffer.length - bytes);
                    for(int i = begin; i < bytes; i++){
                        if(buffer[i] == "#".getBytes()[0]) {
                            Log.d("Found #", mHandler.toString());
                            mHandler.obtainMessage(1, begin, i, buffer).sendToTarget();
                            Log.d("Message obtained", mHandler.toString());
                            begin++;
                            if(i == bytes - 1){
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
                    mTextView.setText(writeMessage);
                    Log.d("receiving", writeMessage);

                    break;
            }

        }
    };

}
