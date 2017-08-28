package com.volynski.familytrack.data;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.User;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 17.08.2017.
 */

public class FamilyTrackRepository implements FamilyTrackDataSource {
    private static final String TAG = FamilyTrackRepository.class.getSimpleName();

    private FirebaseDatabase mFirebaseDatabase;
    private String mGoogleAccountIdToken;
    private FirebaseAuth mFirebaseAuth;

    // TODO проверить необходимость передачи GoogleSignInAccount в конструктор
    public FamilyTrackRepository(String googleAccountIdToken) {
        Timber.v("FamilyTrackRepository created with idToken=" + googleAccountIdToken);
        mGoogleAccountIdToken = googleAccountIdToken;
    }

    private void firebaseAuthWithGoogle(String idToken) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + idToken);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mFirebaseAuth = FirebaseAuth.getInstance();
        if (mFirebaseAuth.getCurrentUser() == null) {
            mFirebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "signInWithCredential", task.getException());
                            }
                        }
                    });
        }
    }

    private FirebaseDatabase getFirebaseConnection() {
        if (mFirebaseDatabase == null) {
            firebaseAuthWithGoogle(mGoogleAccountIdToken);
            mFirebaseDatabase = FirebaseDatabase.getInstance();
        }
        return mFirebaseDatabase;
    }

    @Override
    public void createUser(@NonNull User user, final CreateUserCallback callback) {

        DatabaseReference ref = getFirebaseConnection().getReference(Group.REGISTERED_USERS_GROUP_KEY);
        DatabaseReference newUserRef = ref.push();

        newUserRef.setValue(user);
        if (callback != null) {
            // TODO Check this. Don't like it
            user.setUserUuid(newUserRef.getKey());
            callback.onCreateUserCompleted(new FirebaseResult<User>(user));
        }
    }

    @Override
    public void updateUser(@NonNull User user) {

    }

    @Override
    public void changeUserStatus(@NonNull String userUuid, @NonNull int newStatus) {

    }

    @Override
    public void getUserByUuid(@NonNull String userUuid, @NonNull final GetUserByUuidCallback callback) {
        DatabaseReference ref = getFirebaseConnection().getReference(Group.REGISTERED_USERS_GROUP_KEY);
        Query query = ref.orderByKey().equalTo(userUuid).limitToFirst(1);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = null;
                if (dataSnapshot.getChildrenCount() > 0) {
                    user = FirebaseUtil.getUserFromSnapshot(dataSnapshot.getChildren().iterator().next());
                };
                callback.onGetUserByUuidCompleted(new FirebaseResult<User>(user));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void getUserByEmail(@NonNull String userEmail, @NonNull final GetUserByEmailCallback callback) {
        DatabaseReference ref = getFirebaseConnection().getReference(Group.REGISTERED_USERS_GROUP_KEY);
        Query query = ref.orderByChild("email").equalTo(userEmail).limitToFirst(1);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = null;
                if (dataSnapshot.getChildrenCount() > 0) {
                    user = FirebaseUtil.getUserFromSnapshot(dataSnapshot.getChildren().iterator().next());
                };
                callback.onGetUserByEmailCompleted(new FirebaseResult<User>(user));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void createGroup(@NonNull final Group group, String adminUuid, final CreateGroupCallback callback) {
        getUserByUuid(adminUuid, new GetUserByUuidCallback() {
            @Override
            public void onGetUserByUuidCompleted(FirebaseResult<User> result) {
                if (result.getData() != null) {
                    DatabaseReference ref = getFirebaseConnection().getReference("groups");
                    String groupKey = ref.push().getKey();
                    group.setGroupUuid(groupKey);

                    User newAdmin = result.getData().clone();
                    newAdmin.setRoleId(User.ROLE_ADMIN);
                    newAdmin.setStatusId(User.USER_JOINED);
                    newAdmin.setGroupUuid(groupKey);
                    newAdmin.setLastKnownLocation(null);

                    group.getMembers().put(newAdmin.getUserUuid(), newAdmin);


                    // create new group at /groups/groupKey and at /users/userKey/group
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/groups/" + groupKey, group);
                    childUpdates.put(Group.REGISTERED_USERS_GROUP_KEY + "/" + newAdmin.getUserUuid(), newAdmin);
                    mFirebaseDatabase.getReference()
                            .updateChildren(childUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (callback != null) {
                                callback.onCreateGroupCompleted(new FirebaseResult<Group>(group));
                            }
                        }
                    });
                } else {
                    Timber.v("null");
                }
            }
        });
    }

    @Override
    public void addUser(@NonNull String groupUuid, @NonNull String userUuid) {

    }

    @Override
    public void removeUser(@NonNull String groupUuid, @NonNull String userUuid) {

    }


    @Override
    public void getGroupByUuid(@NonNull String groupUuid, @NonNull final GetGroupByUuidCallback callback) {
        DatabaseReference ref = getFirebaseConnection().getReference("groups");
        Query query = ref.orderByKey().equalTo(groupUuid).limitToFirst(1);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get Group object with all members
                Group group = FirebaseUtil.getGroupFromSnapshot(dataSnapshot.getChildren().iterator().next());
                callback.onGetGroupByUuidCompleted(new FirebaseResult<Group>(group));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkObject(Object o) {
        int i = 0;
    }
}
