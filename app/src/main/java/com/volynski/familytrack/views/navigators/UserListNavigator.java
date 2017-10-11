package com.volynski.familytrack.views.navigators;

import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.volynski.familytrack.data.models.firebase.User;

/**
 * Created by DmitryVolynski on 28.08.2017.
 *
 * Defines the navigation actions that can be called from a list item in the user list.
 *
 */

public interface UserListNavigator {
    void openUserDetails(String userUuid, View rootView);
    void removeUser(String userUuid);
    void inviteUsers();
    void dismissInviteUsersDialog();
    void userClicked(User user, String uiContext);
}
