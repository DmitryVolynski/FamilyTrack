package com.volynski.familytrack.viewmodels;

import android.content.Context;
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
import com.volynski.familytrack.data.models.MembershipListItem;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.utils.NetworkUtil;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.util.List;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 05.10.2017.
 */

public class UserMembershipViewModel
        extends AbstractViewModel
        implements View.OnClickListener {

    public final static String UI_CONTEXT = UserMembershipViewModel.class.getSimpleName();
    private final static String TAG = UserMembershipViewModel.class.getSimpleName();
    private UserListNavigator mNavigator;

    public final ObservableBoolean showLeaveGroupWarningDialog =
            new ObservableBoolean(false);
    public final ObservableList<MembershipListItemViewModel> viewModels =
            new ObservableArrayList<>();

    public UserMembershipViewModel(Context context,
                             String currentUserUuid,
                             FamilyTrackDataSource dataSource) {
        super(context, currentUserUuid, dataSource);
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
        notifyChange();
    }

    /**
     * Starts loading data according to group membership of the user
     * ViewModel will populate the view if current user is member of any group
     */
    public void start() {
        if (!NetworkUtil.networkUp(mContext)) {
            snackbarText.set(mContext.getString(R.string.network_not_available));
            return;
        }

        if (mCurrentUserUuid.equals("")) {
            Timber.e(mContext.getString(R.string.ex_useruuid_is_empty));
            return;
        }

        isDataLoading.set(true);
        mRepository.getUserByUuid(mCurrentUserUuid, new FamilyTrackDataSource.GetUserByUuidCallback() {
            @Override
            public void onGetUserByUuidCompleted(FirebaseResult<User> result) {
                if (result.getData() != null) {
                    mCurrentUser = result.getData();
                    createViewModels();
                } else {
                    Timber.v(String.format(mContext.getString(R.string.ex_user_with_uuid_not_found), mCurrentUserUuid));
                    isDataLoading.set(false);
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
                isDataLoading.set(false);
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
        if (!NetworkUtil.networkUp(mContext)) {
            snackbarText.set(mContext.getString(R.string.network_not_available));
            return;
        }

        // check the user if he is one and only admin in current group
        if (mCurrentUser.getActiveMembership() == null && membershipListItemViewModel.isActive.get()) {
            Timber.e(String.format(mContext.getString(R.string.ex_no_active_group_for_useruuid),
                    mCurrentUser.getUserUuid()));
            return;
        }
        if (mCurrentUser.getActiveMembership() != null) {
            // join to new group or leave current group
            final String activeGroupUuid = mCurrentUser.getActiveMembership().getGroupUuid();
            mRepository.getGroupByUuid(activeGroupUuid, false,
                    new FamilyTrackDataSource.GetGroupByUuidCallback() {
                        @Override
                        public void onGetGroupByUuidCompleted(FirebaseResult<Group> result) {
                            Group group = result.getData();
                            if (group == null) {
                                Timber.e(String.format(mContext.getString(R.string.ex_group_uuid_not_found),
                                        activeGroupUuid));
                                return;
                            }
                            if (group.getAdminsCount(mCurrentUser.getUserUuid()) == 0 && group.getMembers().size() > 1) {
                                // there is only one admin in group
                                // can't leave current group or create new one
                                showLeaveGroupWarningDialog.set(!showLeaveGroupWarningDialog.get());
                                return;
                            }
                            finishChangeMembership(mCurrentUser.getUserUuid(), activeGroupUuid,
                                    (membershipListItemViewModel.isActive.get() ?
                                            "" : membershipListItemViewModel.item.get().getGroupUuid()),
                                    group.getName(), membershipListItemViewModel.isActive.get() ?
                                            "" : membershipListItemViewModel.item.get().getGroupName());
                        }
                    });
        } else {
            finishChangeMembership(mCurrentUser.getUserUuid(), "",
                    membershipListItemViewModel.item.get().getGroupUuid(),
                    "", membershipListItemViewModel.item.get().getGroupName());
        }
    }

    private void finishChangeMembership(String userUuid,
                                        String fromGroupUuid,
                                        String toGroupUuid,
                                        final String fromGroupName,
                                        final String toGroupName) {
        mRepository.changeUserMembership(mCurrentUser.getUserUuid(), fromGroupUuid, toGroupUuid,
                new FamilyTrackDataSource.ChangeUserMembershipCallback() {
                    @Override
                    public void onChangeUserMembershipCompleted(FirebaseResult<String> result) {
                        if (result.getData().equals(FirebaseResult.RESULT_OK)) {
                            snackbarText.set(String.format(mContext.getString(R.string.msg_group_changed),
                                    fromGroupName, toGroupName));
                            start();
                        }
                    }
                });

    }

    public void createNewGroup(final String groupName) {
        if (!NetworkUtil.networkUp(mContext)) {
            snackbarText.set(mContext.getString(R.string.network_not_available));
            return;
        }

        if (mCurrentUser.getActiveMembership() != null) {
            final String activeGroupUuid = mCurrentUser.getActiveMembership().getGroupUuid();
            mRepository.getGroupByUuid(activeGroupUuid, false,
                    new FamilyTrackDataSource.GetGroupByUuidCallback() {
                        @Override
                        public void onGetGroupByUuidCompleted(FirebaseResult<Group> result) {
                            Group group = result.getData();
                            if (group == null) {
                                Timber.e(String.format(mContext.getString(R.string.ex_group_uuid_not_found),
                                        activeGroupUuid));
                                return;
                            }
                            if (group.getAdminsCount(mCurrentUser.getUserUuid()) == 0 && group.getMembers().size() > 1) {
                                // there is only one admin in current user group
                                // can't leave current group or create new one
                                showLeaveGroupWarningDialog.set(!showLeaveGroupWarningDialog.get());
                                return;
                            } else {
                                mRepository.createGroup(new Group(groupName), mCurrentUserUuid,
                                        new FamilyTrackDataSource.CreateGroupCallback() {
                                            @Override
                                            public void onCreateGroupCompleted(FirebaseResult<Group> result) {
                                                if (result.getData() != null) {
                                                    snackbarText.set(String
                                                            .format(mContext.getString(R.string.msg_group_created), groupName));
                                                    start();
                                                }
                                            }
                                        });
                            }
                        }
                    });
        } else {
            mRepository.createGroup(new Group(groupName), mCurrentUserUuid,
                    new FamilyTrackDataSource.CreateGroupCallback() {
                        @Override
                        public void onCreateGroupCompleted(FirebaseResult<Group> result) {
                            if (result.getData() != null) {
                                snackbarText.set(String.format(mContext.getString(R.string.msg_group_created), groupName));
                                start();
                            }
                        }
                    });
        }
    }
}
