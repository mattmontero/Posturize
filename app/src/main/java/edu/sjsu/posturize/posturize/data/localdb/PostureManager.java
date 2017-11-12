package edu.sjsu.posturize.posturize.data.localdb;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import edu.sjsu.posturize.posturize.data.PostureMeasurement;
import edu.sjsu.posturize.posturize.users.PosturizeUser;
import edu.sjsu.posturize.posturize.users.PosturizeUserInfo;

/**
 * Created by Matt on 11/3/2017.
 */

public class PostureManager {
    private Context context;
    private PosturizeDBContract db;

    /***
     *@param context application context
     */
    public PostureManager(Context context){
        this.context = context;
    }

    public void openDB(){
        db = new PosturizeDBContract(context);
        db.open();
    }

    public void closeDB(){
        db.close();
    }

    public long insert(float value, long time){
        //Log.d("ADDING MILLIS", "current millis: " + Calendar.getInstance().getTimeInMillis());
        long newId = db.insertRow(PosturizeUserInfo.getInstance().getEmail(), time, value);
        return newId;
    }

    public void delete(String user){
        if (db.deleteUser(user)) {
            Log.d("PostureManager", "User: " + PosturizeUserInfo.getInstance().getEmail() + " deleted");
        } else {
            Log.d("Posturemanager", "Something happened and " + PosturizeUserInfo.getInstance().getEmail() + "was NOT deleted");
        }
    }

    public ArrayList<PostureMeasurement> get(Calendar day){
        return construct(db.getDay(day));
    }

    public ArrayList<PostureMeasurement> get(Calendar start, Calendar end){
        return construct(db.getDays(start, end));
    }

    private ArrayList<PostureMeasurement> construct(Cursor cursor){
        ArrayList<PostureMeasurement> values = new ArrayList<>();
        if(cursor.moveToFirst()) {
            while (true) {
                values.add(new PostureMeasurement(
                        cursor.getLong(PosturizeDBContract.PostureEntry.COL_DATETIME),
                        cursor.getFloat(PosturizeDBContract.PostureEntry.COL_VALUE)));
                if(!cursor.moveToNext())
                    break;
            }
        }
        cursor.close();
        return values;
    }
}
