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
 * Created by DmitryVolynski on 06.09.2017.
 */

public class GroupListItemViewModel extends BaseObservable
        implements PopupMenuListener, RecyclerViewListAdapterOnClickHandler {

    private Context mContext;
    private Group mGroup;
    //private UserListNavigator mNavigator;
    public final ObservableBoolean checked = new ObservableBoolean(false);

    public GroupListItemViewModel(Context context, Group group /*GroupListNavigator navigator*/) {
        mContext = context;
        mGroup = group;
        //mNavigator = navigator;
    }

    @Override
    public void onClick(int itemId, View v) {
        // item click goes to detail screen
        //mNavigator.userClicked(55.994017, 37.195024);
        Timber.v("Item " + itemId + " clicked");
    }

    @Override
    public void menuCommand(MenuItem item) {

    }

    /**
     * Creates a list of viewModels for every group
     * These viewModels should be used to present each group in a list
     *
     * @param context
     * @param groups - list of groups
     * @return list of {@link UserListItemViewModel}
     */
    public static List<GroupListItemViewModel> createViewModels(Context context, List<Group> groups) {
        List<GroupListItemViewModel> result = new ArrayList<>();
        if (groups != null) {
            // TODO need to review this code
            // Here I did silent navigator assignment, this is not good for common cases
            //UserListNavigator navigator = (UserListNavigator)context;
            for (Group group : groups) {
                result.add(new GroupListItemViewModel(context, group /*navigator*/));
            }
        }
        return result;
    }

    /*
    public void setNavigator(UserListNavigator mNavigator) {
        this.mNavigator = mNavigator;
    }
    */

    public Group getGroup() {
        return mGroup;
    }

    public void setGroup(Group mGroup) {
        this.mGroup = mGroup;
    }
}
