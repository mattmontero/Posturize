package edu.sjsu.posturize.posturize.data.localdb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.support.compat.BuildConfig;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.jjoe64.graphview.series.DataPoint;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Calendar;

import edu.sjsu.posturize.posturize.users.GoogleAccountInfo;

import static org.junit.Assert.*;

/**
 * Created by Matt on 11/26/2017.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = edu.sjsu.posturize.posturize.BuildConfig.class, sdk = 21)
public class PostureManagerTest {
    String userId = "123";
    PostureManager pm;


    @Before
    public void setUp() throws Exception {
        pm = new PostureManager(RuntimeEnvironment.application.getApplicationContext());
        GoogleAccountInfo.getInstance().setMock(userId,"me@posturize.com");
    }

    @Test
    public void openDB() throws Exception {
        assertEquals(false, pm.isDBopen());
        pm.openDB();
        assertEquals(true, pm.isDBopen());
        pm.closeDB();
        assertEquals(false, pm.isDBopen());
    }

    @Test
    public void insert() throws Exception {
        pm.openDB();
        pm.empty();
        // Given
        float testFloat1 = -4.50f;
        float testFloat2 = -9.05f;
        pm.insert(testFloat1);
        pm.insert(testFloat2);

        ArrayList<DataPoint> dp = pm.get(Calendar.getInstance());
        assertEquals(2, dp.size());
        assertEquals(testFloat1, dp.get(0).getY(), .001);
        assertEquals(testFloat2, dp.get(1).getY(), .001);
    }

    @Test
    public void getAllUser() throws Exception {
        pm.openDB();
        pm.empty();

        pm.insert(-4.0f);
        assertEquals(userId, pm.getAllUser().get(0));
        assert pm.getAllUser().size() == 1;
        pm.closeDB();
    }
}