package edu.sjsu.posturize.posturize.users;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

/**
 * Created by Matt on 10/28/2017.
 */

public class PosturizeUserInfo {
    private static PosturizeUserInfo singleton;

    private String id = null;
    private String firstName = null;
    private String lastName = null;
    private String email = null;
    public boolean signingOut = false;

    private PosturizeUserInfo() {
    }

    public static PosturizeUserInfo getInstance() {
        if (singleton == null)
            singleton = new PosturizeUserInfo();
        return singleton;
    }

    public String getId() { return id; }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setUser(GoogleSignInAccount ga) {
        if (ga == null) {
            email = lastName = firstName = id = null;
        } else {
            id = ga.getId();
            email = ga.getEmail();
            lastName = ga.getFamilyName();
            firstName = ga.getGivenName();
        }
        signingOut = false;
    }

    public void signOut(){
        signingOut = true;
    }
}
