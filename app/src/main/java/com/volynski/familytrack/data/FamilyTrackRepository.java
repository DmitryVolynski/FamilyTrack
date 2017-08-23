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
import com.volynski.familytrack.data.models.firebase.GroupUser;
import com.volynski.familytrack.data.models.firebase.User;

/**
 * Created by DmitryVolynski on 17.08.2017.
 */

public class FamilyTrackRepository implements FamilyTrackDataSource {
    private static final String TAG = FamilyTrackRepository.class.getSimpleName();

    private FirebaseDatabase mFirebaseDatabase;
    private GoogleSignInAccount mGoogleSignInAccount;
    private FirebaseAuth mFirebaseAuth;

    public FamilyTrackRepository(GoogleSignInAccount account) {
        mGoogleSignInAccount = account;
    }

    /***
     *
     * @param acct
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
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
            //firebaseAuthWithGoogle(mGoogleSignInAccount);
            mFirebaseDatabase = FirebaseDatabase.getInstance();
        }
        return mFirebaseDatabase;
    }

    @Override
    public void createUser(@NonNull User user, final CreateUserCallback callback) {
        DatabaseReference ref = getFirebaseConnection().getReference("users");
        DatabaseReference newUserRef = ref.push();

        newUserRef.setValue(user);
        if (callback != null) {
            // TODO Check this. Don't like it
            user.setUserUuid(newUserRef.getKey());
            callback.onCreateUserCallback(new FirebaseResult<User>(user));
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
        DatabaseReference ref = getFirebaseConnection().getReference("users");
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
        DatabaseReference ref = getFirebaseConnection().getReference("users");
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
    public void createGroup(@NonNull final Group group, String adminUuid) {
        getUserByUuid(adminUuid, new GetUserByUuidCallback() {
            @Override
            public void onGetUserByUuidCompleted(FirebaseResult<User> result) {
                if (result.getData() != null) {
                    group.getMembers().put(result.getData().getUserUuid(),
                            new GroupUser(result.getData().getUserUuid(), User.ADMIN_ROLE, User.USER_JOINED));
                    DatabaseReference ref = getFirebaseConnection().getReference("groups");
                    ref.push().setValue(group);
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
}
