package com.volynski.familytrack.data;

import android.support.annotation.NonNull;

import com.google.firebase.database.FirebaseDatabase;
import com.volynski.familytrack.data.models.firebase.GeofenceEvent;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.Location;
import com.volynski.familytrack.data.models.firebase.Settings;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.data.models.firebase.Zone;

import java.util.List;

/**
 * Created by DmitryVolynski on 16.08.2017.
 */

public interface FamilyTrackDataSource {

    FirebaseDatabase getFirebaseConnection();

    //
    // --------- geofence events operations
    //
    interface GetGeofenceEventsByUserUuidCallback { void onGetGeofenceEventsByUserUuidCompleted(FirebaseResult<List<GeofenceEvent>> result); }
    void getGeofenceEventsByUserUuid(@NonNull String userUuid,
                                     @NonNull GetGeofenceEventsByUserUuidCallback callback );

    interface DeleteGeofenceEventsCallback { void onDeleteGeofenceEventsCompleted(FirebaseResult<String> result); }
    void deleteGeofenceEvent(@NonNull String userUuid,
                              @NonNull String eventuuid,
                              DeleteGeofenceEventsCallback callback );
    void deleteGeofenceEvents(@NonNull String userUuid,
                              DeleteGeofenceEventsCallback callback );

    interface CreateGeofenceEventCallback { void onCreateGeofenceEventCompleted(FirebaseResult<String> result); }
    void createGeofenceEvent(@NonNull String groupUuid,
                             @NonNull GeofenceEvent geofenceEvent,
                             CreateGeofenceEventCallback callback);

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

    interface UpdateUserCallback { void onUpdateUserCompleted(FirebaseResult<String> result); }
    void updateUser(@NonNull User user, UpdateUserCallback callback);

    interface UpdateUserLocationCallback { void onUpdateUserLocationCompleted(FirebaseResult<String> result); }
    void updateUserLocation(@NonNull String userUuid,
                            @NonNull Location location,
                            UpdateUserLocationCallback callback);

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

    interface GetUserGroupsCallback { void onGetUserGroupsCompleted(FirebaseResult<List<Group>> result); }
    /**
     * Returns a list of all available groups for user: current + all groups that user is invited in
     * @param userUuid - user key
     * @param callback - callback to return result
     */
    void getUserGroups(@NonNull String userUuid,
                       @NonNull GetUserGroupsCallback callback);

    interface ChangeUserMembershipCallback { void onChangeUserMembershipCompleted(FirebaseResult<String> result); }
    void changeUserMembership(@NonNull String userUuid,
                              @NonNull String fromGroupUuid,
                              @NonNull String toGroupUuid,
                              @NonNull ChangeUserMembershipCallback callback);
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

    interface GetUserTrackCallback {void onGetUserTrackCompleted(FirebaseResult<List<Location>> result); }

    /**
     * Returns a historical list of known user locations for selected period of time
     * @param userUuid - user uuid
     * @param periodStart - start date in milliseconds
     * @param periodEnd - end date in milliseconds
     * @param callback - callback to return result
     */
    void getUserTrack(@NonNull String userUuid,
                      @NonNull long periodStart,
                      @NonNull long periodEnd,
                      @NonNull GetUserTrackCallback callback);

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

    /**
     * Removes user from specified group
     * User will be deleted from path
     * /groups/<groupUuid>/members/<userUuid>
     *
     * @param groupUuid - group uuid
     * @param userUuid - user uuid
     * @param callback - callback
     */
    void removeUserFromGroup(@NonNull String groupUuid, @NonNull String userUuid, RemoveUserFromGroupCallback callback);

    /**
     *
     */
    interface GetGroupByUuidCallback {void onGetGroupByUuidCompleted(FirebaseResult<Group> result); }

    /**
     * Reads all group data (including members) for specified group
     * @param groupUuid - group key to find specified group
     * @param trackGroupNode - true if we need to track changes on group node
     * @param callback - callback to return result
     */
    void getGroupByUuid(@NonNull String groupUuid, boolean trackGroupNode, @NonNull GetGroupByUuidCallback callback);

    interface GetSettingsByGroupUuidCallback { void onGetSettingsByGroupUuidCompleted(FirebaseResult<Settings> result); }

    /**
     * Reads settings for group with specified uuid
     * @param groupUuid - group uuid
     * @param callback - callback to return result
     */
    void getSettingsByGroupUuid(@NonNull String groupUuid, @NonNull GetSettingsByGroupUuidCallback callback);

    interface UpdateSettingsByGroupUuidCallback { void onUpdateSettingsByGroupUuidCompleted(FirebaseResult<String> result); }

    /**
     * Updates settings for group with specified uuid
     * @param groupUuid - group uuid
     * @param settings - new setting for group
     * @param callback - callback
     */
    void updateSettingsByGroupUuid(@NonNull String groupUuid,
                                   @NonNull Settings settings,
                                   UpdateSettingsByGroupUuidCallback callback);

    // --------------------------------------------------------------------------------------------
    //
    // contact list operations
    //
    // --------------------------------------------------------------------------------------------
    interface GetContactsToInviteCallback {void onGetContactsToInviteCompleted(FirebaseResult<List<User>> result); }

    /**
     * reads contacts from phone and returns them as list of available mUsers to invite
     * list is filtered using these criterias:
     *      - user from contacts shouldn't be already invited
     *      - user from contacts should have email & phone number both
     * @param callback
     */
    void getContactsToInvite(@NonNull String groupUuid,
                             @NonNull GetContactsToInviteCallback callback);

    interface InviteUsersCallback {void onInviteUsersCompleted(FirebaseResult<String> result ); }

    /**
     * Stores mUsers from usersToinvite list into specified group in firebase
     * User ignored if already invited (stored in group)
     * @param groupUuid - group Id to invite mUsers to
     * @param usersToinvite - list of mUsers to invite
     * @param callback
     */
    void inviteUsers(@NonNull String groupUuid, @NonNull List<User> usersToinvite, @NonNull InviteUsersCallback callback);

    // --------------------------------------------------------------------------------------------
    //
    // zones operations
    //
    // --------------------------------------------------------------------------------------------
    interface CreateZoneCallback { void onCreateZoneCompleted(FirebaseResult<String> result); }

    /**
     * Creates a new observed zone (geofence location) in firebase
     * @param zone - description of new zone (geofence)
     * @param callback - optional callback
     */
    void createZone(@NonNull String grupUuid, @NonNull Zone zone, CreateZoneCallback callback);

    interface UpdateZoneCallback { void onUpdateZoneCompleted(FirebaseResult<String> result); }

    /**
     * Updates an existing observed zone (geofence location) in firebase
     * @param zone - description of zone with existing key(!!!) (geofence)
     * @param callback - optional callback
     */
    void updateZone(@NonNull String groupUuid, @NonNull Zone zone, UpdateZoneCallback callback);

    interface RemoveZoneCallback { void onRemoveZoneCompleted(FirebaseResult<String> result); }

    /**
     * Removes zone from db
     * @param groupUuid - group key where we will try to find zone to remove
     * @param zoneUuid - key of deleting zone
     * @param callback - optional callback
     */
    void removeZone(@NonNull String groupUuid, @NonNull String zoneUuid, RemoveZoneCallback callback);
}
