package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.view.MenuItem;
import android.view.View;

import com.volynski.familytrack.adapters.RecyclerViewListAdapterOnClickHandler;
import com.volynski.familytrack.data.models.MembershipListItem;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 06.09.2017.
 */

public class MembershipListItemViewModel extends BaseObservable
        implements PopupMenuListener, RecyclerViewListAdapterOnClickHandler {

    private Context mContext;
    private UserMembershipViewModel mMasterViewModel;

    public final ObservableField<MembershipListItem> item = new ObservableField<>();
    public final ObservableBoolean checked = new ObservableBoolean(false);
    public final ObservableBoolean isActive = new ObservableBoolean(false);

    public MembershipListItemViewModel(Context context,
                                       MembershipListItem item, boolean isActive,
                                       UserMembershipViewModel masterViewModel) {
        mContext = context;
        this.item.set(item);
        this.isActive.set(isActive);
        mMasterViewModel = masterViewModel;
    }

    public void changeMembership() {
        if (mMasterViewModel == null) {
            Timber.e("mMasterViewModel is null. Can't execute startChangeMembership command");
            return;
        }
        mMasterViewModel.startChangeMembership(this);
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
    public static List<MembershipListItemViewModel> createViewModels(Context context, List<Group> groups) {
        List<MembershipListItemViewModel> result = new ArrayList<>();
        if (groups != null) {
            // TODO need to review this code
            // Here I did silent navigator assignment, this is not good for common cases
            //UserListNavigator navigator = (UserListNavigator)context;
            for (Group group : groups) {
                result.add(new MembershipListItemViewModel(context, group, false));
            }
        }
        return result;
    }
     */

    /*
    public void setNavigator(UserListNavigator mNavigator) {
        this.mNavigator = mNavigator;
    }
    */

}
