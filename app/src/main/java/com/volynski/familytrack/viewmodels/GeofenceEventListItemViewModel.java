package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.view.MenuItem;
import android.view.View;

import com.volynski.familytrack.R;
import com.volynski.familytrack.adapters.RecyclerViewListAdapterOnClickHandler;
import com.volynski.familytrack.data.models.firebase.GeofenceEvent;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 30.10.2017.
 */

public class GeofenceEventListItemViewModel extends BaseObservable
            implements RecyclerViewListAdapterOnClickHandler

    {
        private Context mContext;
        private UserListNavigator mNavigator;
        private String mUiContext;
        public final ObservableBoolean checked = new ObservableBoolean(false);
        public final ObservableField<GeofenceEvent> event = new ObservableField<>();

    public GeofenceEventListItemViewModel(Context context,
                                          GeofenceEvent geofenceEvent,
                                          UserListNavigator navigator,
                                          String uiContext) {
        mContext = context;
        this.event.set(geofenceEvent);
        mNavigator = navigator;
        mUiContext = uiContext;
    }

    @Override
    public void onClick(int itemId, View v) {
        mNavigator.eventClicked(event.get(), mUiContext);
    }

    public void setNavigator(UserListNavigator mNavigator) {
        this.mNavigator = mNavigator;
    }

}
