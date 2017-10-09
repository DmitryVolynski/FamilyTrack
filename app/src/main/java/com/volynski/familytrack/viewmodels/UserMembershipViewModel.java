package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableList;
import android.util.Log;
import android.view.View;

import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.MembershipListItem;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.util.List;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 05.10.2017.
 */

public class UserMembershipViewModel
        extends BaseObservable
        implements View.OnClickListener {

    public final static String UI_CONTEXT = UserMembershipViewModel.class.getSimpleName();
    private final static String TAG = UserMembershipViewModel.class.getSimpleName();
    private final Context mContext;
    private String mCurrentUserUuid = "";
    private User mCurrentUser;
    private boolean mIsDataLoading;
    private FamilyTrackDataSource mRepository;
    private UserListNavigator mNavigator;

    public final ObservableBoolean showLeaveGroupWarningDialog = new ObservableBoolean(false);
    public final ObservableList<MembershipListItemViewModel> viewModels = new ObservableArrayList<>();

    public UserMembershipViewModel(Context context,
                             String currentUserUuid,
                             FamilyTrackDataSource dataSource) {
        mCurrentUserUuid = currentUserUuid;
        mContext = context.getApplicationContext();
        mRepository = dataSource;
    }

    @Override
    public void onClick(View v) {
    }


    /**
     * Joins mUsers to selected group
     * Shows a dialog window with all available groups (for joining)
     * If user select one, he will be joined to selected group
     */
    private void joinGroup() {
        Log.v(TAG, "joinGroup started");
        notifyChange();
    }

    /**
     * Starts loading data according to group membership of the user
     * ViewModel will populate the view if current user is member of any group
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
                    createViewModels();
                } else {
                    Timber.v("User with uuid=" + mCurrentUserUuid + " not found ");
                }
            }
        });
    }

    /**
     *
     */
    private void createViewModels() {
        mRepository.getUserGroups(mCurrentUser.getUserUuid(), new FamilyTrackDataSource.GetUserGroupsCallback() {
            @Override
            public void onGetUserGroupsCompleted(FirebaseResult<List<Group>> result) {
                viewModels.clear();
                String activeGroupUuid = (mCurrentUser.getActiveMembership() != null ?
                    mCurrentUser.getActiveMembership().getGroupUuid() : "");

                if (result.getData() != null) {
                    for (Group group : result.getData()) {
                        List<MembershipListItem> newList = MembershipListItem.createListFromGroup(group);
                        for (MembershipListItem item : newList) {
                            boolean isActive = (group.getGroupUuid().equals(activeGroupUuid) &&
                                item.getType() == MembershipListItem.TYPE_GROUP);
                            viewModels.add(new MembershipListItemViewModel(mContext, item, isActive,
                                    UserMembershipViewModel.this));
                        }
                    }
                }
            }
        });
    }

    public void setNavigator(UserListNavigator mNavigator) {
        this.mNavigator = mNavigator;
    }

    /**
     * Fired when user clicked on 'LEAVE' or 'JOIN' button in appropriate group
     * User could leave existing group|join another one if
     *      - he is just a member of group (not admin
     *      - he is admin but there is at least one more admin in the group that user is going to leave
     * User couldn't leave current group if he is one and only admin
     * @param membershipListItemViewModel - viewmodel of appropriate group
     */
    public void startChangeMembership(final MembershipListItemViewModel membershipListItemViewModel) {
        // check the user if he is one and only admin in current group
        if (mCurrentUser.getActiveMembership() == null) {
            Timber.e("Unexpected error. User " + mCurrentUser.getUserUuid() +
                    " should have active group to perform 'startChangeMembership' action");
            return;
        }
        final String activeGroupUuid = mCurrentUser.getActiveMembership().getGroupUuid();
        mRepository.getGroupByUuid(activeGroupUuid,
            new FamilyTrackDataSource.GetGroupByUuidCallback() {
                @Override
                public void onGetGroupByUuidCompleted(FirebaseResult<Group> result) {
                    Group group = result.getData();
                    if (group == null) {
                        Timber.e("Unexpected error. Group " + activeGroupUuid + " not found");
                        return;
                    }
                    if (group.getAdminsCount(mCurrentUser.getUserUuid()) == 0) {
                        // there is only one admin in group
                        // can't leave current group or create new one
                        showLeaveGroupWarningDialog.set(!showLeaveGroupWarningDialog.get());
                    }
                    String fromGroupUuid = activeGroupUuid;
                    String toGroupUuid = (membershipListItemViewModel.isActive.get() ?
                            "" : membershipListItemViewModel.item.get().getGroupUuid());
                    //mRepository.changeUserMembership(mCurrentUser.getUserUuid(), fromGroupUuid, toGroupUuid)
                }
            });
    }

    public void createNewGroup(String groupName) {
        Timber.v("Create group: " + groupName);

        mRepository.createGroup(new Group(groupName), mCurrentUserUuid,
                new FamilyTrackDataSource.CreateGroupCallback() {
                    @Override
                    public void onCreateGroupCompleted(FirebaseResult<Group> result) {
                        //if ()
                    }
                });
    }
}
