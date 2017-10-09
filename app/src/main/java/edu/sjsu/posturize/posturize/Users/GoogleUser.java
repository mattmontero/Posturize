package edu.sjsu.posturize.posturize.Users;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import edu.sjsu.posturize.posturize.R;

/**
 * Created by Matt on 10/8/2017.
 */

public class GoogleUser implements PosturizeUser,
        GoogleApiClient.OnConnectionFailedListener{

    private final String TAG = "Google User";
    private GoogleApiClient mGoogleApiClient;

    public GoogleUser(GoogleApiClient gApiClient){
        mGoogleApiClient = gApiClient;
    }

    @Override
    public void signIn() {

    }

    @Override
    public void signOut() {

    }

    @Override
    public boolean rememberMe() {
        return false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
