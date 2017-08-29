package com.volynski.familytrack.viewmodels;


import android.content.Context;
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

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 22.08.2017.
 */

public class UserListViewModel
        extends BaseObservable
        implements View.OnClickListener {

    private final static String TAG = UserListViewModel.class.getSimpleName();
    private final Context mContext;
    private User mCurrentUser;
    private boolean mIsDataLoading;
    private FamilyTrackDataSource mRepository;

    public final ObservableBoolean showDialog = new ObservableBoolean(false);
    public final ObservableList<User> users = new ObservableArrayList<>();

    public UserListViewModel(Context context,
                             FamilyTrackDataSource dataSource) {
        mContext = context.getApplicationContext();
        mRepository = dataSource;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_fragmentuserslist_creategroup:
                createGroup();
                break;
            case R.id.button_fragmentuserslist_addusers:
                addUsers();
                break;
            case R.id.button_fragmentuserslist_joingroup:
                joinGroup();
                break;
        }
        refreshList();
    }

    /**
     * Reads list of users from firebase and refresh it in RecyclerView
     */
    private void refreshList() {
        Log.v(TAG, "refreshList started");
    }

    /**
     * Shows a dialog with a list of all available users
     * Users extracted from Google+ circles using appropriate API
     * Set of selected users will be added to current group with state 'Joining'
     * Every joined user will receive a notification with invitation to join
     */
    private void addUsers() {
        Log.v(TAG, "addUsers started");
        showDialog.set(true);
    }

    /**
     * Joins users to selected group
     * Shows a dialog window with all available groups (for joining)
     * If user select one, he will be joined to selected group
     */
    private void joinGroup() {
        Log.v(TAG, "joinGroup started");

        users.add(User.getFakeUser());
        users.add(User.getFakeUser());
        notifyChange();
    }

    /**
     * Creates new group in firebase DB.
     * Current user (who created the group) becomes an Admin of this group
     */
    private void createGroup() {
        Timber.v("createGroup started");

    }

    public void createNewGroup(String groupName) {
        Timber.v("Create group: " + groupName);
        User currentUser = SharedPrefsUtil.getCurrentUser(mContext);

        FamilyTrackDataSource dataSource = new FamilyTrackRepository(null);
        dataSource.createGroup(new Group(groupName), currentUser.getUserUuid(), null);
    }

    /**
     * Starts loading data according to group membership of the user
     * ViewModel will populate the view if current user is member of any group
     * @param user - User object representing current user
     */
    public void start(User user) {
        mCurrentUser = user;
        if (mCurrentUser.getStatusId() != User.USER_JOINED) {
            return;
        }

        mIsDataLoading = true;
        loadUsersList();
    }

    /**
     *
     */
    private void loadUsersList() {
        mRepository.getGroupByUuid(mCurrentUser.getGroupUuid(),
                new FamilyTrackDataSource.GetGroupByUuidCallback() {
                    @Override
                    public void onGetGroupByUuidCompleted(FirebaseResult<Group> result) {
                        populateUserListFromDbResult(result);
                        mIsDataLoading = false;
                    }
                });
    }

    /**
     *
     * @param result
     */
    private void populateUserListFromDbResult(FirebaseResult<Group> result) {
        if (result.getData() != null && result.getData().getMembers() != null) {
            for (User user : result.getData().getMembers().values()) {
                this.users.add(user);
            }
        }
    }


}
