package edu.sjsu.posturize.posturize.data.localdb;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

import edu.sjsu.posturize.posturize.data.PostureMeasurement;
import edu.sjsu.posturize.posturize.users.GoogleAccountInfo;

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

    /**
     * Opens SQLite database. This should not be called on the main thread
     */
    public void openDB(){
        db = new PosturizeDBContract(context);
        db.open();
    }

    /**
     * Closed SQLite database.
     */
    public void closeDB(){
        db.close();
    }

    /**
     * Inserts a row with current user ID, email, time, and provided value
     * @param value float received from wearables.
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long insert(float value){
        //Log.d("ADDING MILLIS", "current millis: " + Calendar.getInstance().getTimeInMillis());
        return db.insertRow(GoogleAccountInfo.getInstance().getId(), GoogleAccountInfo.getInstance().getEmail(), Calendar.getInstance().getTimeInMillis(), value);
    }

    /**
     * Deletes where user_id = userId
     * @param userId current userID
     */
    public void delete(String userId){
        if (db.deleteUser(userId)) {
            Log.d("PostureManager", "User: " + GoogleAccountInfo.getInstance().getEmail() + " deleted");
        } else {
            Log.d("Posturemanager", "Something happened and " + GoogleAccountInfo.getInstance().getEmail() + "was NOT deleted");
        }
    }

    public ArrayList<String> getAllUser(){
        ArrayList<String> values = new ArrayList<>();
        Cursor cursor = db.getUniqueUserId();
        //Cursor cursor = db.getAllRows();
        if(cursor.moveToFirst()) {
            while (true) {
                values.add(cursor.getString(0));
                if(!cursor.moveToNext())
                    break;
            }
        }
        cursor.close();
        return values;
    }

    /**
     * @param day Calendar day to grab all measurements for the current user
     * @return ArrayList<PostureMeasurement> all received slouch values for the current user
     */
    public ArrayList<PostureMeasurement> get(Calendar day){
        return construct(db.getDay(day));
    }

    public ArrayList<PostureMeasurement> get(String id, Calendar day){
        return construct(db.getDay(id, day));
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
