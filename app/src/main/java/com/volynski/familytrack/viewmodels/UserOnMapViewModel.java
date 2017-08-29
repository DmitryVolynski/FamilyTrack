package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableList;

import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.User;

/**
 * Created by DmitryVolynski on 22.08.2017.
 */

public class UserOnMapViewModel extends BaseObservable {
    private final Context mContext;
    private User mCurrentUser;
    private boolean mIsDataLoading = false;
    private FamilyTrackDataSource mRepository;

    public final ObservableList<User> users = new ObservableArrayList<>();

    public UserOnMapViewModel(Context context,
                             FamilyTrackDataSource dataSource) {
        mContext = context.getApplicationContext();
        mRepository = dataSource;
    }

    /**
     * Starts loading data according to group membership of the user (groupUuid)
     * ViewModel will populate the view if current user is member of any group
     * @param user - User object representing current user
     */
    public void start(User user) {
        mCurrentUser = user;
        if (mCurrentUser.getStatusId() != User.USER_JOINED) {
            return;
        }

        mIsDataLoading = true;
        loadUsersList(mCurrentUser.getGroupUuid());
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
                if (user.getStatusId() == User.USER_JOINED) {
                    this.users.add(user);
                }
            }
        }
    }
}
