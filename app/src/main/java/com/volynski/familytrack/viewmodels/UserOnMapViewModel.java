package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableArrayMap;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableList;
import android.databinding.ObservableMap;
import android.view.View;

import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.Location;
import com.volynski.familytrack.data.models.firebase.Membership;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.views.navigators.UserListNavigator;
import com.volynski.familytrack.views.navigators.UserOnMapNavigator;

import java.util.Calendar;
import java.util.List;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 22.08.2017.
 */

public class UserOnMapViewModel extends BaseObservable {
    public final static String UI_CONTEXT = "UserOnMapViewModel";

    private final Context mContext;
    private String mCurrentUserUuid = "";
    private User mCurrentUser;
    private User mSelectedUser;
    private boolean mIsDataLoading = false;
    private FamilyTrackDataSource mRepository;

    public ObservableBoolean redrawPath = new ObservableBoolean(false);
    public ObservableBoolean redrawMarkers = new ObservableBoolean(false);
    // for map markers
    public final ObservableList<User> users = new ObservableArrayList<>();

    // horizontal list of users
    public final ObservableList<UserListItemViewModel> viewModels = new ObservableArrayList<>();

    // toggle buttons array
    public final ObservableArrayMap<String, Boolean> toggleButtons = new ObservableArrayMap<>();

    public final ObservableList<Location> path = new ObservableArrayList<>();

    private UserListNavigator mNavigator;

    public UserOnMapViewModel(Context context,
                              String currentUserUuid,
                             FamilyTrackDataSource dataSource) {
        mCurrentUserUuid = currentUserUuid;
        mContext = context.getApplicationContext();
        mRepository = dataSource;

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
                    if (mCurrentUser.getActiveMembership() != null) {
                        loadUsersList(mCurrentUser.getActiveMembership().getGroupUuid());
                    }
                } else {
                    Timber.v("User with uuid=" + mCurrentUserUuid + " not found ");
                }
            }
        });

    }

    /**
     * Get group members from DB and populates mUsers object for the view
     * @param groupUuid - group Id to get members of
     */
    private void loadUsersList(String groupUuid) {
        mRepository.getGroupByUuid(groupUuid,
                new FamilyTrackDataSource.GetGroupByUuidCallback() {
                    @Override
                    public void onGetGroupByUuidCompleted(FirebaseResult<Group> result) {
                        populateUserListFromDbResult(result);
                        mIsDataLoading = false;
                    }
                });
    }

    /**
     * Converts Firebase result (group with members) into ObservableList<User>
     * Users with state=USER_JOINED will be included. They are joined to the group and could be tracked
     * @param result - Firebase result of getGroupByUuid
     */
    private void populateUserListFromDbResult(FirebaseResult<Group> result) {
        if (result.getData() != null && result.getData().getMembers() != null) {
            for (User user : result.getData().getMembers().values()) {
                if (user.getActiveMembership().getStatusId() == Membership.USER_JOINED) {
                    this.users.add(user);
                    this.viewModels.add(new UserListItemViewModel(mContext, user, mNavigator, UI_CONTEXT));
                }
            }
            redrawMarkers.set(true);
        }
    }

    public void setNavigator(UserListNavigator mNavigator) {
        this.mNavigator = mNavigator;
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

        Calendar now = Calendar.getInstance();
        Calendar start = Calendar.getInstance();
        start.add(Calendar.DATE, -20);
        mRepository.getUserTrack(mSelectedUser.getUserUuid(), start.getTimeInMillis(), now.getTimeInMillis(),
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

    public User getSelectedUser() {
        return mSelectedUser;
    }

    public void selectUser(User user) {
        mSelectedUser = user;
        setupUserTrack();
    }
}
