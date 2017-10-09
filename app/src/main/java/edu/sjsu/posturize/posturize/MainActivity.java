package edu.sjsu.posturize.posturize;

import edu.sjsu.posturize.posturize.bluetooth.*;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;

import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener{

    private static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothConnection mBluetoothConnection;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private TextView mTextView;
    private Button mConnectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("onCreate", "Starting");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setTitle(getString(R.string.signed_in_greeting, "User"));
        setViewsAndListeners();
        connectBLE();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTextView = (TextView) findViewById(R.id.numberViewer);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        Log.d("onCreate","Done");
    }

    private boolean connectBLE(){
        final String BLUETOOTH = "Bluetooth_Setup";
        //1. Check if bluetooth is supported
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothConnection = new BluetoothConnection(mBluetoothAdapter);
        mBluetoothConnection.setTextView(mTextView);
        Log.d("Text View Setup", "mTextView");
        //1. Check if device has bluetooth.
        if(mBluetoothAdapter == null){
            //Device does not support Bluetooth.
            Log.d(BLUETOOTH, "Bluetooth is not supported");
            return false;
        } else {
            Log.d(BLUETOOTH, "Bluetooth is supported");
        }
        //2. Check if bluetooth is enabled
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(BLUETOOTH, "Bluetooth is not enabled");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 6969);
        } else {
            Log.d(BLUETOOTH, "Bluetooth is enabled");
        }

        //3. Get the Bluetooth module device
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        //mDevice should end up being HC-06
        BluetoothDevice mDevice = null;
        if(pairedDevices.size() > 0){
            for(BluetoothDevice device : pairedDevices) {
                if(device.getName().equals("HC-06")){
                    //This is our bluetooth device.
                    mDevice = device;
                    Log.d(BLUETOOTH, device.getName());
                    Log.d(BLUETOOTH, device.toString());
                    break;
                }
            }
        }
        if(mDevice == null){
            Log.d(BLUETOOTH, "No device found");
            mConnectButton.setText("Connect");
            return false;
        }

        Log.d(BLUETOOTH, mDevice.getName());
        Log.d(BLUETOOTH, mDevice.toString());

        //4. Create the connection thread
        mBluetoothConnection.connectThread(mDevice);
        Log.d("ConnectThread", "created");
        mBluetoothConnection.startConnectThread();
        Log.d("ConnectThread", "Running...");
        mBluetoothConnection.isConnected();
        mConnectButton.setText("Disconnect");
        return true;

    }

    private void setViewsAndListeners(){
        mTextView = (TextView)findViewById(R.id.numberViewer);
        mConnectButton = (Button) findViewById(R.id.connectButton);
        //((Button) findViewById(R.id.frontporch_signInButton)).setOnClickListener(this);
        ((Button) findViewById(R.id.signoutButton)).setOnClickListener(this);
        ((Button) findViewById(R.id.calibrateButton)).setOnClickListener(this);
        ((Button) findViewById(R.id.refreshButton)).setOnClickListener(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void calibrate(){
        mTextView.setText("Calibrating...");
        mBluetoothConnection.write("*");
    }

    private void fpSignIn(Intent intent){
        startActivity(intent);
    };

    private void connectButtonPressed() {
        if(mBluetoothConnection.isConnected()){
            mBluetoothConnection.cancelConnectThread();
            mTextView.setText("Disconnected");
            ((Button)findViewById(R.id.connectButton)).setText("Connect");
        } else {
            mTextView.setText("Connecting...");
            if(connectBLE()){
                mTextView.setText("Connected!");
                ((Button)findViewById(R.id.connectButton)).setText("Disconnect");
            }
            mTextView.setText("Something bad happened.");
        }
    }

    private void refresh(){
        mTextView.setText("Super Fresh");
    }

    @Override
    public void onClick(View view) {
        Log.d("onClick", view.toString());
        switch (view.getId()){
            case R.id.calibrateButton:
                calibrate();
                break;
            case R.id.refreshButton:
                refresh();
                break;
            case R.id.connectButton:
                connectButtonPressed();
                break;
            case R.id.signoutButton:
                //mBluetoothConnection.kill();
                //userData.save()
                this.finish();
                break;
            /*
            case R.id.frontporch_signInButton:
                fpSignIn(new Intent(this, SignInActivity.class));
                break;
            */
            default:
                break;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            Log.d("onCreateView", "Start");
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));


            Log.d("onCreateView", "Done");
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }
}
