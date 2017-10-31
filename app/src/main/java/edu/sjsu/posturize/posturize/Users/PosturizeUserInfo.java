package edu.sjsu.posturize.posturize.Users;

import android.app.Application;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

/**
 * Created by Matt on 10/28/2017.
 */

public class PosturizeUserInfo extends Application{
    private String firstName = null;
    private String lastName = null;
    private String email = null;

    public String getFirstName(){
        return firstName;
    }

    public String getLastName(){
        return lastName;
    }

    public String getEmail(){
        return email;
    }

    public void setUser(GoogleSignInAccount ga){
        email = ga.getEmail();
        lastName = ga.getFamilyName();
        firstName = ga.getGivenName();
    }

}
