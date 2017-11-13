package com.volynski.familytrack.viewmodels;


import android.content.Context;
import android.database.Observable;
import android.databinding.BaseObservable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableList;
import android.util.Log;
import android.view.View;

import com.volynski.familytrack.R;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.GeofenceEvent;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.data.models.firebase.Zone;
import com.volynski.familytrack.services.locators.SimulatedLocationProvider;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 22.08.2017.
 */

public class UserListViewModel
        extends AbstractViewModel {

    public final static String UI_CONTEXT = UserListViewModel.class.getSimpleName();
    private UserListNavigator mNavigator;

    public final ObservableBoolean showDialog =
            new ObservableBoolean(false);
    public final ObservableList<UserListItemViewModel> viewModels =
            new ObservableArrayList<>();
    public final ObservableField<String> groupName =
            new ObservableField<>("");

    private List<User> mUsers = new ArrayList<>();

    public UserListViewModel(Context context,
                             String currentUserUuid,
                             FamilyTrackDataSource dataSource,
                             UserListNavigator navigator) {
        super(context, currentUserUuid, dataSource);
        mNavigator = navigator;
    }

    /**
     * Starts loading data according to group membership of the user
     * ViewModel will populate the view if current user is member of any group
     * @param currentUserUuid - User object representing current user
     */
    public void start(String currentUserUuid) {
        mCurrentUserUuid = currentUserUuid;
        if (mCurrentUserUuid.equals("")) {
            Timber.e("Can't start viewmodel. UserUuid is empty");
            return;
        }

        isDataLoading.set(true);
        mRepository.getUserByUuid(mCurrentUserUuid, new FamilyTrackDataSource.GetUserByUuidCallback() {
            @Override
            public void onGetUserByUuidCompleted(FirebaseResult<User> result) {
                if (result.getData() != null) {
                    mCurrentUser = result.getData();
                    loadUsersList();
                } else {
                    Timber.v("User with uuid=" + mCurrentUserUuid + " not found ");
                    isDataLoading.set(false);
                }
            }
        });
    }

    /**
     *
     */
    private void loadUsersList() {
        if (mCurrentUser.getActiveMembership() != null) {
            mRepository.getGroupByUuid(mCurrentUser.getActiveMembership().getGroupUuid(), false,
                    new FamilyTrackDataSource.GetGroupByUuidCallback() {
                        @Override
                        public void onGetGroupByUuidCompleted(FirebaseResult<Group> result) {
                            populateUserListFromDbResult(result);
                            isDataLoading.set(false);
                        }
                    });
        }
    }

    /**
     *
     * @param result
     */
    private void populateUserListFromDbResult(FirebaseResult<Group> result) {
        mUsers.clear();
        viewModels.clear();
        if (result.getData() != null && result.getData().getMembers() != null) {
            groupName.set(result.getData().getName());
            for (User user : result.getData().getMembers().values()) {
                viewModels.add(new UserListItemViewModel(mContext, user, mNavigator, UI_CONTEXT));
                mUsers.add(user);
            }
        }
    }

    public void createGeofenceEvent() {
        Zone zone = new Zone("-Kjhgfjdgh", "Test zone", SimulatedLocationProvider.RED_SQUARE_LOCATION, 200, null);

        GeofenceEvent event = new GeofenceEvent(mCurrentUser.getUserUuid(), mCurrentUser.getDisplayName(),
                mCurrentUser.getFamilyName(), mCurrentUser.getGivenName(), zone, 1);

        mRepository.createGeofenceEvent(mCurrentUser.getActiveMembership().getGroupUuid(), event,
                new FamilyTrackDataSource.CreateGeofenceEventCallback() {
                    @Override
                    public void onCreateGeofenceEventCompleted(FirebaseResult<String> result) {
                        int i = 0;
                    }
                });
        mRepository.createGeofenceEvent(mCurrentUser.getActiveMembership().getGroupUuid(), event,
                new FamilyTrackDataSource.CreateGeofenceEventCallback() {
                    @Override
                    public void onCreateGeofenceEventCompleted(FirebaseResult<String> result) {
                        int i = 0;
                    }
                });
    }

    public void setNavigator(UserListNavigator mNavigator) {
        this.mNavigator = mNavigator;
    }
    public UserListNavigator getNavigator() {
        return mNavigator;
    }

    public void excludeUser(final String userUuid) {
        if (userUuid.equals(mCurrentUserUuid)) {
            snackbarText.set("");
            snackbarText.set("You can't exclude yourself");
            return;
        }

        if (mCurrentUser.getActiveMembership() == null) {
            Timber.v("Active group for " + userUuid + " == null. Expected value");
            snackbarText.set("Unable to exclude user");
            return;
        }

        String activeGroupUuid = mCurrentUser.getActiveMembership().getGroupUuid();
        mRepository.removeUserFromGroup(activeGroupUuid, userUuid, new FamilyTrackDataSource.RemoveUserFromGroupCallback() {
            @Override
            public void onRemoveUserFromGroupCompleted(FirebaseResult<String> result) {
                if (result.getData().equals(FirebaseResult.RESULT_OK)) {
                    snackbarText.set("");
                    snackbarText.set("User excluded from group");
                    loadUsersList();
                } else {
                    snackbarText.set("Unable to exclude user " + userUuid);
                }
            }
        });

    }
}
