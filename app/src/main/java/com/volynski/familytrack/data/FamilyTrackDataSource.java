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
    void getUserByUuid(@NonNull String userUuid,
                       @NonNull GetUserByUuidCallback callback);

    interface GetUserByEmailCallback { void onGetUserByEmailCompleted(FirebaseResult<User> result); }
    void getUserByEmail(@NonNull String userEmail,
                        @NonNull GetUserByEmailCallback callback);

    interface GetUserByPhoneCallback { void onGetUserByPhoneCompleted(FirebaseResult<User> result); }
    void getUserByPhone(@NonNull String userPhone,
                        @NonNull GetUserByPhoneCallback callback);

    interface GetGroupsAvailableToJoinCallback {void onGetGroupsAvailableToJoinCompleted(FirebaseResult<List<Group>> result); }

    /**
     * Returns a list of groups, in which user with specified phoneNumber/email was invited
     * Conditions of group selection:
     *      - user with specified phoneNumber/email contains in a group
     *      - user has status User.USER_INVITED
     * @param phoneNumber - user phoneNumber
     * @param email - user email
     * @param callback - callback to return result
     */
    void getGroupsAvailableToJoin(@NonNull String phoneNumber,
                                  //@NonNull String email,
                                  @NonNull GetGroupsAvailableToJoinCallback callback);

    // --------------------------------------------------------------------------------------------
    //
    // group's operations
    //
    // --------------------------------------------------------------------------------------------
    interface CreateGroupCallback {void onCreateGroupCompleted(FirebaseResult<Group> result); }
    void createGroup(@NonNull Group group, String adminUuid, CreateGroupCallback callback);

    interface AddUserToGroupCallback { void onAddUserToGroupCompleted(FirebaseResult<String> result); }
    void addUserToGroup(@NonNull String userUuid, @NonNull String groupUuid, AddUserToGroupCallback callback);

    interface RemoveUserFromGroupCallback { void onRemoveUserFromGroupCompleted(FirebaseResult<String> result); }
    void removeUserFromGroup(@NonNull String groupUuid, @NonNull String userUuid, RemoveUserFromGroupCallback callback);

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
     * reads contacts from phone and returns them as list of available mUsers to invite
     * list is filtered using these criterias:
     *      - user from contacts shouldn't be already invited
     *      - user from contacts should have email & phone number both
     * @param callback
     */
    void getContactsToInvite(@NonNull GetContactsToInvite callback);

    interface InviteUsersCallback {void onInviteUsersCompleted(FirebaseResult<String> result ); }

    /**
     * Stores mUsers from usersToinvite list into specified group in firebase
     * User ignored if already invited (stored in group)
     * @param groupUuid - group Id to invite mUsers to
     * @param usersToinvite - list of mUsers to invite
     * @param callback
     */
    void inviteUsers(@NonNull String groupUuid, @NonNull List<User> usersToinvite, @NonNull InviteUsersCallback callback);
}
