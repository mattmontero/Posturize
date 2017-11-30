package edu.sjsu.posturize.posturize.bluetooth;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Matt on 11/18/2017.
 * Singleton to track the state of the wearables
 */

public class WearableState extends Observable{

    public final static String CALIBRATED = "CALIBRATED";
    public final static String NOT_CALIBRATED = "NOT_CALIBRATED";
    public final static String CONNECTED = "CONNECTED";
    public final static String DISCONNECTED = "DISCONNECTED";

    private static WearableState singletonWearableState;
    private static String isConnected;
    private static String isCalibrated;

    /**
     * Initial values set to disconnected
     */
    private WearableState(){
        isConnected = DISCONNECTED;
        isCalibrated = NOT_CALIBRATED;
    }

    public static WearableState getInstance(){
        if (singletonWearableState == null){
            singletonWearableState = new WearableState();
        }
        return singletonWearableState;
    }

    public void setIsCalibrated(boolean isCalibrated) {
        WearableState.isCalibrated = ((isCalibrated) ? CALIBRATED : NOT_CALIBRATED);
        setChanged();
        notifyObservers(WearableState.isCalibrated);
    }

    public void setIsConnected(boolean isConnected) {
        WearableState.isConnected = ((isConnected) ? CONNECTED : DISCONNECTED);
        setChanged();
        notifyObservers(WearableState.isConnected);
    }

    public boolean isCalibrated() {
        return isCalibrated.equals(CALIBRATED);
    }

    public boolean isConnected() {
        return isConnected.equals(CONNECTED);
    }


}
