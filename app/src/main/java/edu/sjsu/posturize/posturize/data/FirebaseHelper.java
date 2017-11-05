package edu.sjsu.posturize.posturize.data;

import android.support.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by markbragg on 10/19/17.
 */

public class FirebaseHelper {
    private final String USERS = "users";
    private final String FIRST = "first";
    private final String LAST = "last";
    private final String USER_SLOUCHES = "user_slouches";

    private static FirebaseHelper instance;
    private FirebaseFirestore db;

    private FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public static FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    /**
     * adds a new user to Firestore
     * @param id the id to use to reference the user
     * @param firstName first name of the user
     * @param lastName last name of the user
     */
    public void addUserToFirestore(String id, String firstName, String lastName) {
        // Create a new user with a first, middle, and last name
        Map<String, Object> user = new HashMap<>();
        user.put(FIRST, firstName);
        user.put(LAST, lastName);

        DocumentReference userRef = db.collection(USERS).document(id);
        userRef.set(user);
    }

    /**
     * adds slouches to the
     * @param id
     * @param slouches
     */
    public void addSlouchesToFirestoreForUser(String id, HashMap<String, Object> slouches) {
        DocumentReference slouchesRef = db.collection(USER_SLOUCHES).document(id);
        if (slouchesExistForUser(id)) {
            slouchesRef.update(slouches);
        } else {
            slouchesRef.set(slouches);
        }
    }

    public DocumentReference getUser(String id) {
        return db.collection(USERS).document(id);
    }

    public DocumentReference getSlouchesForUser(String id) {
        return db.collection(USER_SLOUCHES).document(id);
    }

    public boolean slouchesExistForUser(String id) {
        final boolean[] result = new boolean[1];
        getSlouchesForUser(id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            result[0] = task.getResult().exists();
                        }
                    }
                });
        return result[0];
    }

    public boolean userExists(String id) {
        final boolean[] result = new boolean[1];
        getUser(id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    result[0] = task.getResult().exists();
                }
            }
        });
        return result[0];
    }
}
