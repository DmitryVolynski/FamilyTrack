package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableList;
import android.util.Log;

import com.volynski.familytrack.R;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.GeofenceEvent;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.data.models.firebase.Zone;
import com.volynski.familytrack.utils.NetworkUtil;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 30.10.2017.
 */

public class GeofenceEventsViewModel extends AbstractViewModel {
    public final static String UI_CONTEXT = GeofenceEventsViewModel.class.getSimpleName();
    private UserListNavigator mNavigator;

    // text for snackbar
    public ObservableField<String> snackbarText = new ObservableField<>();
    public ObservableBoolean redrawZone = new ObservableBoolean(false);
    public Zone zoneToShow;

    public final ObservableList<GeofenceEventListItemViewModel>
            viewModels = new ObservableArrayList<>();

    private List<GeofenceEvent> mEvents = new ArrayList<>();


    public GeofenceEventsViewModel(Context context,
                             String currentUserUuid,
                             FamilyTrackDataSource dataSource) {
        super(context, currentUserUuid, dataSource);
    }


    /**
     * Starts loading data according to group membership of the user
     * ViewModel will populate the view if current user is member of any group
     */
    public void start(String currentUserUuid) {
        if (!NetworkUtil.networkUp(mContext)) {
            snackbarText.set(mContext.getString(R.string.network_not_available));
            return;
        }

        if (mCurrentUserUuid.equals("")) {
            Timber.e("Can't start viewmodel. UserUuid is empty");
            return;
        }

        if (isCreatedFromViewHolder()) {
            return;
        }

        isDataLoading.set(true);
        mRepository.getUserByUuid(mCurrentUserUuid, new FamilyTrackDataSource.GetUserByUuidCallback() {
            @Override
            public void onGetUserByUuidCompleted(FirebaseResult<User> result) {
                if (result.getData() != null) {
                    mCurrentUser = result.getData();
                    loadEventsList();
                } else {
                    Timber.v(String.format(mContext.getString(R.string.ex_user_with_uuid_not_found), mCurrentUserUuid));
                    isDataLoading.set(false);
                }
            }
        });
    }

    /**
     *
     */
    private void loadEventsList() {
        if (mCurrentUser.getActiveMembership() != null) {
            mRepository.getGeofenceEventsByUserUuid(mCurrentUser.getUserUuid(),
                    new FamilyTrackDataSource.GetGeofenceEventsByUserUuidCallback() {
                @Override
                public void onGetGeofenceEventsByUserUuidCompleted(FirebaseResult<List<GeofenceEvent>> result) {
                    isDataLoading.set(false);
                    populateViewmodels(result);
                }
            });
        } else {
            isDataLoading.set(false);
        }
    }

    public void setNavigator(UserListNavigator mNavigator) {
        this.mNavigator = mNavigator;
        if (viewModels != null) {
            for (GeofenceEventListItemViewModel listItemViewModel : viewModels) {
                listItemViewModel.setNavigator(mNavigator);
            }
        }
    }

    /**
     *
     * @param result
     */
    private void populateViewmodels(FirebaseResult<List<GeofenceEvent>> result) {
        mEvents.clear();
        viewModels.clear();
        if (result.getData() != null) {
            for (GeofenceEvent event : result.getData()) {
                viewModels.add(new GeofenceEventListItemViewModel(mContext, event, mNavigator, UI_CONTEXT));
                mEvents.add(event);
            }
            if (mEvents.size() > 0) {
                selectEvent(mEvents.get(0));
            } else {
                zoneToShow = null;
                redrawZone.set(!redrawZone.get());
            }
        }
    }

    public void selectEvent(GeofenceEvent event) {
        zoneToShow = event.getZone();
        redrawZone.set(!redrawZone.get());

        for (GeofenceEventListItemViewModel itemViewModel : viewModels) {
            itemViewModel.checked.set(itemViewModel.event.get().getEventUuid().equals(event.getEventUuid()));
            itemViewModel.notifyChange();
        }
    }

    public void deleteEvents() {
        mRepository.deleteGeofenceEvents(mCurrentUserUuid, new FamilyTrackDataSource.DeleteGeofenceEventsCallback() {
            @Override
            public void onDeleteGeofenceEventsCompleted(FirebaseResult<String> result) {
                if (result.getData().equals(FirebaseResult.RESULT_OK)) {
                    snackbarText.set(mContext.getString(R.string.geofence_events_deleted));
                    loadEventsList();
                }
            }
        });
    }

    public void deleteEvent(String eventUuid) {
        mRepository.deleteGeofenceEvent(mCurrentUserUuid, eventUuid,
                new FamilyTrackDataSource.DeleteGeofenceEventsCallback() {
                    @Override
                    public void onDeleteGeofenceEventsCompleted(FirebaseResult<String> result) {
                        if (result.getData().equals(FirebaseResult.RESULT_OK)) {
                            snackbarText.set(mContext.getString(R.string.geofence_event_deleted));
                            loadEventsList();
                        }
                    }
                });
    }
}
