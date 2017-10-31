package edu.sjsu.posturize.posturize.users;

import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

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
