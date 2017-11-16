package edu.sjsu.posturize.posturize.users;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

/**
 * Created by Matt on 10/28/2017.
 */

public class GoogleAccountInfo {
    private static GoogleAccountInfo singleton;

    private String id = null;
    private String firstName = null;
    private String lastName = null;
    private String email = null;
    public static boolean signingOut = false;

    /************************ Singleton ************************/
    private GoogleAccountInfo() {
    }

    public static GoogleAccountInfo getInstance() {
        if (singleton == null)
            singleton = new GoogleAccountInfo();
        return singleton;
    }


    /**
     *
     * @return account id
     */
    public String getId() { return id; }

    /**
     *
     * @return account first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     *
     * @return account last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     *
     * @return account email
     */
    public String getEmail() {
        return email;
    }

    /**
     *
     * @param ga google account with user properties
     */
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
