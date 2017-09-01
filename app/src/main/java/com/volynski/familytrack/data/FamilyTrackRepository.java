package com.volynski.familytrack.data;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 17.08.2017.
 */

public class FamilyTrackRepository implements FamilyTrackDataSource {
    private static final String TAG = FamilyTrackRepository.class.getSimpleName();

    // column list for the contacts provider
    /**
    private final static String[] CONTACTS_PROJECTION = {
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
            ContactsContract.Contacts.PHOTO_URI,
            ContactsContract.Contacts.
    };
     */

    private FirebaseDatabase mFirebaseDatabase;
    private String mGoogleAccountIdToken;
    private FirebaseAuth mFirebaseAuth;
    private Context mContext;

    // TODO проверить необходимость передачи GoogleSignInAccount в конструктор
    public FamilyTrackRepository(String googleAccountIdToken, Context context) {
        Timber.v("FamilyTrackRepository created with idToken=" + googleAccountIdToken);
        mGoogleAccountIdToken = googleAccountIdToken;
        mContext = context.getApplicationContext();
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

    /**
     * just to debug anonymous methods
     * @param o
     */
    private void checkObject(Object o) {
        int i = 0;
    }

    @Override
    public void getContactsToInvite(@NonNull GetContactsToInvite callback) {
        User user = null;
        // read contacts from ContactsContract.Data.CONTENT_URI
        Cursor cursor = mContext.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                null,
                ContactsContract.Data.HAS_PHONE_NUMBER + "!=0 AND (" + ContactsContract.Data.MIMETYPE + "=? OR " + ContactsContract.Data.MIMETYPE + "=?)",
                new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE},
                ContactsContract.Data.CONTACT_ID);

        // merge same contacts with emails & phone numbers
        Map<String, User> contacts = new HashMap<>();
        while (cursor.moveToNext()) {
            String key = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID));
            boolean isNew = !contacts.containsKey(key);

            if (isNew) {
                String givenName = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                user = new User("", "", "", cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)),
                        "", "", "", User.ROLE_UNDEFINED, User.USER_CREATED, "", null);
            } else {
                user = contacts.get(key);
            }

            String mimeType = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));
            String data = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA1));
            if (mimeType.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                user.setEmail(data);
            } else {
                user.setPhone(data);
            }

            if (isNew) {
                contacts.put(key, user);
            } else {
                contacts.replace(key, user);
            }
        }

        // select contacts that have phone number and email both
        List<User> users = new ArrayList<>();
        for (String key : contacts.keySet()) {
            User u = contacts.get(key);
            if (!u.getPhone().equals("") && !u.getEmail().equals("")) {
                users.add(u);
            }
        }
        FirebaseResult<List<User>> result = new FirebaseResult<List<User>>(users);
        callback.onGetContactsToInviteCompleted(result);
    }

    @Override
    public void inviteContacts(@NonNull final String groupUuid,
                               @NonNull final List<User> usersToinvite,
                               @NonNull final InviteContactsCallback callback) {
        // check that group exists
        getGroupByUuid(groupUuid, new GetGroupByUuidCallback() {
            @Override
            public void onGetGroupByUuidCompleted(FirebaseResult<Group> result) {
                if (result.getData() == null) {
                    // group not found
                    callback.onInviteContactsCompleted(
                            new FirebaseResult<String>(FamilyTrackException.getInstance(mContext, FamilyTrackException.DB_GROUP_NOT_FOUND)));
                    return;
                }

                DatabaseReference ref = getFirebaseConnection().getReference("groups/" + groupUuid + "/members");
                for (User user : usersToinvite) {
                    if (!isAlreadyInGroup(user, result.getData())) {
                        user.setStatusId(User.USER_INVITED);
                        user.setRoleId(User.ROLE_UNDEFINED);
                        ref.push().setValue(user);
                    }
                }
            }
        });
    }

    /**
     * checks if user already is member of group
     * @param user - user to check
     * @param group - group of users from firebase wich we want to check against membership of user
     * @return true if user already in group
     */
    private boolean isAlreadyInGroup(User user, Group group) {
        boolean result = false;
        for (String key : group.getMembers().keySet()) {
            User groupUser = group.getMembers().get(key);
            if (user.getPhone().equals(groupUser.getPhone()) &&
                user.getEmail().equals(groupUser.getEmail())) {
                result = true;
                break;
            }
        }
        return result;
    }
}
