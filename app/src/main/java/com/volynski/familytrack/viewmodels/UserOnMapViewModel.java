package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Observable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableArrayMap;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableDouble;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ObservableList;
import android.databinding.ObservableMap;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.Location;
import com.volynski.familytrack.data.models.firebase.Membership;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.data.models.firebase.Zone;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 22.08.2017.
 */

public class UserOnMapViewModel extends AbstractViewModel {
    public final static String UI_CONTEXT = "UserOnMapViewModel";
    public final static int EM_NONE = 0;
    public final static int EM_EDIT = 1;
    public final static int EM_NEW = 2;


/*
    private final Context mContext;
    private String mCurrentUserUuid = "";
    private User mCurrentUser;
*/
    private User mSelectedUser;
//    private boolean mIsDataLoading = false;
//    private FamilyTrackDataSource mRepository;

    public ObservableBoolean zoneDbOpCompleted = new ObservableBoolean(false);
    public ObservableBoolean redrawZones = new ObservableBoolean(false);
    public ObservableBoolean redrawPath = new ObservableBoolean(false);
    public ObservableBoolean redrawMarkers = new ObservableBoolean(false);
    // for map markers
    public final ObservableList<User> users = new ObservableArrayList<>();

    // horizontal list of users
    public final ObservableList<UserListItemViewModel> viewModels = new ObservableArrayList<>();

    // toggle buttons array
    public final ObservableArrayMap<String, Boolean> toggleButtons = new ObservableArrayMap<>();

    public final ObservableList<Location> path = new ObservableArrayList<>();
    public final ObservableMap<String, Zone> zones = new ObservableArrayMap<>();

    // observable fields for editing new/existing zone
    public final ObservableField<String> zoneName = new ObservableField<>();
    public final ObservableInt zoneRadius = new ObservableInt(Zone.DEFAULT_RADIUS);
    public final ObservableDouble zoneCenterLatitude = new ObservableDouble(0);
    public final ObservableDouble zoneCenterLongitude = new ObservableDouble(0);
    public final ObservableInt zoneEditMode = new ObservableInt(EM_NONE);

    // text for snackbar
    public ObservableField<String> snackbarText = new ObservableField<>();


    private UserListNavigator mNavigator;
    private String mZoneKey = "";

    public UserOnMapViewModel(Context context,
                              String currentUserUuid,
                              FamilyTrackDataSource dataSource,
                              UserListNavigator navigator) {
        super(context, currentUserUuid, dataSource);
/*
        mCurrentUserUuid = currentUserUuid;
        mContext = context.getApplicationContext();
        mRepository = dataSource;
*/
        mNavigator = navigator;

        initToggleButtons();
    }

    private void initToggleButtons() {
        toggleButtons.put("OFF", true);
        toggleButtons.put("1H", false);
        toggleButtons.put("8H", false);
        toggleButtons.put("1D", false);
        toggleButtons.put("1W", false);
    }

    /**
     * Starts loading data according to group membership of the user (groupUuid)
     * ViewModel will populate the view if current user is member of any group
     */
    public void start() {

        if (mCurrentUserUuid.equals("")) {
            Timber.e("Can't start viewmodel. UserUuid is empty");
            return;
        }

        isDataLoading.set(true);
        mRepository.getUserByUuid(mCurrentUserUuid, new FamilyTrackDataSource.GetUserByUuidCallback() {
            @Override
            public void onGetUserByUuidCompleted(FirebaseResult<User> result) {
                if (result.getData() != null) {
                    mCurrentUser = result.getData();
                    if (mCurrentUser.getActiveMembership() != null) {
                        populateObservables(mCurrentUser.getActiveMembership().getGroupUuid());
                    }
                } else {
                    Timber.v("User with uuid=" + mCurrentUserUuid + " not found ");
                }
            }
        });

    }

    /**
     * Read group info and populates users & zones (geofences)
     * @param groupUuid - group Id to read
     */
    private void populateObservables(String groupUuid) {
        mRepository.getGroupByUuid(groupUuid, true,
                new FamilyTrackDataSource.GetGroupByUuidCallback() {
                    @Override
                    public void onGetGroupByUuidCompleted(FirebaseResult<Group> result) {
                        populateUserListFromDbResult(result);
                        populateZonesFromDbResult(result);
                        isDataLoading.set(false);
                    }
                });
    }

    /**
     * Populates ObservableList<User> from Firebase result (group with members )
     * Users with state=USER_JOINED will be included. They are joined to the group and could be tracked
     * @param result - Firebase result (Group object) of getGroupByUuid
     */
    private void populateUserListFromDbResult(FirebaseResult<Group> result) {
        if (result.getData() != null && result.getData().getMembers() != null) {
            this.users.clear();
            this.viewModels.clear();
            for (User user : result.getData().getMembers().values()) {
                if (user.getActiveMembership() != null &&
                        user.getActiveMembership().getStatusId() == Membership.USER_JOINED) {
                    this.users.add(user);
                    this.viewModels.add(new UserListItemViewModel(mContext, user, mNavigator, UI_CONTEXT));
                }
            }
            setupUserTrack();
            redrawMarkers.set(!redrawMarkers.get());
        }
    }

    /**
     * Populates ObservableMap<Zone> from Firebase result (group with members & zones)
     * Users with state=USER_JOINED will be included. They are joined to the group and could be tracked
     * @param result - Firebase result (Group object) of getGroupByUuid
     */
    private void populateZonesFromDbResult(FirebaseResult<Group> result) {
        if (result.getData() != null && result.getData().getGeofences() != null) {
            zones.clear();
            zones.putAll(result.getData().getGeofences());
            redrawZones.set(!redrawZones.get());
        }
    }
    public void setNavigator(UserListNavigator mNavigator) {
        this.mNavigator = mNavigator;
        if (viewModels != null) {
            for (UserListItemViewModel listItemViewModel : viewModels) {
                listItemViewModel.setNavigator(mNavigator);
            }
        }
    }

    public void onToggleButtonClick(String period) {
        for (String buttonKey : toggleButtons.keySet()) {
            toggleButtons.put(buttonKey, period.equals(buttonKey));
        }
        setupUserTrack();
    }

    private void setupUserTrack() {
        if (mSelectedUser == null) {
            Timber.v("Selected user==null, can't show track");
            return;
        }

        long now = Calendar.getInstance().getTimeInMillis();
        long start = getTrackPeriodStart();

        if (start > 0) {
            mRepository.getUserTrack(mSelectedUser.getUserUuid(), start, now,
                    new FamilyTrackDataSource.GetUserTrackCallback() {
                        @Override
                        public void onGetUserTrackCompleted(FirebaseResult<List<Location>> result) {
                            path.clear();
                            path.addAll(result.getData());
                            // just switch the value to inform that it's nessesary to redraw the path
                            redrawPath.set(!redrawPath.get());
                        }
                    });
        }
    }

    public User getSelectedUser() {
        return mSelectedUser;
    }

    public void selectUser(User user) {
        boolean unselect = false;
        if (user != null) {
            if (mSelectedUser != null &&
                    mSelectedUser.getUserUuid().equals(user.getUserUuid())) {
                // unselect user if selected user was clicked again
                mSelectedUser = null;
                unselect = true;
            } else {
                mSelectedUser = user;
            }
            for (UserListItemViewModel vm : viewModels) {
                vm.checked.set(vm.getUser().getUserUuid().equals(user.getUserUuid()) && !unselect);
            }
            setupUserTrack();
        }
    }

    public void startEditZone(String zoneKey) {
        if (zones.containsKey(zoneKey)) {
            mZoneKey = zoneKey;
            Zone editZone = zones.get(zoneKey);
            zoneName.set(editZone.getName());
            zoneRadius.set(editZone.getRadius());
            zoneCenterLatitude.set(editZone.getLatitude());
            zoneCenterLongitude.set(editZone.getLongitude());
            zoneEditMode.set(EM_EDIT);

            for (UserListItemViewModel item : viewModels) {
                item.checked.set(editZone.getTrackedUsers().contains(item.getUser().getUserUuid()));
            }
        }
    }

    public void startNewZone() {
        zoneName.set("New Zone");
        zoneRadius.set(Zone.DEFAULT_RADIUS);
        zoneEditMode.set(EM_NEW);
    }

    public void saveZone() {
        List<String> trackedUsers = getTrackedUsersList();
        Zone zone = new Zone(mZoneKey, zoneName.get(),
                new LatLng(zoneCenterLatitude.get(), zoneCenterLongitude.get()),
                zoneRadius.get(), trackedUsers);
        if (zoneEditMode.get() == EM_NEW) {
            mRepository.createZone(mCurrentUser.getActiveMembership().getGroupUuid(),
                    zone, new FamilyTrackDataSource.CreateZoneCallback() {
                @Override
                public void onCreateZoneCompleted(FirebaseResult<String> result) {
                    snackbarText.set("Zone created");
                    zoneEditMode.set(EM_NONE);
                    updateZonesList();
                    zoneDbOpCompleted.set(!zoneDbOpCompleted.get());
                }
            });
        } else if (zoneEditMode.get() == EM_EDIT) {
            mRepository.updateZone(mCurrentUser.getActiveMembership().getGroupUuid(),
                    zone, new FamilyTrackDataSource.UpdateZoneCallback() {
                        @Override
                        public void onUpdateZoneCompleted(FirebaseResult<String> result) {
                            snackbarText.set("Zone updated");
                            zoneEditMode.set(EM_NONE);
                            updateZonesList();
                            zoneDbOpCompleted.set(!zoneDbOpCompleted.get());
                        }
                    });
        } else {
            Timber.v("Attempt to save zone in unknown mode");
        }
    }

    /**
     * Scans @viewmodels for users, who should be tracked in current zone
     * (@UserListItemViewModel.checked should be true)
     * @return
     */
    private List<String> getTrackedUsersList() {
        List<String> result = new ArrayList<>();
        for (UserListItemViewModel item : viewModels) {
            if (item.checked.get()) {
                result.add(item.getUser().getUserUuid());
            }
        }
        return result;
    }

    public void removeZone() {
        if (zoneEditMode.get() != EM_EDIT || mZoneKey.equals("")) {
            Timber.v("Zone key is empty or mode != EM_EDIT. Unable to remove zone");
            return;
        }

        mRepository.removeZone(mCurrentUser.getActiveMembership().getGroupUuid(), mZoneKey,
                new FamilyTrackDataSource.RemoveZoneCallback() {
                    @Override
                    public void onRemoveZoneCompleted(FirebaseResult<String> result) {
                        snackbarText.set("Zone removed");
                        updateZonesList();
                        zoneEditMode.set(EM_NONE);
                        zoneDbOpCompleted.set(!zoneDbOpCompleted.get());
                    }
                });
    }
    /**
     * Reads all group-defined zones into local list
     * and change value of ObeservableBoolean redrawZones
     * to force view to redraw all zones
     */
    private void updateZonesList() {
        mRepository.getGroupByUuid(mCurrentUser.getActiveMembership().getGroupUuid(), false,
                new FamilyTrackDataSource.GetGroupByUuidCallback() {
                    @Override
                    public void onGetGroupByUuidCompleted(FirebaseResult<Group> result) {
                        populateZonesFromDbResult(result);
                    }
                });
    }

    public void cancelZoneEdit() {
        zoneEditMode.set(EM_NONE);
    }

    private long getTrackPeriodStart() {
        String key = "";

        for (String k : toggleButtons.keySet()) {
            if (toggleButtons.get(k)) {
                key = k;
                break;
            }
        }

        long result = -1;
        Calendar now = Calendar.getInstance();
        switch (key) {
            case "OFF":
                result = 0;
                break;
            case "1H":
                now.add(Calendar.HOUR, -1); ;
                break;
            case "8H":
                now.add(Calendar.HOUR, -8); ;
                break;
            case "1D":
                now.add(Calendar.DATE, -1); ;
                break;
            case "1W":
                now.add(Calendar.DATE, -7); ;
                break;
            default:
                result = 0;
        }
        return (result < 0 ? now.getTimeInMillis() : 0);
    }
}
