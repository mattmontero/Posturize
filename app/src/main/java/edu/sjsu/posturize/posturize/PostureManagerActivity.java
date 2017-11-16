package edu.sjsu.posturize.posturize;

import edu.sjsu.posturize.posturize.data.localdb.PostureManager;
import edu.sjsu.posturize.posturize.users.GoogleAccountInfo;

import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.TextView;

import java.util.Calendar;

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
        ((TextView) findViewById(R.id.textDisplay)).setText(tempPm.get(Calendar.getInstance()).toString());
        tempPm.closeDB();
    }

    private void deleteUserRecords(){
        tempPm.openDB();
        tempPm.delete(GoogleAccountInfo.getInstance().getEmail());
        tempPm.closeDB();
        displayRecords();
    }

    private  void addRecord() {
        tempPm.openDB();
        tempPm.insert((float)(Math.random() * (70 - 65) + 65));
        ((TextView) findViewById(R.id.textDisplay)).setText(tempPm.get(Calendar.getInstance()).toString());
        tempPm.closeDB();
    }
}
