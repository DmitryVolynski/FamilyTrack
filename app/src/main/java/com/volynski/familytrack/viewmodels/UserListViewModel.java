package com.volynski.familytrack.viewmodels;


import android.content.Context;
import android.database.Observable;
import android.databinding.BaseObservable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableList;
import android.util.Log;
import android.view.View;

import com.volynski.familytrack.R;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 22.08.2017.
 */

public class UserListViewModel
        extends BaseObservable
        implements View.OnClickListener {

    private final static String TAG = UserListViewModel.class.getSimpleName();
    private final Context mContext;
    private String mCurrentUserUuid = "";
    private User mCurrentUser;
    private boolean mIsDataLoading;
    private FamilyTrackDataSource mRepository;
    private UserListNavigator mNavigator;

    public final ObservableBoolean showDialog = new ObservableBoolean(false);
    public final ObservableList<UserListItemViewModel> viewModels = new ObservableArrayList<>();

    private List<User> mUsers = new ArrayList<>();


    public UserListViewModel(Context context,
                             String currentUserUuid,
                             FamilyTrackDataSource dataSource) {
        mCurrentUserUuid = currentUserUuid;
        mContext = context.getApplicationContext();
        mRepository = dataSource;
    }

    /**
     * Shows a dialog with a list of all available mUsers
     * User list extracted from phone contacts
     * Set of selected mUsers will be added to current group with state 'Joining'
     */
    public void inviteUsers() {
        Timber.v("Invite mUsers");
        if (mNavigator != null) {
            mNavigator.inviteUsers();
        } else {
            Timber.e("mNavigator is null");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_fragmentuserslist_creategroup:
                createGroup();
                break;
            case R.id.button_fragmentuserslist_addusers:
                inviteUsers();
                break;
            case R.id.button_fragmentuserslist_joingroup:
                joinGroup();
                break;
        }
        refreshList();
    }

    /**
     * Reads list of mUsers from firebase and refresh it in RecyclerView
     */
    private void refreshList() {
        Log.v(TAG, "refreshList started");
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
     * Creates new group in firebase DB.
     * Current user (who created the group) becomes an Admin of this group
     */
    private void createGroup() {
        Timber.v("createGroup started");
        showDialog.set(true);
    }

    public void createNewGroup(String groupName) {
        Timber.v("Create group: " + groupName);
        User currentUser = SharedPrefsUtil.getCurrentUser(mContext);

        FamilyTrackDataSource dataSource = new FamilyTrackRepository(null, mContext);
        dataSource.createGroup(new Group(groupName), currentUser.getUserUuid(), null);
    }

    /**
     * Starts loading data according to group membership of the user
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
                    loadUsersList();
                } else {
                    Timber.v("User with uuid=" + mCurrentUserUuid + " not found ");
                }
            }
        });
    }

    /**
     *
     */
    private void loadUsersList() {
        if (mCurrentUser.getActiveMembership() != null) {
            mRepository.getGroupByUuid(mCurrentUser.getActiveMembership().getGroupUuid(),
                    new FamilyTrackDataSource.GetGroupByUuidCallback() {
                        @Override
                        public void onGetGroupByUuidCompleted(FirebaseResult<Group> result) {
                            populateUserListFromDbResult(result);
                            mIsDataLoading = false;
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
            for (User user : result.getData().getMembers().values()) {
                viewModels.add(new UserListItemViewModel(mContext, user, mNavigator));
                mUsers.add(user);
            }
        }
    }

    public void setNavigator(UserListNavigator mNavigator) {
        this.mNavigator = mNavigator;
    }
}
