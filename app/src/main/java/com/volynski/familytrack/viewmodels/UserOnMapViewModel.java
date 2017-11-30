package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableArrayMap;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableDouble;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ObservableList;
import android.databinding.ObservableMap;

import com.google.android.gms.maps.model.LatLng;
import com.volynski.familytrack.R;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.Location;
import com.volynski.familytrack.data.models.firebase.Membership;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.data.models.firebase.Zone;
import com.volynski.familytrack.utils.NetworkUtil;
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

    private User mSelectedUser;

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

    private String mEditZoneUuid = "";

    public UserOnMapViewModel(Context context,
                              String currentUserUuid,
                              FamilyTrackDataSource dataSource,
                              UserListNavigator navigator) {
        super(context, currentUserUuid, dataSource);
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
        if (!NetworkUtil.networkUp(mContext)) {
            snackbarText.set(mContext.getString(R.string.network_not_available));
            return;
        }

        if (mCurrentUserUuid.equals("")) {
            Timber.e(mContext.getString(R.string.ex_useruuid_is_empty));
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
                    } else {
                        // user is not a member of any group
                        clearViewData();
                    }
                } else {
                    Timber.v(String.format(mContext.getString(R.string.ex_user_with_uuid_not_found), mCurrentUserUuid));
                }
            }
        });

    }

    private void clearViewData() {
        users.clear();
        viewModels.clear();
        zones.clear();
        path.clear();
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
        boolean createViewModels = viewModels.size() == 0;
        if (result.getData() != null && result.getData().getMembers() != null) {
            this.users.clear();
            //this.viewModels.clear();
            for (User user : result.getData().getMembers().values()) {
                if (user.getActiveMembership() != null &&
                        user.getActiveMembership().getStatusId() == Membership.USER_JOINED) {
                    this.users.add(user);
                    if (createViewModels)
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
        if (!NetworkUtil.networkUp(mContext)) {
            snackbarText.set(mContext.getString(R.string.network_not_available));
            return;
        }

        if (mSelectedUser == null) {
            Timber.v(mContext.getString(R.string.ex_selected_user_is_null));
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

    public void selectUser(User user, boolean forceSelect) {
        boolean unselect = false;
        if (user != null) {
            if (mSelectedUser != null && !forceSelect &&
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
            mEditZoneUuid = zoneKey;
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
        zoneName.set(mContext.getString(R.string.new_zone_name));
        zoneRadius.set(Zone.DEFAULT_RADIUS);
        zoneEditMode.set(EM_NEW);
        for (UserListItemViewModel item : viewModels) {
            item.checked.set(false);
        }
    }

    public void saveZone() {
        if (!NetworkUtil.networkUp(mContext)) {
            snackbarText.set(mContext.getString(R.string.network_not_available));
            return;
        }

        if (mCurrentUser.getActiveMembership() != null &&
                mCurrentUser.getActiveMembership().getRoleId() != Membership.ROLE_ADMIN) {
            snackbarText.set(mContext.getString(R.string.admins_only_message));
            return;
        }

        if (zoneCenterLatitude.get() == 0 && zoneCenterLongitude.get() == 0) {
            mNavigator.showPopupDialog(mContext.getString(R.string.saving_geofence_dialog_title),
                    mContext.getString(R.string.msg_select_a_point));
            return;
        }

        if (zoneName.get().equals("")) {
            mNavigator.showPopupDialog(mContext.getString(R.string.saving_geofence_dialog_title),
                    mContext.getString(R.string.msg_specify_geofence_name));
            return;
        }

        if (zoneRadius.get() == 0) {
            mNavigator.showPopupDialog(mContext.getString(R.string.saving_geofence_dialog_title),
                    mContext.getString(R.string.msg_specify_zone_radius));
            return;
        }

        List<String> trackedUsers = getTrackedUsersList();
        Zone zone = new Zone(mEditZoneUuid, zoneName.get(),
                new LatLng(zoneCenterLatitude.get(), zoneCenterLongitude.get()),
                zoneRadius.get(), trackedUsers);
        if (zoneEditMode.get() == EM_NEW) {
            mRepository.createZone(mCurrentUser.getActiveMembership().getGroupUuid(),
                    zone, new FamilyTrackDataSource.CreateZoneCallback() {
                @Override
                public void onCreateZoneCompleted(FirebaseResult<String> result) {
                    snackbarText.set(mContext.getString(R.string.msg_zone_created));
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
                            snackbarText.set(mContext.getString(R.string.msg_zone_updated));
                            zoneEditMode.set(EM_NONE);
                            updateZonesList();
                            zoneDbOpCompleted.set(!zoneDbOpCompleted.get());
                        }
                    });
        } else {
            Timber.v(mContext.getString(R.string.ex_saving_zone_in_unknown_mode));
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
        if (!NetworkUtil.networkUp(mContext)) {
            snackbarText.set(mContext.getString(R.string.network_not_available));
            return;
        }

        if (zoneEditMode.get() != EM_EDIT || mEditZoneUuid.equals("")) {
            Timber.v(mContext.getString(R.string.ex_zone_key_is_empty));
            return;
        }

        mRepository.removeZone(mCurrentUser.getActiveMembership().getGroupUuid(), mEditZoneUuid,
                new FamilyTrackDataSource.RemoveZoneCallback() {
                    @Override
                    public void onRemoveZoneCompleted(FirebaseResult<String> result) {
                        snackbarText.set(mContext.getString(R.string.msg_zone_removed));
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

    public String getEditZoneUuid() {
        return mEditZoneUuid;
    }
}
