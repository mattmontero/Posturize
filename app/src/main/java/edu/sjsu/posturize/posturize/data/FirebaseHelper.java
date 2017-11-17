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

import java.util.ArrayList;
import java.util.Date;
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
    private final String TIMES = "times";

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
                            if (snapshot != null && snapshot.exists()) {
                                ArrayList<String> analysisList = (ArrayList<String>) snapshot.getData().get("daily");
                                if(analysisList != null && !analysisList.isEmpty())
                                    DailyUpdateActivity.setAnalysis(analysisList.get(0));
                                else
                                    DailyUpdateActivity.setAnalysis("NO ANALYSIS FOUND");
                            }
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
                            if (task.isSuccessful() && !task.getResult().exists()) {
                                setUser(userMap(false));
                                setAnalysis(analysisMap(new ArrayList<String>()));
                                setSlouches(slouchMap(new ArrayList<Date>(), new ArrayList<Double>()));
                            }
                        }
                    });
        }
    }

    /**
     * adds slouches to the slouches collection, <userId> document
     * @param data slouch data for user from sqlite
     */
    public void addSlouchesToFirestoreForUser(final String id, final HashMap<String, Object> data) {
        Log.i("DAILY SYNC", id);
        //if(isSignedIn()){
        getUserSlouchesTask(id).addOnCompleteListener(
                new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult().exists())
                            updateSlouches(id, data);
                    }
                });
        getUserTask(id).addOnCompleteListener(
                new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult().exists())
                            updateUser(id, userMap(true));
                    }
                });
        //}
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

    public DocumentReference getUserReference(String id) {
        return firestore
                .collection(USERS)
                .document(id);
    }

    /**
     *
     * @return userTask
     */
    private Task<DocumentSnapshot> getUserTask() {
        return getUserReference().get();
    }
    private Task<DocumentSnapshot> getUserTask(String id) {
        return getUserReference(id).get();
    }

    /**
     *
     * @return userSlouchesReference
     */
    private DocumentReference getUserSlouchesReference() {
        return firestore
                .collection(USER_SLOUCHES)
                .document(ID());
    }
    private DocumentReference getUserSlouchesReference(String id) {
        return firestore
                .collection(USER_SLOUCHES)
                .document(id);
    }

    /**
     *
     * @return userSlouchesTask
     */
    public Task<DocumentSnapshot> getUserSlouchesTask() {
        return getUserSlouchesReference().get();
    }
    public Task<DocumentSnapshot> getUserSlouchesTask(String id) {
        return getUserSlouchesReference(id).get();
    }
    /**
     *
     * @return document reference for current daily analysis from firestore
     */
    private DocumentReference getCurrentDailyAnalysisReference() {
        return firestore
                .collection(ANALYSIS)
                .document(ID());
    }
    /*********************************************************************************/




    /*************************** FIRESTORE SETTERS ***************************/
    /**
     * sets user in firestore
     * @param data map to set user to
     */
    private void setUser(Map<String, Object> data) {
        this.setUser(ID(), data);
    }

    private void setUser(String id, Map<String, Object> data) {
        firestore
                .collection(USERS)
                .document(id)
                .set(data);
    }

    private void updateUser( Map<String, Object> data){
        this.updateUser(ID(), data);
    }

    private void updateUser(String id, Map<String, Object> data) {
        firestore
                .collection(USERS)
                .document(id)
                .update(data);
    }

    private void setAnalysis(Map<String, Object> data){
        this.setAnalysis(ID(), data);
    }

    private void setAnalysis(String id, Map<String, Object> data) {
        firestore
                .collection(ANALYSIS)
                .document(id)
                .set(data);
    }

    private void setSlouches(Map<String, Object> data){
        this.setSlouches(ID(), data);
    }

    private void setSlouches(String id, Map<String, Object> data) {
        firestore
                .collection(USER_SLOUCHES)
                .document(id)
                .set(data);
    }

    private void updateSlouches(Map<String, Object> data){
        this.updateSlouches(ID(), data);
    }

    private void updateSlouches(String id, Map<String, Object> data) {
        firestore
                .collection(USER_SLOUCHES)
                .document(id)
                .update(data);
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
        user.put(IS_SYNCED, synced);
        if(!synced) {
            user.put(FIRST, sUserInfo.getFirstName());
            user.put(LAST, sUserInfo.getLastName());
            user.put(EMAIL, sUserInfo.getEmail());
        }
        return user;
    }


    /**
     * creates a user map from GoogleAccountInfo
     * @param synced if true put key "issynced" in map and set value to true
     * @return user map
     */
    private Map<String, Object> analysisMap(ArrayList<String> data) {
        Map<String, Object> map = new HashMap<>();
        map.put(DAILY, data);
        return map;
    }


    /**
     * creates a user map from GoogleAccountInfo
     * @param synced if true put key "issynced" in map and set value to true
     * @return user map
     */
    private Map<String, Object> slouchMap(ArrayList<Date> times, ArrayList<Double> slouches) {
        Map<String, Object> map = new HashMap<>();
        map.put(USER_SLOUCHES, slouches);
        map.put(TIMES, slouches);
        return map;
    }
}
