package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableBoolean;
import android.view.MenuItem;
import android.view.View;

import com.volynski.familytrack.R;
import com.volynski.familytrack.adapters.RecyclerViewListAdapterOnClickHandler;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 05.10.2017.
 */

public class UserMembershipItemViewModel extends BaseObservable implements RecyclerViewListAdapterOnClickHandler {
    private Context mContext;
    private Group mGroup;
    private UserListNavigator mNavigator;
    public final ObservableBoolean isActive = new ObservableBoolean(false);

    public UserMembershipItemViewModel(Context context, Group group,
                                 UserListNavigator navigator) {
        mContext = context;
        mGroup = group;
        mNavigator = navigator;
    }

    @Override
    public void onClick(int itemId, View v) {
    }

    /**
     * Creates a list of viewModels for every user
     * These viewModels should be used to present each user in a list
     *
     * @param context
     * @param groups - list of Group objects
     * @return list of {@link UserMembershipItemViewModel}
     */
    public static List<UserMembershipItemViewModel> createViewModels(Context context, List<Group> groups) {
        List<UserMembershipItemViewModel> result = new ArrayList<>();
        if (groups != null) {
            // TODO need to review this code
            for (Group group : groups) {
                result.add(new UserMembershipItemViewModel(context, group, null /*navigator*/));
            }
        }
        return result;
    }


    public void setNavigator(UserListNavigator mNavigator) {
        this.mNavigator = mNavigator;
    }

    public Group getGroup() {
        return mGroup;
    }

    public void setGroup(Group mGroup) {
        this.mGroup = mGroup;
        notifyChange();
    }
}
