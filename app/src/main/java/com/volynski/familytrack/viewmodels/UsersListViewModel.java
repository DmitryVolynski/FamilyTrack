package com.volynski.familytrack.viewmodels;


import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableList;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.volynski.familytrack.R;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.data.models.ui.UsersListItemModel;
import com.volynski.familytrack.utils.AuthUtil;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 22.08.2017.
 */

public class UsersListViewModel
        extends BaseObservable
        implements View.OnClickListener {

    private final static String TAG = UsersListViewModel.class.getSimpleName();
    private final Context mContext;

    public final ObservableBoolean showDialog = new ObservableBoolean(false);
    public final ObservableList<UsersListItemModel> items = new ObservableArrayList<>();

    public UsersListViewModel(Context context) {
        mContext = context.getApplicationContext();
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
        User currentUser = AuthUtil.getCurrentUserFromPrefs(mContext);

        FamilyTrackDataSource dataSource = new FamilyTrackRepository(null);
        dataSource.createGroup(new Group(groupName), currentUser.getUserUuid());
    }
}
