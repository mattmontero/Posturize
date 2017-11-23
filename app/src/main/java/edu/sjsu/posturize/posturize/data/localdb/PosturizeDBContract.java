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
    public static final int DATABASE_VERSION = 3;

    public static class PostureEntry implements BaseColumns {
        public static final String TABLE_NAME = "posturize";
        public static final String KEY_USER_ID = "userid";
        public static final String KEY_USER = "user";
        public static final String KEY_DATETIME = "datetime";
        public static final String KEY_VALUE = "value";
        public static final String[] ALL_KEYS = new String[] {
                _ID, KEY_USER_ID, KEY_USER, KEY_DATETIME, KEY_VALUE};

        public static final int COL__ID = 0;
        public static final int COL_USER_ID = 1;
        public static final int COL_USER = 2;
        public static final int COL_DATETIME = 3;
        public static final int COL_VALUE = 4;
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

    public boolean isOpen(){
        return mDb.isOpen();
    }

    //**********
    //* Public *
    //**********
    public PosturizeDBContract(Context context){
        this.context = context;
        mDbHelper = new DBHelper(context);
    }

    /**
     * Opens SQLite database. This should not be called from the main thread.
     * @return PosturizeDBContract singleton
     */
    public PosturizeDBContract open(){
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    /**
     * Insert new row
     * @param userId
     * @param user
     * @param datestamp
     * @param value
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long insertRow(String userId, String user, long datestamp, float value){
        ContentValues values = new ContentValues();
        values.put(PostureEntry.KEY_USER_ID, userId);
        values.put(PostureEntry.KEY_USER, user);
        values.put(PostureEntry.KEY_DATETIME, datestamp);
        values.put(PostureEntry.KEY_VALUE, value);

        return mDb.insert(PostureEntry.TABLE_NAME, null, values);
    }

    /**
     * Deletes user from table
     * @param userId
     * @return the number of rows affected if a whereClause is passed in, 0 otherwise. To remove all rows and get a count pass "1" as the whereClause.
     */
    public boolean deleteUser(String userId){
        String where = PostureEntry.KEY_USER_ID + " = '" + userId + "'";
        return mDb.delete(PostureEntry.TABLE_NAME, where, null) != 0;
    }

    /**
     * Deletes single row by rowID in posturize
     * @param rowId
     * @return the number of rows affected if a whereClause is passed in, 0 otherwise. To remove all rows and get a count pass "1" as the whereClause.
     */
    public boolean deleteRow(long rowId){
        String where = PostureEntry._ID + "=" + rowId;
        return mDb.delete(PostureEntry.TABLE_NAME, where, null) != 0;
    }

    /**
     * Deletes all rows in table posturize
     */
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

    //selecct unique userId from posturize
    public Cursor getUniqueUserId(){
        String where = null;
        Cursor c = mDb.rawQuery("SELECT DISTINCT userid FROM posturize", null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public Cursor getRow(long id){
        String where = PostureEntry._ID + " = '" + id + "'";
        Cursor c = mDb.query(true, PostureEntry.TABLE_NAME, PostureEntry.ALL_KEYS,
                where, null, null, null, null, null);
        if(c != null){
            c.moveToFirst();
        }
        return c;
    }

    /**
     * Select * from posturize
     * @return Cursor for query
     */
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
        return this.getDay(GoogleAccountInfo.getInstance().getId(), day);
    }

    public Cursor getDay(String id, Calendar day){
        long[] startEnd = dayStartAndEndInMillis(day);
        Log.d(TAG, "Start: " + startEnd[0]);
        Log.d(TAG, "End  : " + startEnd[1]);

        String where = PostureEntry.KEY_USER_ID + " = '" + id + "'" + " AND " +
                        PostureEntry.KEY_DATETIME + " >= " + startEnd[0] + " AND " +
                        PostureEntry.KEY_DATETIME + " < " + startEnd[1];

        Log.d(TAG, "WHERE: " + where);

        Cursor c = mDb.query(PostureEntry.TABLE_NAME, PostureEntry.ALL_KEYS,
                where, null, null, null, PostureEntry.KEY_DATETIME);
        if(c != null)
            c.moveToFirst();
        return c;
    }

    /**
     * @param start Starting Day
     * @param end Ending Day
     * @return Cursor for the query to grab all keys where time stamp is in between start and end.
     */
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

            onCreate(mmDb);
        }
    }
}
