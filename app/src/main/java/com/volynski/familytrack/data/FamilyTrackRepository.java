package com.volynski.familytrack.data;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.Log;

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
import com.volynski.familytrack.data.models.firebase.HistoryItem;
import com.volynski.familytrack.data.models.firebase.Location;
import com.volynski.familytrack.data.models.firebase.Membership;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.data.models.firebase.Zone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
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
        Timber.v("firebaseAuthWithGooogle:" + idToken);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mFirebaseAuth = FirebaseAuth.getInstance();
        if (mFirebaseAuth.getCurrentUser() == null) {
            mFirebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Timber.v(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

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
                callback.onGetUserByUuidCompleted(new FirebaseResult<User>(databaseError));
            }
        });
    }

    @Override
    public void getUserByEmail(@NonNull String userEmail, @NonNull final GetUserByEmailCallback callback) {
        DatabaseReference ref = getFirebaseConnection().getReference(Group.REGISTERED_USERS_GROUP_KEY);
        Query query = ref.orderByChild("email").equalTo(userEmail).limitToFirst(1);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
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
                callback.onGetUserByEmailCompleted(new FirebaseResult<User>(databaseError));
            }
        });
    }

    @Override
    public void getUserByPhone(@NonNull String userPhone, @NonNull final GetUserByPhoneCallback callback) {
        DatabaseReference ref = getFirebaseConnection().getReference(Group.REGISTERED_USERS_GROUP_KEY);
        Query query = ref.orderByChild("phone").equalTo(userPhone).limitToFirst(1);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = null;
                if (dataSnapshot.getChildrenCount() > 0) {
                    user = FirebaseUtil.getUserFromSnapshot(dataSnapshot.getChildren().iterator().next());
                };
                callback.onGetUserByPhoneCompleted(new FirebaseResult<User>(user));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onGetUserByPhoneCompleted(new FirebaseResult<User>(databaseError));
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

                    /*
                    User newAdmin = result.getData().clone();
                    newAdmin.setLastKnownLocation(null);
                    */
                    User user = result.getData();
                    User newAdmin = new User(user.getUserUuid(), user.getFamilyName(), user.getGivenName(),
                            user.getDisplayName(), user.getPhotoUrl(), user.getEmail(), user.getPhone(), null,
                            (user.getLastKnownLocation() == null ? null : user.getLastKnownLocation().clone()));
                    Membership membership = new Membership(groupKey, group.getName(),
                            Membership.ROLE_ADMIN, Membership.USER_JOINED);
                    newAdmin.setMemberships(new HashMap<String, Membership>());
                    newAdmin.getMemberships().put(groupKey, membership);

                    user.addMembership(membership);
                    group.getMembers().put(newAdmin.getUserUuid(), newAdmin);

                    // create new group at /groups/groupKey and at /mUsers/userKey/group
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put(Group.REGISTERED_USERS_GROUP_KEY + "/" + user.getUserUuid(), user);
                    childUpdates.put("/groups/" + groupKey, group);
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
    public void removeUserFromGroup(@NonNull String groupUuid,
                                    @NonNull String userUuid,
                                    RemoveUserFromGroupCallback callback) {

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
                        "", "", "", null, null);
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

        // TODO удалить те контакты, которые уже получили приглашение (занесены в бд)
        FirebaseResult<List<User>> result = new FirebaseResult<List<User>>(users);
        callback.onGetContactsToInviteCompleted(result);
    }

    @Override
    public void inviteUsers(@NonNull final String groupUuid,
                            @NonNull final List<User> usersToinvite,
                            @NonNull final InviteUsersCallback callback) {
        // check that group exists
        getGroupByUuid(groupUuid, new GetGroupByUuidCallback() {
            @Override
            public void onGetGroupByUuidCompleted(FirebaseResult<Group> result) {
                if (result.getData() == null) {
                    // group not found
                    callback.onInviteUsersCompleted(
                            new FirebaseResult<String>(FamilyTrackException.getInstance(mContext, FamilyTrackException.DB_GROUP_NOT_FOUND)));
                    return;
                }

                for (User user : usersToinvite) {
                    inviteUser(user, result.getData());
                }
            }
        });
    }

    private void inviteUser(final User user, final Group group) {
        if (isUserAlreadyInvited(user, group)) {
            return;
        }

        // проверяем логинился ли такой пользователь в системе
        final Map<String, Object> childUpdates = new HashMap<>();
        getUserByPhone(user.getPhone(), new GetUserByPhoneCallback() {
            @Override
            public void onGetUserByPhoneCompleted(FirebaseResult<User> result) {
                User dbUser = result.getData();
                if (dbUser == null) {
                    // пользователь не найден, вначале создаем пользователя в ветке registered_users
                    user.addMembership(new Membership(group.getGroupUuid(), group.getName(), Membership.ROLE_UNDEFINED, Membership.USER_INVITED));
                    user.setUserUuid(getFirebaseConnection().getReference(Group.REGISTERED_USERS_GROUP_KEY).push().getKey());
                    childUpdates.put(Group.REGISTERED_USERS_GROUP_KEY + "/" + user.getUserUuid(), user);

                    group.addUser(user);
                    childUpdates.put("/groups/" + group.getGroupUuid(), group);
                } else {
                    // в системе найден зарегистрированный пользователь, но он не приглашен в группу

                }
                getFirebaseConnection().getReference().updateChildren(childUpdates);
            }
        });
    }

    /**
     * checks if user already is member of group
     * @param user - user to check
     * @param group - group of mUsers from firebase wich we want to check against membership of user
     * @return true if user already in group
     */
    private boolean isUserAlreadyInvited(User user, Group group) {
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

    @Override
    public void getGroupsAvailableToJoin(@NonNull String phoneNumber,
                                         final @NonNull GetGroupsAvailableToJoinCallback callback) {
        this.getUserByPhone(phoneNumber, new GetUserByPhoneCallback() {
            @Override
            public void onGetUserByPhoneCompleted(FirebaseResult<User> result) {
                checkObject(result);
                if (result.getException() != null) {
                    callback.onGetGroupsAvailableToJoinCompleted(new FirebaseResult<List<Group>>(result.getException()));
                }
                List<Group> groups = new ArrayList<Group>();
                if (result.getData() != null) {
                    if (result.getData().getMemberships() != null) {
                        for (String key : result.getData().getMemberships().keySet()) {
                            Membership membership = result.getData().getMemberships().get(key);
                            if (membership.getStatusId() == Membership.USER_INVITED ||
                                    membership.getStatusId() == Membership.USER_DEPARTED)
                                groups.add(new Group(membership.getGroupUuid(), membership.getGroupName()));
                        }
                    }
                }
                callback.onGetGroupsAvailableToJoinCompleted(new FirebaseResult<List<Group>>(groups));
            }
        });
    }

    @Override
    public void addUserToGroup(@NonNull String userUuid,
                               @NonNull String groupUuid,
                               final AddUserToGroupCallback callback) {
        // для перевода пользователя в состояние MEMEMBER нам необходимо:
        //      - изменить статус пользователя в ветке registered_users/<user_key>
        //      - изменить статус пользователя в ветке groups/<group_key>/members/<user_key>/memberships/<group_key>
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(FamilyTrackDbRefsHelper.groupOfUserRef(userUuid, groupUuid) + User.FIELD_ROLE_ID, Membership.ROLE_MEMBER);
        childUpdates.put(FamilyTrackDbRefsHelper.groupOfUserRef(userUuid, groupUuid) + User.FIELD_STATUS_ID, Membership.USER_JOINED);

        childUpdates.put(FamilyTrackDbRefsHelper.userMembershipRef(userUuid, groupUuid) + User.FIELD_ROLE_ID, Membership.ROLE_MEMBER);
        childUpdates.put(FamilyTrackDbRefsHelper.userMembershipRef(userUuid, groupUuid) + User.FIELD_STATUS_ID, Membership.USER_JOINED);

        mFirebaseDatabase.getReference()
                .updateChildren(childUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (callback != null) {
                            callback.onAddUserToGroupCompleted(new FirebaseResult<String>("Ok"));
                        }
                    }
                });
    }

    @Override
    public void updateUserLocation(@NonNull final String userUuid,
                                   @NonNull final Location location,
                                   @NonNull final UpdateUserLocationCallback callback) {
        Timber.v("Started");
        getUserByUuid(userUuid, new GetUserByUuidCallback() {
            @Override
            public void onGetUserByUuidCompleted(FirebaseResult<User> result) {
                // check if user exists
                User user = result.getData();
                if (user == null) {
                    callback.onUpdateUserLocationCompleted(
                            new FirebaseResult<String>(FamilyTrackException.getInstance(mContext,
                                    FamilyTrackException.DB_USER_BY_UUID_NOT_FOUND)));
                    return;
                }

                // now prepare data for updates
                // we should update user location in three nodes:
                // /groups/<groupUuid>/members/<userUuid> - for active group only!
                // /registered_users/<userUuid>
                // /history/<userUuid>/<new location>
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(FamilyTrackDbRefsHelper.userRef(userUuid) + User.FIELD_LAST_KNOWN_LOCATION, location);
                if (user.getActiveMembership() != null) {
                    childUpdates.put(FamilyTrackDbRefsHelper.userOfGroupRef(
                            user.getActiveMembership().getGroupUuid(), userUuid) + User.FIELD_LAST_KNOWN_LOCATION, location);
                }

                String historyItemPath = FamilyTrackDbRefsHelper.userHistory(userUuid);
                DatabaseReference ref = getFirebaseConnection().getReference(historyItemPath);
                String historyItemKey = ref.push().getKey();

                childUpdates.put(historyItemPath + "/" + historyItemKey, location);
                mFirebaseDatabase.getReference()
                        .updateChildren(childUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (callback != null) {
                                    callback.onUpdateUserLocationCompleted(
                                            new FirebaseResult<String>("Ok"));
                                }
                            }
                        });
            }
        });
    }

    @Override
    public void getUserTrack(@NonNull String userUuid,
                             @NonNull long periodStart,
                             @NonNull long periodEnd,
                             final @NonNull GetUserTrackCallback callback) {
        String historyItemPath = FamilyTrackDbRefsHelper.userHistory(userUuid);
        DatabaseReference ref = getFirebaseConnection().getReference(historyItemPath);

        Query query = ref.orderByChild("timestamp").startAt(periodStart).endAt(periodEnd);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Location> locations = new ArrayList<Location>();
                if (dataSnapshot.getChildren() != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        locations.add(FirebaseUtil.getLocationFromSnapshot(snapshot));
                    }
                }
                callback.onGetUserTrackCompleted(new FirebaseResult<List<Location>>(locations));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    // --------------------------------------------------------------------------------------------
    //
    // zones operations
    //
    // --------------------------------------------------------------------------------------------

    @Override
    public void createZone(@NonNull String groupUuid,
                           @NonNull Zone zone,
                           CreateZoneCallback callback) {
        DatabaseReference ref = getFirebaseConnection()
                .getReference(FamilyTrackDbRefsHelper.zonesOfGroup(groupUuid));
        DatabaseReference newRec = ref.push();

        newRec.setValue(zone);
        if (callback != null) {
            callback.onCreateZoneCompleted(new FirebaseResult<String>(newRec.getKey()));
        }

    }

    @Override
    public void updateZone(@NonNull String grupUuid,
                           @NonNull Zone zone,
                           UpdateZoneCallback callback) {

    }
}
