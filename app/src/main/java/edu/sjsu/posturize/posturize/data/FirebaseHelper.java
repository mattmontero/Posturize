package edu.sjsu.posturize.posturize.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

import edu.sjsu.posturize.posturize.notifications.reminder.DailyUpdateActivity;
import edu.sjsu.posturize.posturize.users.GoogleAccountInfo;

import static android.content.ContentValues.TAG;

/**
 * Created by markbragg on 10/19/17.
 */

public class FirebaseHelper {
    private final String USERS = "users";
    private final String FIRST = "first";
    private final String LAST = "last";
    private final String USER_SLOUCHES = "slouches";
    private final String EMAIL = "email";
    private final String ANALYSIS = "analysis";
    private final String DAILY = "daily";
    private final String IS_SYNCED = "isSynced";
    private final String CURRENT = "current";

    private static FirebaseHelper instance;
    private FirebaseFirestore firestore;
    private GoogleAccountInfo sUserInfo;

    private FirebaseHelper() {
        sUserInfo = GoogleAccountInfo.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public static FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    public void setFirestoreReferenceListeners() {
        setUserListener();
        setDailyAnalysisListener();
    }

    private void setUserListener() {
        if (isSignedIn()) {

            getUserReference().addSnapshotListener(
                    new EventListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot snapshot,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen failed.", e);
                                return;
                            }

                            if (snapshot != null && snapshot.exists()) {
                                Log.d(TAG, "Snapshot listener -> Current data: " + snapshot.getData());
                            } else {
                                Log.d(TAG, "Current data: null");
                            }
                        }
                    });
        }
    }

    private void setDailyAnalysisListener() {
        if (isSignedIn()) {
            getCurrentDailyAnalysisReference().addSnapshotListener(
                    new EventListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot snapshot,
                                            @Nullable FirebaseFirestoreException e) {
                            if (snapshot != null && snapshot.exists())
                                DailyUpdateActivity.setAnalysis((String) snapshot.getData().get("analysis"));
                        }
                    });
        }
    }

    /**
     * adds a new user to Firestore using GoogleAccountInfo properties after they log in
     */
    public void addUserToFirestore() {
        if (isSignedIn()) {
            getUserTask().addOnCompleteListener(
                    new OnCompleteListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && !task.getResult().exists())
                                setUser(userMap(false));
                        }
                    });
        }
    }

    /**
     * adds slouches to the slouches collection, <userId> document
     * @param data slouch data for user from sqlite
     */
    public void addSlouchesToFirestoreForUser(final HashMap<String, Object> data) {
        if (isSignedIn()) {
            getUserSlouchesTask().addOnCompleteListener(
                    new OnCompleteListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && !task.getResult().exists())
                                setUser(data);
                        }
                    });
            getUserTask().addOnCompleteListener(
                    new OnCompleteListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult().exists())
                                setUser(userMap(true));
                        }
                    });
        }
    }



    /*************************** FIRESTORE GETTERS ***************************/
    /**
     *
     * @return userReference
     */
    public DocumentReference getUserReference() {
        return firestore
                .collection(USERS)
                .document(sUserInfo.getId());
    }

    /**
     *
     * @return userTask
     */
    private Task<DocumentSnapshot> getUserTask() {
        return getUserReference().get();
    }

    /**
     *
     * @return userSlouchesReference
     */
    private DocumentReference getUserSlouchesReference() {
        return firestore
                .collection(USER_SLOUCHES)
                .document(sUserInfo.getId());
    }

    /**
     *
     * @return userSlouchesTask
     */
    public Task<DocumentSnapshot> getUserSlouchesTask() {
        return getUserSlouchesReference().get();
    }

    /**
     *
     * @return document reference for current daily analysis from firestore
     */
    private DocumentReference getCurrentDailyAnalysisReference() {
        return firestore.
                collection(ANALYSIS).
                document(ID()).
                collection(DAILY).
                document(CURRENT);
    }
    /*********************************************************************************/




    /*************************** FIRESTORE SETTERS ***************************/
    /**
     * sets user in firestore
     * @param data map to set user to
     */
    private void setUser(Map<String, Object> data) {
        firestore
                .collection(USER_SLOUCHES)
                .document(ID())
                .set(data);
    }
    /*********************************************************************************/




    /*************************** POSTURIZE USER INFO ***************************/
    /**
     *
     * @return true is user is signed into google account
     */
    private boolean isSignedIn() {
        // TODO: extend to every possible login, may need to be done in GoogleAccountInfo.java
        return sUserInfo != null && !GoogleAccountInfo.signingOut;
    }

    /**
     *
     * @return user id
     */
    private String ID() {
        return sUserInfo.getId();
    }
    /*********************************************************************************/




    /**
     * creates a user map from GoogleAccountInfo
     * @param synced if true put key "issynced" in map and set value to true
     * @return user map
     */
    private Map<String, Object> userMap(boolean synced) {
        Map<String, Object> user = new HashMap<>();
        user.put(FIRST, sUserInfo.getFirstName());
        user.put(LAST, sUserInfo.getLastName());
        user.put(EMAIL, sUserInfo.getEmail());
        if (synced) user.put(IS_SYNCED, synced);
        return user;
    }


}
