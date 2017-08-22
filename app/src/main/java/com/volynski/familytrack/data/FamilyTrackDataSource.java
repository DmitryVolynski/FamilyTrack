package com.volynski.familytrack.data;

import android.support.annotation.NonNull;

import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.User;

/**
 * Created by DmitryVolynski on 16.08.2017.
 */

public interface FamilyTrackDataSource {

    interface GetUserByEmailCallback { void onGetUserByEmailCompleted(FirebaseResult<User> result); }

    // user's operations
    void createUser(@NonNull User user);
    void updateUser(@NonNull User user);
    void changeUserStatus(@NonNull String userUuid, @NonNull int newStatus);
    void getUserByUuid(@NonNull String userUuid);
    void getUserByEmail(@NonNull String userEmail, @NonNull GetUserByEmailCallback callback);

    // group's operations
    void createGroup(@NonNull Group group);
    void addUser(@NonNull String groupUuid, @NonNull String userUuid);
    void removeUser(@NonNull String groupUuid, @NonNull String userUuid);

}
