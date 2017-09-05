package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.Membership;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 22.08.2017.
 */

public class UserOnMapViewModel extends BaseObservable {
    private final Context mContext;
    private String mCurrentUserUuid = "";
    private User mCurrentUser;
    private boolean mIsDataLoading = false;
    private FamilyTrackDataSource mRepository;

    public final ObservableList<User> users = new ObservableArrayList<>();
    private UserListNavigator mNavigator;

    public UserOnMapViewModel(Context context,
                              String currentUserUuid,
                             FamilyTrackDataSource dataSource) {
        mCurrentUserUuid = currentUserUuid;
        mContext = context.getApplicationContext();
        mRepository = dataSource;
    }

    /**
     * Starts loading data according to group membership of the user (groupUuid)
     * ViewModel will populate the view if current user is member of any group
     * @param user - User object representing current user
     */
    public void start() {

        if (mCurrentUserUuid.equals("")) {
            Timber.e("Can't start viewmodel. UserUuid is empty");
            return;
        }

        mIsDataLoading = true;
        mRepository.getUserByUuid(mCurrentUserUuid, new FamilyTrackDataSource.GetUserByUuidCallback() {
            @Override
            public void onGetUserByUuidCompleted(FirebaseResult<User> result) {
                if (result.getData() != null) {
                    mCurrentUser = result.getData();
                    if (mCurrentUser.getActiveMembership() != null) {
                        loadUsersList(mCurrentUser.getActiveMembership().getGroupUuid());
                    }
                } else {
                    Timber.v("User with uuid=" + mCurrentUserUuid + " not found ");
                }
            }
        });

    }

    /**
     * Get group members from DB and populates users object for the view
     * @param groupUuid - group Id to get members of
     */
    private void loadUsersList(String groupUuid) {
        mRepository.getGroupByUuid(groupUuid,
                new FamilyTrackDataSource.GetGroupByUuidCallback() {
                    @Override
                    public void onGetGroupByUuidCompleted(FirebaseResult<Group> result) {
                        populateUserListFromDbResult(result);
                        mIsDataLoading = false;
                    }
                });
    }

    /**
     * Converts Firebase result (group with members) into ObservableList<User>
     * Users with state=USER_JOINED will be included. They are joined to the group and could be tracked
     * @param result - Firebase result of getGroupByUuid
     */
    private void populateUserListFromDbResult(FirebaseResult<Group> result) {
        if (result.getData() != null && result.getData().getMembers() != null) {
            for (User user : result.getData().getMembers().values()) {
                if (user.getActiveMembership().getStatusId() == Membership.USER_JOINED) {
                    this.users.add(user);
                }
            }
        }
    }

    public void setNavigator(UserListNavigator mNavigator) {
        this.mNavigator = mNavigator;
    }
}
