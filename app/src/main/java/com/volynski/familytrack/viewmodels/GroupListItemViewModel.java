package com.volynski.familytrack.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.view.View;

import com.volynski.familytrack.adapters.RecyclerViewListAdapterOnClickHandler;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 21.11.2017.
 */

public class GroupListItemViewModel
        extends BaseObservable
        implements RecyclerViewListAdapterOnClickHandler {
    private String mGroupUuid;
    public ObservableField<String> groupName = new ObservableField<>("");
    public ObservableBoolean selected = new ObservableBoolean(false);
    private FirstTimeUserViewModel mMasterViewModel;

    public GroupListItemViewModel(String groupUuid, String groupName, boolean selected,
                                  FirstTimeUserViewModel masterViewModel) {
        mGroupUuid = groupUuid;
        this.groupName.set(groupName);
        this.selected.set(selected);
        mMasterViewModel = masterViewModel;
    }

    @Override
    public void onClick(int itemId, View v) {
        if (mMasterViewModel != null) {
            mMasterViewModel.groupSelected(mGroupUuid);
        } else {
            Timber.v("mMasterViewModel is null");
        }

    }

    public String getGroupUuid() {
        return mGroupUuid;
    }
}
