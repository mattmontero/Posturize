package edu.sjsu.posturize.posturize.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import edu.sjsu.posturize.posturize.users.PosturizeUserInfo;

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

    private static FirebaseHelper instance;
    private FirebaseFirestore db;
    private PosturizeUserInfo sUserInfo;

    // document references
//    final CollectionReference dailyAnalysisCollectionReference = db.collection(ANALYSIS).document(sUserInfo.getId()).collection(DAILY);

    private FirebaseHelper() {
        sUserInfo = PosturizeUserInfo.getInstance();
        db = FirebaseFirestore.getInstance();
        setFirestoreReferenceListeners();
    }

    public static FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    private void setFirestoreReferenceListeners() {
        setUserListener();
        setDailyAnalysisListener();
    }

    private void setUserListener() {
        final CollectionReference usersReference = db.collection(USERS);
        DocumentReference userDocumentReference = usersReference.document(sUserInfo.getId());
        userDocumentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    private void setDailyAnalysisListener() {
//        dailyAnalysisCollectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
//                if (e != null) {
//                    Log.w(TAG, "Listen failed.", e);
//                    return;
//                }
//
//                if (documentSnapshots != null && documentSnapshots.exists()) {
//                    Log.d(TAG, "Current data: " + documentSnapshots.getData());
//                } else {
//                    Log.d(TAG, "Current data: null");
//                }
//            }
//        });
    }

    /**
     * adds a new user to Firestore using PosturizeUserInfo properties
     */
    public void addUserToFirestore() {
        getUser(sUserInfo.getId()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().exists()) {
                            Map<String, Object> user = new HashMap<>();
                            user.put(FIRST, sUserInfo.getFirstName());
                            user.put(LAST, sUserInfo.getLastName());
                            user.put(EMAIL, sUserInfo.getEmail());

                            DocumentReference userRef = db.collection(USERS).document(sUserInfo.getId());
                            userRef.set(user);
                        }
                    }
                });
    }

    /**
     * adds slouches to the slouches collection, <userId> document
     * @param data
     */
    public void addSlouchesToFirestoreForUser(final HashMap<String, Object> data) {
        getSlouchesForUser().get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().exists()) {
                            DocumentReference slouchesRef = db.collection(USER_SLOUCHES).document(sUserInfo.getId());
                            slouchesRef.set(data);
                        }
                    }
                });
        getUser(sUserInfo.getId()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            Map<String, Object> user = new HashMap<>();
                            user.put(IS_SYNCED, true);

                            DocumentReference userRef = db.collection(USERS).document(sUserInfo.getId());
                            userRef.update(user);
                        }
                    }
                });
    }

    public DocumentReference getUser(String id) {
        return db.collection(USERS).document(id);
    }

    public DocumentReference getSlouchesForUser() {
        return db.collection(USER_SLOUCHES).document(sUserInfo.getId());
    }

    public boolean slouchesExistForUser() {
        final boolean[] result = new boolean[1];
        getSlouchesForUser().get()
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

    public String getDailyAnalysis() {
        final QuerySnapshot[] analysis = new QuerySnapshot[1];
        db.collection("analysis")
                .document(sUserInfo.getId())
                .collection("daily")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("daily analysis query", "success!");
                            analysis[0] = task.getResult();
                        }
                    }
                });
        return analysis[0].toString();
    }
}
