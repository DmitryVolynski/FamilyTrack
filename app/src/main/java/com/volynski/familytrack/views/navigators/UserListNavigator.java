package com.volynski.familytrack.views.navigators;

import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.volynski.familytrack.data.models.firebase.GeofenceEvent;
import com.volynski.familytrack.data.models.firebase.User;

/**
 * Created by DmitryVolynski on 28.08.2017.
 *
 * Defines the navigation actions that can be called from a list item in the user list.
 *
 */

public interface UserListNavigator {
    void editUserDetails(String userUuid, View rootView);
    void inviteCompleted();
    void dismissInviteUsersDialog();
    void userClicked(User user);
    void eventClicked(GeofenceEvent event, String mUiContext);
    void showUserOnMap(User user, boolean forceSelect);
    void showPopupDialog(String title, String message);
}
