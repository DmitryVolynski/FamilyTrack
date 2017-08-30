package com.volynski.familytrack.data;

import android.support.annotation.NonNull;

import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.User;

import java.util.List;

/**
 * Created by DmitryVolynski on 16.08.2017.
 */

public interface FamilyTrackDataSource {


    //
    // --------- user's operations
    //

    /**
     *
     */
    interface CreateUserCallback { void onCreateUserCompleted(FirebaseResult<User> result); }

    /**
     *
     * @param user
     * @param callback
     */
    void createUser(@NonNull User user, CreateUserCallback callback);

    void updateUser(@NonNull User user);

    void changeUserStatus(@NonNull String userUuid, @NonNull int newStatus);

    interface GetUserByUuidCallback { void onGetUserByUuidCompleted(FirebaseResult<User> result); }
    void getUserByUuid(@NonNull String userUuid, @NonNull GetUserByUuidCallback callback);

    interface GetUserByEmailCallback { void onGetUserByEmailCompleted(FirebaseResult<User> result); }
    void getUserByEmail(@NonNull String userEmail, @NonNull GetUserByEmailCallback callback);

    //
    // ---------- group's operations
    //
    interface CreateGroupCallback {void onCreateGroupCompleted(FirebaseResult<Group> result); }
    void createGroup(@NonNull Group group, String adminUuid, CreateGroupCallback callback);

    void addUser(@NonNull String groupUuid, @NonNull String userUuid);
    void removeUser(@NonNull String groupUuid, @NonNull String userUuid);

    /**
     *
     */
    interface GetGroupByUuidCallback {void onGetGroupByUuidCompleted(FirebaseResult<Group> result); }
    void getGroupByUuid(@NonNull String groupUuid, @NonNull GetGroupByUuidCallback callback);

    //
    // ---------- contact list operations
    //
    interface GetContactsToInvite {void onGetContactsToInviteCompleted(FirebaseResult<List<User>> result); }

    /**
     * reads contacts from phone and returns them as list of available users to invite
     * list is filtered using these criterias:
     *      - user from contacts shouldn't be already invited
     *      - user from contacts should have a gmail address
     * @param callback
     */
    void getContactsToInvite(@NonNull GetContactsToInvite callback);
}
