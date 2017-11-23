package edu.sjsu.posturize.posturize;

import edu.sjsu.posturize.posturize.data.localdb.PostureManager;
import edu.sjsu.posturize.posturize.users.GoogleAccountInfo;

import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.TextView;

import java.text.DecimalFormat;
import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.SimpleFormatter;

public class PostureManagerActivity extends AppCompatActivity
        implements View.OnClickListener{

    private PostureManager tempPm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreate: Starting");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posture_manager);
        setViewsAndListeners();

        tempPm = new PostureManager(this.getApplicationContext());

    }

    private void setViewsAndListeners(){
        this.setTitle(getString(R.string.signed_in_greeting, GoogleAccountInfo.getInstance().getFirstName()));
        findViewById(R.id.add_record).setOnClickListener(this);
        findViewById(R.id.delete_records).setOnClickListener(this);
        findViewById(R.id.display_records).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Log.d("onClick", view.toString());
        switch (view.getId()){
            //TODO: temp buttons
            case R.id.add_record:
                addRecord();
                break;
            case R.id.delete_records:
                deleteUserRecords();
                break;
            case R.id.display_records:
                displayRecords();
                break;
            //TODO: End temp buttons
            default:
                break;
        }
    }

    private void displayRecords(){
        tempPm.openDB();
        ((TextView) findViewById(R.id.textDisplay)).setText(formatQuery(tempPm.get(Calendar.getInstance())));
        tempPm.closeDB();
    }

    private void deleteUserRecords(){
        tempPm.openDB();
        tempPm.delete(GoogleAccountInfo.getInstance().getId());
        tempPm.closeDB();
        displayRecords();
    }

    private  void addRecord() {
        tempPm.openDB();
        DecimalFormat df = new DecimalFormat("#.00");
        float value = Float.parseFloat(df.format((float)(Math.random() * (5 - 3) + 3)));
        tempPm.insert(value);
        ((TextView) findViewById(R.id.textDisplay)).setText(formatQuery(tempPm.get(Calendar.getInstance())));
        tempPm.closeDB();
    }

    private String formatQuery(ArrayList<DataPoint> dps){
        String data = "[";
        for(int i = 0; i < dps.size(); i++){
            data = data.concat("[" + (long)(dps.get(i).getX()) + ", " + dps.get(i).getY() + "]");
            if(i < dps.size()-1){
                data = data.concat(", ");
            }
        }
        data = data.concat("]");
        Log.d("FormatQuery", data);
        return data;
    }
}
