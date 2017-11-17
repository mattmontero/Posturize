package edu.sjsu.posturize.posturize;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.bluetooth.BluetoothAdapter;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.util.Date;

import edu.sjsu.posturize.posturize.bluetooth.BluetoothConnection;
import edu.sjsu.posturize.posturize.data.FirebaseHelper;
import edu.sjsu.posturize.posturize.scheduledjob.DailySync;
import edu.sjsu.posturize.posturize.users.GoogleAccountInfo;

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
            handleGoogleSignInResult(result);
        } else {
            //showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    handleGoogleSignInResult(googleSignInResult);
                }
            });
        }
    }

    /**
     * workaround to get context in any non-activity class
     * @return app context
     */
    public static Context getAppContext(){
        return appContext;
    }

    /**
     * Define a mGoogleApiClient and get access to the Google Sign-In API
     */
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

    /**
     *
     * @param rememberMe value to be stored in shared preferences with key "REMEMBER_ME"
     */
    private void saveUserLogin(boolean rememberMe){
        sharedPreferences.edit().putBoolean("REMEMBER_ME", rememberMe).commit();
    }

    /**
     * activate google sign in activity
     */
    private void googleSignIn(){
        saveUserLogin(((CheckBox) findViewById(R.id.remember_me)).isChecked());
        Intent googleSignInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        Log.d(TAG, "googleSignInIntent" + googleSignInIntent.toString());
        startActivityForResult(googleSignInIntent, USER_SIGN_IN);
    }

    /**
     * sign user out of google
     */
    private void googleSignOut(){
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Log.d(TAG, "Sign out status: " + status.toString());
                        GoogleAccountInfo.getInstance().setUser(null);
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
        if(GoogleAccountInfo.getInstance().signingOut){
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
                handleGoogleSignInResult(result);
                break;
        }
    }

    /**
     * setup app to pertain to user's google account
     * @param result
     */
    private void handleGoogleSignInResult(GoogleSignInResult result){
        Log.d(TAG, "result status message: " + result.getStatus().getStatusCode());
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        Log.d(TAG, "Result toString: " + result.toString());

        if(result.isSuccess()) {
            //Sign in successfully, show authenticated UI.
            GoogleSignInAccount account = result.getSignInAccount();
            GoogleAccountInfo.getInstance().setUser(result.getSignInAccount());
            FirebaseHelper.getInstance().addUserToFirestore();
            FirebaseHelper.getInstance().setFirestoreReferenceListeners();

            FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
            int curTimeMilis = (int) new Date().getTime();
            Job myJob = dispatcher.newJobBuilder()
                    .setReplaceCurrent(true)
                    // the JobService that will be called
                    .setService(DailySync.class)
                    // uniquely identifies the job
                    .setTag("PosturizeDailySync")
                    // one-off job
                    .setRecurring(false)
                    // start between 0 and 60 seconds from now
                    //.setTrigger(Trigger.executionWindow(0,(curTimeMilis/86400000+1) * 86400000 - curTimeMilis))
                    .setTrigger(Trigger.executionWindow(60, 60))
                    // don't overwrite an existing job with the same the
                    .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                    // constraints that need to be satisfied for the job to run
//                    .setConstraints(
//                            // only run on an unmetered network
//                            Constraint.ON_UNMETERED_NETWORK,
//                            // only run when the device is charging
//                            Constraint.DEVICE_CHARGING
//                    )
//                    .setExtras(myExtrasBundle)
                    .build();

            dispatcher.mustSchedule(myJob);

            updateUI(true);
        } else {
            GoogleAccountInfo.getInstance().setUser(null);
            updateUI(false);
        }
    }

    /**
     * update UI to current state
     * @param signedIn state of user account
     */
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
}

