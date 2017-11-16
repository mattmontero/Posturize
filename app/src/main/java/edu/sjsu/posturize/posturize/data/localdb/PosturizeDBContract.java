package edu.sjsu.posturize.posturize.data.localdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.Calendar;

import edu.sjsu.posturize.posturize.users.GoogleAccountInfo;


/**
 * Created by Matt on 11/2/2017.
 */

public final class PosturizeDBContract {
    private static final String TAG = "PosturizeBDContract";

    public static final String DATABASE_NAME = "PosturizeDB";
    public static final int DATABASE_VERSION = 2;

    public static class PostureEntry implements BaseColumns {
        public static final String TABLE_NAME = "posturize";
        public static final String KEY_USER_ID = "userid";
        public static final String KEY_USER = "user";
        public static final String KEY_DATETIME = "datetime";
        public static final String KEY_VALUE = "value";
        public static final String[] ALL_KEYS = new String[] {
                _ID, KEY_USER, KEY_DATETIME, KEY_VALUE};

        public static final int COL__ID = 0;
        public static final int COL_USER = 1;
        public static final int COL_DATETIME = 2;
        public static final int COL_VALUE = 3;
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PostureEntry.TABLE_NAME + " (" +
                    PostureEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    PostureEntry.KEY_USER_ID + " TEXT NOT NULL, " +
                    PostureEntry.KEY_USER + " TEXT NOT NULL, " +
                    PostureEntry.KEY_DATETIME + " LONG NOT NULL, " +
                    PostureEntry.KEY_VALUE + " FLOAT NOT NULL)";
    private static final String SQL_DROP_ENTRIES =
            "DROP TABLE IF EXISTS " + PostureEntry.TABLE_NAME;

    private final Context context;

    private DBHelper mDbHelper;
    private SQLiteDatabase mDb;

    //**********
    //* Public *
    //**********
    public PosturizeDBContract(Context context){
        this.context = context;
        mDbHelper = new DBHelper(context);
    }
    public PosturizeDBContract open(){
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public long insertRow(String userId, String user, long datestamp, float value){
        ContentValues values = new ContentValues();
        values.put(PostureEntry.KEY_USER_ID, userId);
        values.put(PostureEntry.KEY_USER, user);
        values.put(PostureEntry.KEY_DATETIME, datestamp);
        values.put(PostureEntry.KEY_VALUE, value);

        return mDb.insert(PostureEntry.TABLE_NAME, null, values);
    }

    public boolean deleteUser(String userId){
        String where = PostureEntry.KEY_USER_ID + " = '" + userId + "'";
        return mDb.delete(PostureEntry.TABLE_NAME, where, null) != 0;
    }

    public boolean deleteRow(long rowId){
        String where = PostureEntry._ID + "=" + rowId;
        return mDb.delete(PostureEntry.TABLE_NAME, where, null) != 0;
    }

    public void deleteAll() {
        Cursor c = getAllRows();
        long rowId = c.getColumnIndexOrThrow(PostureEntry._ID);
        if (c.moveToFirst()) {
            do {
                deleteRow(c.getLong((int) rowId));
            } while (c.moveToNext());
        }
        c.close();
    }

    // select * from posturize.
    public Cursor getAllRows() {
        String where = null;
        Cursor c = 	mDb.query(true, PostureEntry.TABLE_NAME, PostureEntry.ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public Cursor getDay(Calendar day){
        long[] startEnd = dayStartAndEndInMillis(day);
        Log.d(TAG, "Start: " + startEnd[0]);
        Log.d(TAG, "End  : " + startEnd[1]);

        String where = PostureEntry.KEY_DATETIME + " >= " + startEnd[0] + " AND " +
                        PostureEntry.KEY_DATETIME + " < " + startEnd[1] + " AND " +
                        PostureEntry.KEY_USER_ID + " = '" + GoogleAccountInfo.getInstance().getId() + "'";
        Log.d(TAG, "WHERE: " + where);

        Cursor c = mDb.query(PostureEntry.TABLE_NAME, PostureEntry.ALL_KEYS,
                where, null, null, null, PostureEntry.KEY_DATETIME);
        if(c != null)
            c.moveToFirst();
        return c;
    }

    public Cursor getDays(Calendar start, Calendar end){
        long startMillis = dayStartAndEndInMillis(start)[0];
        long endMillis = dayStartAndEndInMillis(end)[1];
        Log.d(TAG, "Start: " + startMillis);
        Log.d(TAG, "End  : " + endMillis);

        String where = PostureEntry.KEY_DATETIME + " >= " + startMillis + " AND " +
                PostureEntry.KEY_DATETIME + " < " + endMillis + " AND " +
                PostureEntry.KEY_USER + " = '" + GoogleAccountInfo.getInstance().getId() + "'";
        Log.d(TAG, "WHERE: " + where);

        Cursor c = mDb.query(PostureEntry.TABLE_NAME, PostureEntry.ALL_KEYS,
                where, null, null, null, PostureEntry.KEY_DATETIME);

        if(c != null)
            c.moveToFirst();
        return c;
    }

    /*
     * @param day
     * @return long[] First millisecond of day and last millisecond of day
     */
    private long[] dayStartAndEndInMillis(Calendar c){
        //Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        long start = c.getTimeInMillis();
        c.set(Calendar.HOUR, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        long end =  c.getTimeInMillis();

        return new long[]{start, end};
    }

    private static class DBHelper extends SQLiteOpenHelper{
        DBHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase mmDb){
            mmDb.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase mmDb, int oldVersion, int newVersion){
            Log.w(TAG, "Upgrading application's database from version " + oldVersion +
                    " to " + newVersion + ", which will destroy all old data");

            mmDb.execSQL(SQL_DROP_ENTRIES);

            // TODO: sync data.

            onCreate(mmDb);
        }
    }
}
