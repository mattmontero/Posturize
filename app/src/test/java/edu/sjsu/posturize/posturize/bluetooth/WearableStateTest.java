package edu.sjsu.posturize.posturize.bluetooth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.junit.Assert.*;

/**
 * Created by Matt on 11/26/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class WearableStateTest {
    private WearableState wearableState;

    @Before
    public void setUp() throws Exception{
        wearableState = WearableState.getInstance();
    }

    @Test
    public void setIsCalibrated() throws Exception {
        wearableState.setIsCalibrated(true);
        assertEquals(wearableState.isCalibrated(), true);

        wearableState.setIsCalibrated(false);
        assertEquals(wearableState.isCalibrated(), false);
    }

    @Test
    public void setIsConnected() throws Exception {
        wearableState.setIsConnected(true);
        assertEquals(wearableState.isConnected(), true);

        wearableState.setIsConnected(false);
        assertEquals(wearableState.isConnected(), false);
    }
}