package edu.sjsu.posturize.posturize.data.localdb;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;

import edu.sjsu.posturize.posturize.predefined.Predefined;
import edu.sjsu.posturize.posturize.users.GoogleAccountInfo;

/**
 * Created by Matt on 11/3/2017.
 */

public class PostureManager extends Observable{
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

    public boolean isDBopen(){
        if(db != null) {
            return db.isOpen();
        }
        return false;
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
        long row = db.insertRow(GoogleAccountInfo.getInstance().getId(), GoogleAccountInfo.getInstance().getEmail(), Calendar.getInstance().getTimeInMillis(), value);
        setChanged();
        notifyObservers(row);

        return row;
    }

    public void fakeIt(){
        this.openDB();

        this.delete(GoogleAccountInfo.getInstance().getId());

        Calendar c = Calendar.getInstance();
        for (DataPoint dp : Predefined.SQLiteEntries) {
            c.setTime(new Date((long)(dp.getX())));
            float value = (float)dp.getY();
            db.insertRow(GoogleAccountInfo.getInstance().getId(),
                    GoogleAccountInfo.getInstance().getEmail(),
                    c.getTimeInMillis(),value);
        }
        setChanged();
        notifyObservers();

        this.closeDB();
    }

    /**
     * Deletes where user_id = userId
     * @param userId current userID
     */
    public void delete(String userId){
        if (db.deleteUser(userId)) {
            Log.d("PostureManager", "User: " + GoogleAccountInfo.getInstance().getEmail() + " deleted");
        } else {
            Log.d("PostureManager", "Something happened and " + GoogleAccountInfo.getInstance().getEmail() + " was NOT deleted");
        }

        setChanged();
        notifyObservers();
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

    public DataPoint getRow(long row){
        Cursor c = db.getRow(row);
        if(c != null) {
            return new DataPoint((double) c.getLong(PosturizeDBContract.PostureEntry.COL_DATETIME), (double) PosturizeDBContract.PostureEntry.COL_VALUE);
        }
        return null;
    }

    /**
     * @param day Calendar day to grab all measurements for the current user
     * @return ArrayList<DataPoint> all received slouch values for the current user
     */
    public ArrayList<DataPoint> get(Calendar day){
        return construct(db.getDay(day));
    }

    public ArrayList<DataPoint> get(String id, Calendar day){
        return construct(db.getDay(id, day));
    }

    public ArrayList<DataPoint> get(Calendar start, Calendar end){
        return construct(db.getDays(start, end));
    }

    private ArrayList<DataPoint> construct(Cursor cursor){
        ArrayList<DataPoint> values = new ArrayList<>();
        if(cursor.moveToFirst()) {
            while (true) {
                values.add(new DataPoint(
                        (double) cursor.getLong(PosturizeDBContract.PostureEntry.COL_DATETIME), //time = x
                        (double) cursor.getFloat(PosturizeDBContract.PostureEntry.COL_VALUE))); //slouch = y
                if(!cursor.moveToNext())
                    break;
            }
        }
        cursor.close();
        return values;
    }

    public void empty() {
        db.deleteAll();
    }
}
