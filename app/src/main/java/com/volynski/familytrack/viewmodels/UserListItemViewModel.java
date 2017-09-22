package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableBoolean;
import android.view.MenuItem;
import android.view.View;

import com.volynski.familytrack.R;
import com.volynski.familytrack.adapters.RecyclerViewListAdapterOnClickHandler;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 25.08.2017.
 */

public class UserListItemViewModel extends BaseObservable
        implements PopupMenuListener, RecyclerViewListAdapterOnClickHandler {
    private Context mContext;
    private User mUser;
    private UserListNavigator mNavigator;
    public final ObservableBoolean checked = new ObservableBoolean(false);

    public UserListItemViewModel(Context context, User user, UserListNavigator navigator) {
        mContext = context;
        mUser = user;
        mNavigator = navigator;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User mUser) {
        this.mUser = mUser;
        notifyChange();
    }

    @Override
    public void onClick(int itemId, View v) {
        if (mUser.getLastKnownLocation() != null) {
            mNavigator.showUserOnMap(mUser);
        }
    }

    @Override
    public void menuCommand(MenuItem item) {
        if (mNavigator == null) {
            Timber.e("mNavigator is null. Navigation is not available");
            return;
        }
        switch (item.getItemId()) {
            case R.id.menuitem_userpopupmenu_remove:
                mNavigator.removeUser(mUser.getUserUuid());
                break;
            case R.id.menuitem_userpopupmenu_showonmap:
                //mNavigator.showUserOnMap(mUser.getUserUuid());
                //break;
            case R.id.menuitem_userpopupmenu_userdetails:
                mNavigator.openUserDetails(mUser.getUserUuid());
                break;
        }
    }

    /**
     * Creates a list of viewModels for every user
     * These viewModels should be used to present each user in a list
     *
     * @param context
     * @param users - list of mUsers
     * @return list of {@link UserListItemViewModel}
     */
    public static List<UserListItemViewModel> createViewModels(Context context, List<User> users) {
        List<UserListItemViewModel> result = new ArrayList<>();
        if (users != null) {
            // TODO need to review this code
            // Here I did silent navigator assignment, this is not good for common cases
            //UserListNavigator navigator = (UserListNavigator)context;
            for (User user : users) {
                result.add(new UserListItemViewModel(context, user, null /*navigator*/));
            }
        }
        return result;
    }

    public void setNavigator(UserListNavigator mNavigator) {
        this.mNavigator = mNavigator;
    }
}
