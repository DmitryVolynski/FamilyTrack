package com.volynski.familytrack.adapters;

import android.databinding.ObservableField;

import com.google.android.gms.common.data.DataBufferObserver;
import com.volynski.familytrack.R;
import com.volynski.familytrack.data.FamilyTrackException;
import com.volynski.familytrack.data.models.MembershipListItem;
import com.volynski.familytrack.viewmodels.MembershipListItemViewModel;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 08.10.2017.
 */

public class GroupsAndUsersItemType implements ItemTypesResolver {
    @Override
    public int getItemViewType(Object o) {
        if (!(o instanceof MembershipListItemViewModel)) {
            Timber.v("Invalid item class: " + o.getClass().getSimpleName() + ". Expected MembershipListItemViewModel");
            return -1;
        }
        return ((MembershipListItemViewModel)o).item.get().getType();
    }

    @Override
    public int getItemViewLayoutId(int viewType) {
        int layoutId = -1;
        switch (viewType) {
            case MembershipListItem.TYPE_GROUP:
                layoutId = R.layout.group_list_item;
                break;
            case MembershipListItem.TYPE_USER:
                layoutId = R.layout.user_membership_list_item;
                break;
        }
        return layoutId;
    }
}
