package edu.sjsu.posturize.posturize.Users;

import android.content.SharedPreferences;

import java.io.Serializable;

/**
 * Created by Matt on 10/8/2017.
 */

public interface PosturizeUser extends Serializable {
    public boolean rememberMe();
    public void signIn();
    public void signOut();
}
