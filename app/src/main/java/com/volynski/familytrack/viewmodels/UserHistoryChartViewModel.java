package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableArrayMap;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableList;

import com.volynski.familytrack.R;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.Location;
import com.volynski.familytrack.data.models.firebase.Membership;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import timber.log.Timber;


/**
 * Created by DmitryVolynski on 25.09.2017.
 */

public class UserHistoryChartViewModel extends AbstractViewModel {
    public final static String UI_CONTEXT = "UserHistoryChartViewModel";

    private User mSelectedUser;

    public ObservableBoolean redrawChart = new ObservableBoolean(false);
    public final ObservableList<UserListItemViewModel> viewModels = new ObservableArrayList<>();
    public Map<Integer, String> userStatistic =
            new TreeMap<Integer, String>(new Comparator<Integer>() {
        public int compare(Integer o1, Integer o2) {
            return o2.compareTo(o1);
        }
    });

    // toggle buttons array
    public final ObservableArrayMap<String, Boolean> toggleButtons = new ObservableArrayMap<>();
    private UserListNavigator mNavigator;

    public UserHistoryChartViewModel(Context context,
                                     String currentUserUuid,
                                     FamilyTrackDataSource dataSource) {
        super(context, currentUserUuid, dataSource);
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
     *
     */
    public void start() {

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
                        loadUsersList(mCurrentUser.getActiveMembership().getGroupUuid());
                    } else {
                        isDataLoading.set(false);
                    }
                } else {
                    Timber.v(String.format(mContext.getString(R.string.ex_user_with_uuid_not_found), mCurrentUserUuid));
                    isDataLoading.set(false);
                }
            }
        });

    }

    /**
     * Get group members from DB and populates mUsers object for the view
     * @param groupUuid - group Id to get members of
     */
    private void loadUsersList(String groupUuid) {
        mRepository.getGroupByUuid(groupUuid, false,
                new FamilyTrackDataSource.GetGroupByUuidCallback() {
                    @Override
                    public void onGetGroupByUuidCompleted(FirebaseResult<Group> result) {
                        populateUserListFromDbResult(result);
                        isDataLoading.set(false);
                    }
                });
    }

    /**
     * Converts Firebase result (group with members) into ObservableList<User>
     * Users with state=USER_JOINED will be included. They are joined to the group and could be tracked
     * @param result - Firebase result of getGroupByUuid
     */
    private void populateUserListFromDbResult(FirebaseResult<Group> result) {
        viewModels.clear();
        if (result.getData() != null && result.getData().getMembers() != null) {
            for (User user : result.getData().getMembers().values()) {
                if (user.getActiveMembership() != null &&
                        user.getActiveMembership().getStatusId() == Membership.USER_JOINED) {
                    this.viewModels.add(new UserListItemViewModel(mContext, user, mNavigator, UI_CONTEXT));
                }
            }
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
    }

    public User getSelectedUser() {
        return mSelectedUser;
    }

    public void selectUser(User user) {
        mSelectedUser = user;
        for (UserListItemViewModel vm : viewModels) {
            vm.checked.set(vm.getUser().getUserUuid().equals(user.getUserUuid()));
        }
        setupChart();
    }

    private void setupChart() {
        if (mSelectedUser == null) {
            Timber.v(mContext.getString(R.string.ex_selected_user_is_null));
            return;
        }

        long now = Calendar.getInstance().getTimeInMillis();
        long start = getTrackPeriodStart();
        mRepository.getUserTrack(mSelectedUser.getUserUuid(), start, now,
                new FamilyTrackDataSource.GetUserTrackCallback() {
                    @Override
                    public void onGetUserTrackCompleted(FirebaseResult<List<Location>> result) {
                        prepareChartData(result.getData());
                        // just switch the value to inform that it's nessesary to redraw the path
                        redrawChart.set(!redrawChart.get());
                    }
                });
    }

    /**
     * Reads a list of user locations and creates summary data for
     * top 10 most visited locations. Summary data stored in userStatistic
     * @param data
     */
    private void prepareChartData(List<Location> data) {

        userStatistic.clear();
        HashMap<String, Integer> tmp = new HashMap<>();
        if (data == null) {
            Timber.v(mContext.getString(R.string.ex_non_null_data_expected));
            return;
        }
        for (Location loc : data) {
            String locName = loc.getKnownLocationName();
            if (!tmp.containsKey(locName)) {
                tmp.put(locName, 0);
            } else {
                int n = (int) tmp.get(locName);
                tmp.remove(locName);
                tmp.put(locName, ++n);
            }
        }

        for (String key : tmp.keySet()) {
            userStatistic.put(tmp.get(key), key);
        }
        int i = 0;
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
