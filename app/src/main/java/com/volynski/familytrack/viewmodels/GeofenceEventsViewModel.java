package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableList;
import android.util.Log;

import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.GeofenceEvent;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.data.models.firebase.Zone;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 30.10.2017.
 */

public class GeofenceEventsViewModel extends BaseObservable {
    public final static String UI_CONTEXT = GeofenceEventsViewModel.class.getSimpleName();
    private final static String TAG = GeofenceEventsViewModel.class.getSimpleName();
    private final Context mContext;
    private String mCurrentUserUuid = "";
    private User mCurrentUser;
    private boolean mIsDataLoading;
    private FamilyTrackDataSource mRepository;
    private UserListNavigator mNavigator;

    public ObservableBoolean redrawZone = new ObservableBoolean(false);
    public Zone zoneToShow;

    public final ObservableList<GeofenceEventListItemViewModel>
            viewModels = new ObservableArrayList<>();

    private List<GeofenceEvent> mEvents = new ArrayList<>();


    public GeofenceEventsViewModel(Context context,
                             String currentUserUuid,
                             FamilyTrackDataSource dataSource) {
        mCurrentUserUuid = currentUserUuid;
        mContext = context.getApplicationContext();
        mRepository = dataSource;
    }


    /**
     * Reads list of mUsers from firebase and refresh it in RecyclerView
     */
    private void refreshList() {
        Log.v(TAG, "refreshList started");
    }

    /**
     * Starts loading data according to group membership of the user
     * ViewModel will populate the view if current user is member of any group
     * @param user - User object representing current user
     */
    public void start() {

        if (mCurrentUserUuid.equals("")) {
            Timber.e("Can't start viewmodel. UserUuid is empty");
            return;
        }

        mIsDataLoading = true;
        mRepository.getUserByUuid(mCurrentUserUuid, new FamilyTrackDataSource.GetUserByUuidCallback() {
            @Override
            public void onGetUserByUuidCompleted(FirebaseResult<User> result) {
                if (result.getData() != null) {
                    mCurrentUser = result.getData();
                    loadEventsList();
                } else {
                    Timber.v("User with uuid=" + mCurrentUserUuid + " not found ");
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
                    populateViewmodels(result);
                }
            });
        }
    }

    public void setNavigator(UserListNavigator mNavigator) {
        this.mNavigator = mNavigator;
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
            }
        }
    }

    public void clearEvents() {

    }

    public void selectEvent(GeofenceEvent event) {
        zoneToShow = event.getZone();
        redrawZone.set(!redrawZone.get());

        for (GeofenceEventListItemViewModel itemViewModel : viewModels) {
            itemViewModel.checked.set(itemViewModel.event.get().getEventUuid().equals(event.getEventUuid()));
            itemViewModel.notifyChange();
        }
    }
}
