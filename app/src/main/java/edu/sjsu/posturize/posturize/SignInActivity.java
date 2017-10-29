package edu.sjsu.posturize.posturize;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.util.ArrayList;
import java.util.List;

import edu.sjsu.posturize.posturize.Users.PosturizeUserInfo;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class SignInActivity extends AppCompatActivity implements
        LoaderCallbacks<Cursor>,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "SignInActivity";

    private SharedPreferences sharedPreferences;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private View mProgressView;
    private View mLoginFormView;
    private static Context appContext;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        appContext = this.getApplicationContext();
        sharedPreferences = getSharedPreferences("SAVED_LOGIN", Context.MODE_PRIVATE);
        ((SignInButton) findViewById(R.id.google_sign_in_button)).setOnClickListener(this);
        ((Button) findViewById(R.id.google_sign_out_button)).setOnClickListener(this);
        ((Button) findViewById(R.id.continue_button)).setOnClickListener(this);


        mProgressView = findViewById(R.id.login_progress);
        setGoogleApiClient();
    }

    @Override
    protected void onStart(){
        super.onStart();

        if(sharedPreferences.getBoolean("REMEMBER_ME", false)) {
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
                        //hideProgressDialog();
                        handleSignInResult(googleSignInResult);
                    }
                });
            }
        }
    }

    /*
     * workaround to get getDefaultSharedPreferences(context) in any non-activity class
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
                .build();
        Log.d(TAG, "GoogleApiClient:" + mGoogleApiClient.toString());
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
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
        startActivityForResult(googleSignInIntent, RC_SIGN_IN);
    }

    private void googleSignOut(){
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.d(TAG, "Sign out status: " + status.toString());
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //An unresolvable error has occured and Google APIs (including Sign-in) will not be available.
        Log.d(TAG, "OnConnectionFailed:" + connectionResult.toString());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.google_sign_in_button:
                googleSignIn();
                break;
            case R.id.google_sign_out_button:
                googleSignOut();
                break;
            case R.id.continue_button:
                startActivity((new Intent(this, HomeActivity.class)));
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
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d(TAG, "GoogleSignInResult:" + result.getSignInAccount());
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result){
        Log.d(TAG, "result status message: " + result.getStatus().getStatusCode());
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        Log.d(TAG, "Result toString: " + result.toString());

        SharedPreferences.Editor editor = getSharedPreferences("USER_DATA", Context.MODE_PRIVATE).edit();
        if(result.isSuccess()) {
            //Sign in successfully, show authenticated UI.
            GoogleSignInAccount account = result.getSignInAccount();
            ((TextView) findViewById(R.id.account_status)).setText(getString(R.string.signed_in_fmt, account.getDisplayName()) + "\n" + account.getEmail());
            //((PosturizeUserInfo)getApplicationContext()).setUser(account);

            editor.putString("current_user", account.getEmail());
            editor.commit();
            updateUI(true);
        } else {
            //Signed out, show authenticated UI.
            editor.putString("current_user", "");
            editor.commit();
            updateUI(false);
        }
    }

    private void updateUI(boolean signedIn){
        if(signedIn) {
            findViewById(R.id.google_sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.remember_me).setVisibility(View.GONE);
            findViewById(R.id.google_sign_out_button).setVisibility(View.VISIBLE);
            findViewById(R.id.continue_button).setVisibility(View.VISIBLE);
        } else {
            ((TextView) findViewById(R.id.account_status)).setText(R.string.signed_out);

            findViewById(R.id.google_sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.remember_me).setVisibility(View.VISIBLE);
            findViewById(R.id.google_sign_out_button).setVisibility(View.GONE);
            findViewById(R.id.continue_button).setVisibility(View.GONE);
        }
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {

            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

