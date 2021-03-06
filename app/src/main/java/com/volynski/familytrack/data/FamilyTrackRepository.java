package com.volynski.familytrack.data;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.volynski.familytrack.R;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.data.models.firebase.GeofenceEvent;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.Location;
import com.volynski.familytrack.data.models.firebase.Membership;
import com.volynski.familytrack.data.models.firebase.Settings;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.data.models.firebase.Zone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 17.08.2017.
 */

public class FamilyTrackRepository implements FamilyTrackDataSource {
    private static final String TAG = FamilyTrackRepository.class.getSimpleName();

    private FirebaseDatabase mFirebaseDatabase;
    private String mGoogleAccountIdToken;
    private FirebaseAuth mFirebaseAuth;
    private Context mContext;

    // TODO проверить необходимость передачи GoogleSignInAccount в конструктор
    public FamilyTrackRepository(String googleAccountIdToken, Context context) {
        mGoogleAccountIdToken = googleAccountIdToken;
        mContext = context.getApplicationContext();
    }

    private void firebaseAuthWithGoogle(String idToken) {
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
                                Timber.e("signInWithCredential failed", task.getException());
                            }
                        }
                    });
        }
    }

    @Override
    public FirebaseDatabase getFirebaseConnection() {
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
            user.setUserUuid(newUserRef.getKey());
            callback.onCreateUserCompleted(new FirebaseResult<>(user));
        }
    }

    @Override
    public void updateUser(@NonNull final User user, final UpdateUserCallback callback) {
        getUserGroups(user.getUserUuid(), new GetUserGroupsCallback() {
            @Override
            public void onGetUserGroupsCompleted(FirebaseResult<List<Group>> result) {
                Map<String, Object> childUpdates = new HashMap<>();

                childUpdates.put(FamilyTrackDbRefsHelper.userRef(user.getUserUuid()), user);

                if (result.getData() != null) {
                    for (Group group : result.getData()) {
                        childUpdates.put(FamilyTrackDbRefsHelper
                                        .userOfGroupRef(group.getGroupUuid(), user.getUserUuid()),
                                user.cloneForGroupNode(group.getGroupUuid()));
                    }
                }

                mFirebaseDatabase.getReference()
                        .updateChildren(childUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (callback != null) {
                                    callback.onUpdateUserCompleted(new FirebaseResult<>(FirebaseResult.RESULT_OK));
                                }
                            }
                        });
            }
        });
    }

    @Override
    public void changeUserStatus(@NonNull String userUuid, int newStatus) {

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
                }
                callback.onGetUserByUuidCompleted(new FirebaseResult<>(user));
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

        Query query = ref.orderByChild(User.FIELD_EMAIL).equalTo(userEmail).limitToFirst(1);

        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = null;
                if (dataSnapshot.getChildrenCount() > 0) {
                    user = FirebaseUtil.getUserFromSnapshot(dataSnapshot.getChildren().iterator().next());
                }
                callback.onGetUserByEmailCompleted(new FirebaseResult<>(user));
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
        Query query = ref.orderByChild(User.FIELD_PHONE).equalTo(userPhone).limitToFirst(1);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = null;
                if (dataSnapshot.getChildrenCount() > 0) {
                    user = FirebaseUtil.getUserFromSnapshot(dataSnapshot.getChildren().iterator().next());
                }
                callback.onGetUserByPhoneCompleted(new FirebaseResult<>(user));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onGetUserByPhoneCompleted(new FirebaseResult<User>(databaseError));
            }
        });
    }

    @Override
    public void createGroup(@NonNull final Group group, String adminUuid, final CreateGroupCallback callback) {
        // что необходимо сделать при создании новой группы
        //      - исключить пользователя из текущей активной группы (заменить статус на USER_DEPARTED)
        //      - создать новую группу
        //      - включить туда пользователя в роли ROLE_ADMIN и статусе USER_JOINED
        // Эти изменения делаются в двух местах -
        //      1. groups/<groupUuid>/members/<userUuid>/memberships/<groupUuid>/statusId <- USER_DEPARTED
        //      2. registered_users/<userUuid>/memberships/<groupUuid>/statusId <- USER_DEPARTED
        //      3. то же самое для новой группы, но со статусом USER_JOINED - в тех же двух местах
        getUserByUuid(adminUuid, new GetUserByUuidCallback() {
            @Override
            public void onGetUserByUuidCompleted(FirebaseResult<User> result) {
                String currentGroupUuid = null;
                if (result.getData() != null) {
                    User currentUser = result.getData();

                    // если пользователь не входит в группу, то исключать его не нужно
                    if (currentUser.getActiveMembership() != null) {
                        currentGroupUuid = currentUser.getActiveMembership().getGroupUuid();
                    }

                    DatabaseReference ref = getFirebaseConnection()
                            .getReference(FamilyTrackDbRefsHelper.NODE_GROUPS);
                    String newGroupKey = ref.push().getKey();

                    Membership newMembership = new Membership(newGroupKey, group.getName(),
                            Membership.ROLE_ADMIN, Membership.USER_JOINED);

                    currentUser.setMemberships(new HashMap<String, Membership>());
                    currentUser.addMembership(newMembership);
                    group.setGroupUuid(newGroupKey);
                    group.addMember(currentUser);

                    DatabaseReference newGroupRef = mFirebaseDatabase
                            .getReference(FamilyTrackDbRefsHelper.groupRef(newGroupKey));
                    newGroupRef.setValue(group);


                    Map<String, Object> childUpdates = new HashMap<>();
                    if (currentGroupUuid != null) {
                        childUpdates.put(FamilyTrackDbRefsHelper
                                .userMembershipRef(currentUser.getUserUuid(), currentGroupUuid) +
                                Membership.FIELD_STATUS_ID, Membership.USER_DEPARTED);
                        childUpdates.put(FamilyTrackDbRefsHelper
                                .groupOfUserRef(currentUser.getUserUuid(), currentGroupUuid) +
                                Membership.FIELD_STATUS_ID, Membership.USER_DEPARTED);
                    }
                    childUpdates.put(FamilyTrackDbRefsHelper
                                    .groupOfUserRef(currentUser.getUserUuid(), newGroupKey),
                            newMembership);

                    mFirebaseDatabase.getReference()
                            .updateChildren(childUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (callback != null) {
                                        callback.onCreateGroupCompleted(new FirebaseResult<>(group));
                                    }
                                }
                            });

                } else {
                    if (callback != null) {
                        callback.onCreateGroupCompleted(
                                new FirebaseResult<Group>(FamilyTrackException.getInstance(mContext,
                                        FamilyTrackException.DB_USER_BY_UUID_NOT_FOUND)));
                    }
                }
            }
        });
    }

    /**
     * Membership records will be physically deleted from
     *  - registered_users/<userUuid>/memberships/<groupUuid>
     *  - groups/<groupUuid>/members/<userUuid>
     */
    @Override
    public void removeUserFromGroup(@NonNull String groupUuid,
                                    @NonNull String userUuid,
                                    final RemoveUserFromGroupCallback callback) {
        String key1 = FamilyTrackDbRefsHelper.userOfGroupRef(groupUuid, userUuid);
        String key2 = FamilyTrackDbRefsHelper.groupOfUserRef(userUuid, groupUuid);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key1, null);
        childUpdates.put(key2, null);

        getFirebaseConnection().getReference()
                .updateChildren(childUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (callback != null) {
                            callback.onRemoveUserFromGroupCompleted(new FirebaseResult<>(FirebaseResult.RESULT_OK));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Timber.e(e);
                        if (callback != null) {
                            callback.onRemoveUserFromGroupCompleted(
                                    new FirebaseResult<String>(FamilyTrackException.getInstance(mContext,
                                            FamilyTrackException.DB_REMOVE_USER_FAILED)));
                        }
                    }
                });
    }


    @Override
    public void getGroupByUuid(@NonNull String groupUuid,
                               boolean trackGroupNode,
                               final @NonNull GetGroupByUuidCallback callback) {
        DatabaseReference ref = getFirebaseConnection()
                .getReference(FamilyTrackDbRefsHelper.groupRef(groupUuid));
        Query query = ref.orderByValue();

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get Group object with all members
                Group group = FirebaseUtil.getGroupFromSnapshot(dataSnapshot);
                callback.onGetGroupByUuidCompleted(new FirebaseResult<>(group));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        if (trackGroupNode) {
            query.addValueEventListener(listener);
        } else {
            query.addListenerForSingleValueEvent(listener);
        }
    }

    @Override
    public void getContactsToInvite(@NonNull final String groupUuid,
                                    @NonNull final GetContactsToInviteCallback callback) {
        User user;
        // read contacts from ContactsContract.Data.CONTENT_URI
        Cursor cursor = mContext.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                null,
                ContactsContract.Data.HAS_PHONE_NUMBER + "!=0 AND (" +
                        ContactsContract.Data.MIMETYPE + "=? OR " +
                        ContactsContract.Data.MIMETYPE + "=?)",
                new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE},
                ContactsContract.Data.CONTACT_ID);

        // merge same contacts with emails & phone numbers
        final Map<String, User> contacts = new HashMap<>();
        while (cursor.moveToNext()) {
            String key = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID));
            boolean isNew = !contacts.containsKey(key);

            if (isNew) {
                String givenName = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                user = new User(key, "", "",
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)),
                        StringKeys.CREATED_FROM_CONTACTS_KEY, "", "", null, null);
            } else {
                user = contacts.get(key);
            }

            //Timber.v(user.getDisplayName() + "/" + cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE)));

            String mimeType = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));

            try {
                String data = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA1));
                if (mimeType.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                    user.setEmail(data);
                } else {
                    user.setPhone(formatPhoneString(data));
                }

                if (isNew) {
                    contacts.put(key, user);
                } else {
                    contacts.remove(key);
                    contacts.put(key, user);
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }

        cursor.close();

        getGroupByUuid(groupUuid, false, new GetGroupByUuidCallback() {
            @Override
            public void onGetGroupByUuidCompleted(FirebaseResult<Group> result) {
                List<User> users = new ArrayList<>();
                if (result.getData() == null) {
                    Timber.v(String.format(mContext.getString(R.string.ex_group_with_uuid_not_found), groupUuid));
                    callback.onGetContactsToInviteCompleted(new FirebaseResult<List<User>>(users));
                }

                // select contacts that have phone number and email + not already invited in group
                for (String key : contacts.keySet()) {
                    User u = contacts.get(key);
                    if (!u.getPhone().equals("") && !u.getEmail().equals("") &&
                            !isUserInGroup(u, result.getData())) {
                        users.add(u);
                    }
                }
                callback.onGetContactsToInviteCompleted(new FirebaseResult<List<User>>(users));
            }
        });
    }

    private String formatPhoneString(String data) {
        String result = null;
        if (data != null) {
            result = data.replaceAll("[ ()-]", "");
        }
        return result;
    }

    /**
     * Checks if user from phone contact list is already invited
     * @param user - user from phone contacts
     * @param group
     * @return true if group already contains user with email that equals user email
     */
    private boolean isUserInGroup(User user, Group group) {
        boolean result = false;
        if (group.getMembers() != null) {
            for (String key : group.getMembers().keySet()) {
                result = user.getEmail().equals(group.getMembers().get(key).getEmail());
                if (result) {
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public void inviteUsers(@NonNull final String groupUuid,
                            @NonNull final List<User> usersToinvite,
                            @NonNull final InviteUsersCallback callback) {
        // check that group exists
        getGroupByUuid(groupUuid, false, new GetGroupByUuidCallback() {
            @Override
            public void onGetGroupByUuidCompleted(FirebaseResult<Group> result) {
                if (result.getData() == null) {
                    // group not found
                    callback.onInviteUsersCompleted(
                            new FirebaseResult<String>(FamilyTrackException.getInstance(mContext, FamilyTrackException.DB_GROUP_NOT_FOUND)));
                    return;
                }
                CountDownLatch countDownLatch = new CountDownLatch(usersToinvite.size());
                for (User user : usersToinvite) {
                    inviteUser(user, result.getData(), countDownLatch, callback);
                }
            }
        });
    }

    private void inviteUser(final User user,
                            final Group group,
                            final CountDownLatch countDownLatch,
                            final InviteUsersCallback callback) {
        if (isUserAlreadyInvited(user, group)) {
            return;
        }
        //закончил здесь, почему-то вместо uuid подставляется id пользователя из контактов
        // проверяем логинился ли такой пользователь в системе
        final Map<String, Object> childUpdates = new HashMap<>();
        getUserByPhone(user.getPhone(), new GetUserByPhoneCallback() {
            @Override
            public void onGetUserByPhoneCompleted(FirebaseResult<User> result) {
                User dbUser = result.getData();
                Membership newMembership = new Membership(group.getGroupUuid(), group.getName(), Membership.ROLE_UNDEFINED, Membership.USER_INVITED);
                if (dbUser == null) {
                    // пользователь не найден, вначале создаем пользователя в ветке registered_users
                    user.addMembership(newMembership);
                    user.setUserUuid(getFirebaseConnection().getReference(Group.REGISTERED_USERS_GROUP_KEY).push().getKey());
                    childUpdates.put(FamilyTrackDbRefsHelper.userRef(user.getUserUuid()), user);

                    group.addMember(user);
                    childUpdates.put(FamilyTrackDbRefsHelper.groupRef(group.getGroupUuid()), group);
                } else {
                    // пользователь уже регистрировался в системе,
                    // просто добавляем его в группу в качестве приглашенного
                    dbUser.addMembership(newMembership);
                    childUpdates.put(FamilyTrackDbRefsHelper.userRef(dbUser.getUserUuid()), dbUser);
                    group.addMember(dbUser);
                    childUpdates.put(FamilyTrackDbRefsHelper.groupRef(group.getGroupUuid()), group);
                }
                getFirebaseConnection()
                        .getReference()
                        .updateChildren(childUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                countDownLatch.countDown();
                                if (countDownLatch.getCount() == 0 && callback != null) {
                                    callback.onInviteUsersCompleted(new FirebaseResult<>(FirebaseResult.RESULT_OK));
                                }
                            }
                        });
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
    public void getUserGroups(@NonNull String userUuid, @NonNull final GetUserGroupsCallback callback) {
        final List<Group> groups = new ArrayList<>();

        getUserByUuid(userUuid, new GetUserByUuidCallback() {
            @Override
            public void onGetUserByUuidCompleted(FirebaseResult<User> result) {
                if (result.getException() != null) {
                    callback.onGetUserGroupsCompleted(new FirebaseResult<List<Group>>(result.getException()));
                }
                final List<Group> groups = new ArrayList<Group>();

                if (result.getData() != null && result.getData().getMemberships() != null) {
                    //Timber.v("Starting for, number of groups=" + result.getData().getMemberships().size());

                    final CountDownLatch doneSignal =
                            new CountDownLatch(result.getData().getMemberships().size());

                    for (String key : result.getData().getMemberships().keySet()) {
                        Membership membership = result.getData().getMemberships().get(key);
                        // now read data of each group in list
                        //Timber.v("Call getGroupByUuid for " + membership.getGroupUuid());
                        getGroupByUuid(membership.getGroupUuid(), false, new GetGroupByUuidCallback() {
                            @Override
                            public synchronized void onGetGroupByUuidCompleted(FirebaseResult<Group> result) {
                                //Timber.v("onGetGroupByUuidCompleted");
                                if (result.getData() != null) {
                                    groups.add(result.getData());
                                }
                                doneSignal.countDown();
                                if (doneSignal.getCount() == 0) {
                                    // all requests completed, should call callback
                                    callback.onGetUserGroupsCompleted(new FirebaseResult<List<Group>>(groups));
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void changeUserMembership(@NonNull String userUuid,
                                     @NonNull String fromGroupUuid,
                                     @NonNull String toGroupUuid,
                                     final @NonNull ChangeUserMembershipCallback callback) {
        // groups/-KtrkQ0jWp4m3dQg43V9/members/-KtrkPuXJZs21vF6mMzS/memberships/-KtrkQ0jWp4m3dQg43V9
        // registered_users/-Ku_qk0QFapzaREjRYCZ/memberships/-KtrkQ0jWp4m3dQg43V9

        Map<String, Object> childUpdates = new HashMap<>();
        if (!fromGroupUuid.equals("")) {
            childUpdates.put(FamilyTrackDbRefsHelper.userMembershipRef(userUuid, fromGroupUuid) +
                    Membership.FIELD_STATUS_ID, Membership.USER_DEPARTED);
            childUpdates.put(FamilyTrackDbRefsHelper.groupOfUserRef(userUuid, fromGroupUuid) +
                    Membership.FIELD_STATUS_ID, Membership.USER_DEPARTED);
        }

        if (!toGroupUuid.equals("")) {
            childUpdates.put(FamilyTrackDbRefsHelper.userMembershipRef(userUuid, toGroupUuid) +
                    Membership.FIELD_STATUS_ID, Membership.USER_JOINED);
            childUpdates.put(FamilyTrackDbRefsHelper.groupOfUserRef(userUuid, toGroupUuid) +
                    Membership.FIELD_STATUS_ID, Membership.USER_JOINED);
            childUpdates.put(FamilyTrackDbRefsHelper.userMembershipRef(userUuid, toGroupUuid) +
                    Membership.FIELD_ROLE_ID, Membership.ROLE_MEMBER);
            childUpdates.put(FamilyTrackDbRefsHelper.groupOfUserRef(userUuid, toGroupUuid) +
                    Membership.FIELD_ROLE_ID, Membership.ROLE_MEMBER);
        }

        mFirebaseDatabase.getReference()
                .updateChildren(childUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (callback != null) {
                            callback.onChangeUserMembershipCompleted(new FirebaseResult<String>(FirebaseResult.RESULT_OK));
                        }
                    }
                });

    }

    @Override
    public void getGroupsAvailableToJoin(@NonNull String phoneNumber,
                                         final @NonNull GetGroupsAvailableToJoinCallback callback) {
        this.getUserByPhone(phoneNumber, new GetUserByPhoneCallback() {
            @Override
            public void onGetUserByPhoneCompleted(FirebaseResult<User> result) {
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
        // для перевода пользователя в состояние MEMMBER нам необходимо:
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
                            callback.onAddUserToGroupCompleted(new FirebaseResult<String>(FirebaseResult.RESULT_OK));
                        }
                    }
                });
    }

    @Override
    public void updateUserLocation(@NonNull final String userUuid,
                                   @NonNull final Location location,
                                   @NonNull final UpdateUserLocationCallback callback) {
        //Timber.v("Started");
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
                //   - /groups/<groupUuid>/members/<userUuid> - for active group only!
                //   - /registered_users/<userUuid>
                //   - /history/<userUuid>/<new location>
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
                                            new FirebaseResult<String>(FirebaseResult.RESULT_OK));
                                }
                            }
                        });
            }
        });
    }

    @Override
    public void getUserTrack(@NonNull String userUuid,
                             long periodStart,
                             long periodEnd,
                             final @NonNull GetUserTrackCallback callback) {
        String historyItemPath = FamilyTrackDbRefsHelper.userHistory(userUuid);
        DatabaseReference ref = getFirebaseConnection().getReference(historyItemPath);

        Query query = ref.orderByChild(Location.FIELD_TIMESTAMP).startAt(periodStart).endAt(periodEnd);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Location> locations = new ArrayList<>();
                if (dataSnapshot.getChildren() != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        locations.add(FirebaseUtil.getLocationFromSnapshot(snapshot));
                    }
                }
                callback.onGetUserTrackCompleted(new FirebaseResult<>(locations));
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
            callback.onCreateZoneCompleted(new FirebaseResult<>(newRec.getKey()));
        }

    }

    @Override
    public void updateZone(@NonNull String groupUuid,
                           @NonNull Zone zone,
                           UpdateZoneCallback callback) {
        DatabaseReference ref = getFirebaseConnection()
                .getReference(FamilyTrackDbRefsHelper.zoneOfGroup(groupUuid, zone.getUuid()));

        ref.setValue(zone);
        if (callback != null) {
            callback.onUpdateZoneCompleted(new FirebaseResult<>(zone.getUuid()));
        }
    }

    @Override
    public void removeZone(@NonNull String groupUuid,
                           @NonNull String zoneUuid,
                           final RemoveZoneCallback callback) {
        DatabaseReference ref = getFirebaseConnection()
                .getReference(FamilyTrackDbRefsHelper.zoneOfGroup(groupUuid, zoneUuid));
        ref.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (callback != null) {
                    if (databaseError != null) {
                        callback.onRemoveZoneCompleted(new FirebaseResult<String>(databaseError));
                    } else {
                        callback.onRemoveZoneCompleted(new FirebaseResult<>(FirebaseResult.RESULT_OK));
                    }
                }
            }
        });
    }

    @Override
    public void getSettingsByGroupUuid(@NonNull String groupUuid,
                                       @NonNull final GetSettingsByGroupUuidCallback callback) {
        DatabaseReference ref = getFirebaseConnection()
                .getReference(FamilyTrackDbRefsHelper.groupSettingsRef(groupUuid));
        Query query = ref.orderByValue();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Settings s = Settings.getDefaultInstance();
                if (dataSnapshot.getChildrenCount() > 0) {
                    s = dataSnapshot.getValue(Settings.class);
                }
                callback.onGetSettingsByGroupUuidCompleted(new FirebaseResult<>(s));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void updateSettingsByGroupUuid(@NonNull String groupUuid,
                                          @NonNull Settings settings,
                                          UpdateSettingsByGroupUuidCallback callback) {
        DatabaseReference ref = getFirebaseConnection()
                .getReference(FamilyTrackDbRefsHelper.groupSettingsRef(groupUuid));

        ref.setValue(settings);
        if (callback != null) {
            callback.onUpdateSettingsByGroupUuidCompleted(new FirebaseResult<>(FirebaseResult.RESULT_OK));
        }
    }

    @Override
    public void deleteGeofenceEvent(@NonNull String userUuid, @NonNull String eventUuid,
                                    DeleteGeofenceEventsCallback callback) {
        DatabaseReference ref = getFirebaseConnection()
                .getReference(FamilyTrackDbRefsHelper.geofenceEventRef(userUuid, eventUuid));

        ref.setValue(null);
        if (callback != null) {
            callback.onDeleteGeofenceEventsCompleted(new FirebaseResult<>(FirebaseResult.RESULT_OK));
        }
    }

    @Override
    public void deleteGeofenceEvents(@NonNull String userUuid, DeleteGeofenceEventsCallback callback) {
        DatabaseReference ref = getFirebaseConnection()
                .getReference(FamilyTrackDbRefsHelper.geofenceEventsRef(userUuid));

        ref.setValue(null);
        if (callback != null) {
            callback.onDeleteGeofenceEventsCompleted(new FirebaseResult<>(FirebaseResult.RESULT_OK));
        }
    }

    // чтобы не забыть как это рабоотает
    // уведомление формируется для каждого пользователя группы в роли Admin
    // и записывается по адресу geofence_events/<groupAdminUuid>/<event>
    // каждый администратор получает уведомление об изменении прослушиваемого узла, читает эти данные,
    // формирует сообщение и удаляет их
    @Override
    public void createGeofenceEvent(@NonNull final String groupUuid, final GeofenceEvent geofenceEvent,
                                    final CreateGeofenceEventCallback callback) {
        getGroupByUuid(groupUuid, false, new GetGroupByUuidCallback() {
            @Override
            public void onGetGroupByUuidCompleted(FirebaseResult<Group> result) {
                if (result.getData() == null) {
                    //Timber.v("Group with key ='" + groupUuid + "' not found");
                    if (callback != null) {
                        callback.onCreateGeofenceEventCompleted(new FirebaseResult<>(FirebaseResult.RESULT_FAILED));
                    }
                    return;
                }

                for (String key : result.getData().getMembers().keySet()) {
                    User user = result.getData().getMembers().get(key);
                    if (user.getActiveMembership() != null) {
                        if (user.getActiveMembership().getStatusId() == Membership.USER_JOINED &&
                                user.getActiveMembership().getRoleId() == Membership.ROLE_ADMIN) {
                            String path = FamilyTrackDbRefsHelper.geofenceEventsRef(user.getUserUuid());
                            String newKey = getFirebaseConnection().getReference(path).push().getKey();
                            geofenceEvent.setEventUuid(newKey);
                            getFirebaseConnection().getReference(path + newKey).setValue(geofenceEvent);
                        }
                    }
                }
                if (callback != null) {
                    callback.onCreateGeofenceEventCompleted(new FirebaseResult<>(FirebaseResult.RESULT_OK));
                }
            }
        });
    }

    @Override
    public void getGeofenceEventsByUserUuid(@NonNull String userUuid,
                                            final @NonNull GetGeofenceEventsByUserUuidCallback callback) {
        DatabaseReference ref = getFirebaseConnection()
                .getReference(FamilyTrackDbRefsHelper.geofenceEventsRef(userUuid));
        Query query = ref.orderByKey();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<GeofenceEvent> events = new ArrayList<>();
                if (dataSnapshot.getChildren() != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        events.add(FirebaseUtil.getGeofenceEventFromSnapshot(snapshot));
                    }
                }
                callback.onGetGeofenceEventsByUserUuidCompleted(new FirebaseResult<>(events));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
