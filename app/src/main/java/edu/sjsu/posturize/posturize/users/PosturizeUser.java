package edu.sjsu.posturize.posturize.users;

import java.io.Serializable;

/**
 * Created by Matt on 10/8/2017.
 */

public interface PosturizeUser extends Serializable {
    public boolean rememberMe();
    public void signIn();
    public void signOut();
}
