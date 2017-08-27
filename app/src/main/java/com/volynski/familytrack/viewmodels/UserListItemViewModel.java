package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;

import com.volynski.familytrack.data.models.firebase.User;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 25.08.2017.
 */

public class UserListItemViewModel extends BaseObservable {
    private Context mContext;
    private User mUser;

    public UserListItemViewModel(Context context, User user) {
        mContext = context;
        mUser = user;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User mUser) {
        this.mUser = mUser;
        notifyChange();
    }

    /**
     * Creates a list of viewModels for every user
     * These viewModels should be used to present each user in a list
     *
     * @param context
     * @param users - list of users
     * @return list of {@link UserListItemViewModel}
     */
    public static List<UserListItemViewModel> createViewModels(Context context, List<User> users) {
        List<UserListItemViewModel> result = new ArrayList<>();
        if (users != null) {
            for (User user : users) {
                result.add(new UserListItemViewModel(context, user));
            }
        }
        return result;
    }

    public void showPopupMenu() {
        Timber.v("Show popup");
    }
}
