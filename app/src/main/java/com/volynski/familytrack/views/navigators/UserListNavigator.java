package com.volynski.familytrack.views.navigators;

/**
 * Created by DmitryVolynski on 28.08.2017.
 *
 * Defines the navigation actions that can be called from a list item in the user list.
 *
 */

public interface UserListNavigator {
    void openUserDetails(String userUuid);
    void removeUser(String userUuid);
    void showUserOnMap(String userUuid);
    void inviteUser();
}
