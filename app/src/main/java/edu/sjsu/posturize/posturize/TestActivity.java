package edu.sjsu.posturize.posturize;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;

import edu.sjsu.posturize.posturize.data.localdb.PostureManager;
import edu.sjsu.posturize.posturize.data.localdb.PosturizeDBContract;

public class TestActivity extends AppCompatActivity {
    PosturizeDBContract myDb;
    PostureManager pm;
    TextView dbText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        dbText = (TextView) findViewById(R.id.textDisplay);

        pm = new PostureManager(this.getApplicationContext());
        pm.openDB();
        //openDB();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pm.closeDB();
        //closeDB();
    }


    private void openDB() {
        myDb = new PosturizeDBContract(this);
        myDb.open();
    }
    private void closeDB() {
        myDb.close();
    }



    private void displayText(String message) {
        TextView textView = (TextView) findViewById(R.id.textDisplay);
        textView.setText(message);
    }



    public void onClick_AddRecord(View v) {
        long newId = pm.insert((float)(Math.random() * (70 - 65) + 65));

        displayText(pm.get(Calendar.getInstance()).toString());
    }

    public void onClick_ClearAll(View v) {
        myDb.deleteAll();
    }

    public void onClick_DisplayRecords(View v) {
        displayText(pm.get(Calendar.getInstance()).toString());

        //Cursor cursor = myDb.getAllRows();
        //displayRecordSet(cursor);
    }

    // Display an entire recordset to the screen.
    private void displayRecordSet(Cursor cursor) {
        String message = "";
        // populate the message from the cursor

        // Reset cursor to start, checking to see if there's data:
        if (cursor.moveToFirst()) {
            do {
                // Process the data:
                int id = cursor.getInt(PosturizeDBContract.PostureEntry.COL__ID);
                String name = cursor.getString(PosturizeDBContract.PostureEntry.COL_USER);
                long datetime = cursor.getLong(PosturizeDBContract.PostureEntry.COL_DATETIME);
                float value = cursor.getFloat(PosturizeDBContract.PostureEntry.COL_VALUE);

                // Append data to the message:
                message += "id=" + id
                        +", name=" + name
                        +", date=" + datetime
                        +", value=" + value
                        +"\n";
            } while(cursor.moveToNext());
        }

        // Close the cursor to avoid a resource leak.
        cursor.close();

        displayText(message);
    }
}
