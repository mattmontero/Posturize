package edu.sjsu.posturize.posturize;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.bluetooth.BluetoothAdapter;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import edu.sjsu.posturize.posturize.bluetooth.BluetoothConnection;
import edu.sjsu.posturize.posturize.reminder.AlarmNotificationReceiver;
import edu.sjsu.posturize.posturize.users.PosturizeUserInfo;

public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final int USER_SIGN_IN = 9001;
    private static final int USER_SIGN_OUT = 9002;
    private static final String TAG = "SignInActivity";

    private static Context appContext;
    private SharedPreferences sharedPreferences;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        appContext = this.getApplicationContext();
        sharedPreferences = getSharedPreferences("SAVED_LOGIN", Context.MODE_PRIVATE);
        findViewById(R.id.google_sign_in_button).setOnClickListener(this);

        setGoogleApiClient();

        if(sharedPreferences.getBoolean("REMEMBER_ME", false)) {
            silentSignIn();
        }
    }

    private void silentSignIn(){
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            //showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    /*
     * workaround to get context in any non-activity class
     */
    public static Context getAppContext(){
        return appContext;
    }

    private void setGoogleApiClient(){
        /*
         * Configure Google Sign-In and the GoogleApiClient Object
         * 1. create GoogleSignInOptions object
         * Configure sign-in to request the user's ID, email address, and basic
         * profile. ID and basic profile are included in DEFAULT_SIGN_IN.
         */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.server_client_id))
                .build();
        Log.d(TAG, "googleSignInOptions:" + gso.toString());
        //If we need to request additional scopes to access Google APIs, specify them with requestScopes.
        /*
         * 2. Create a GoogleApiClient object with access to the Google Sign-In API
         * and the options we specified.
         * Build a GoogleApiClient with access to the Google Sign-In API and the
         * options specified by gso
         */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();
        Log.d(TAG, "GoogleApiClient:" + mGoogleApiClient.toString());
    }

    private void saveUserLogin(boolean rememberMe){
        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        spEditor.putBoolean("REMEMBER_ME", rememberMe);
        spEditor.commit();
    }

    private void googleSignIn(){
        saveUserLogin(((CheckBox) findViewById(R.id.remember_me)).isChecked());
        Intent googleSignInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        Log.d(TAG, "googleSignInIntent" + googleSignInIntent.toString());
        startActivityForResult(googleSignInIntent, USER_SIGN_IN);
    }

    private void googleSignOut(){
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Log.d(TAG, "Sign out status: " + status.toString());
                        PosturizeUserInfo.getInstance().setUser(null);
                        updateUI(false);
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //An unresolvable error has occured and Google APIs (including Sign-in) will not be available.
        Log.d(TAG, "OnConnectionFailed:" + connectionResult.toString());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(PosturizeUserInfo.getInstance().signingOut){
            googleSignOut();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        //
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.google_sign_in_button:
                googleSignIn();
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:requestCode->" + requestCode + " resultCode->" + resultCode);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        switch(requestCode){
            case USER_SIGN_IN:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                Log.d(TAG, "GoogleSignIn:" + result.getSignInAccount());
                handleSignInResult(result);
                break;
        }
    }

    private void handleSignInResult(GoogleSignInResult result){
        Log.d(TAG, "result status message: " + result.getStatus().getStatusCode());
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        Log.d(TAG, "Result toString: " + result.toString());

        if(result.isSuccess()) {
            //Sign in successfully, show authenticated UI.
            PosturizeUserInfo.getInstance().setUser(result.getSignInAccount());
            updateUI(true);
            setDailyUpdate();
        } else {
            PosturizeUserInfo.getInstance().setUser(null);
            updateUI(false);
        }
    }

    private void updateUI(boolean signedIn){
        if(signedIn) {
            BluetoothConnection btConnection = BluetoothConnection.getInstance();
            btConnection.setBluetoothAdapter(BluetoothAdapter.getDefaultAdapter());

            startActivity(new Intent(this, HomeActivity.class));
            findViewById(R.id.google_sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.remember_me).setVisibility(View.GONE);
        } else {
            findViewById(R.id.google_sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.remember_me).setVisibility(View.VISIBLE);
        }
    }

    private void setDailyUpdate() {
        AlarmManager manager = (AlarmManager) getSystemService(getApplicationContext().ALARM_SERVICE);
        Intent intent;
        PendingIntent pendingIntent;
        intent = new Intent(this, AlarmNotificationReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this,0,intent,0);
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_DAY, AlarmManager.INTERVAL_DAY, pendingIntent);
    }
}

