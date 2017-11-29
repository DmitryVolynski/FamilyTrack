package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Observable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableFloat;

import com.volynski.familytrack.R;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.Location;
import com.volynski.familytrack.data.models.firebase.Membership;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.utils.NetworkUtil;
import com.volynski.familytrack.views.navigators.UserDetailsNavigator;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 07.09.2017.
 */

public class UserDetailsViewModel extends AbstractViewModel {
    public ObservableField<User> user = new ObservableField<User>();

    public ObservableBoolean isDataLoading = new ObservableBoolean(false);
    public ObservableArrayList<String> spinnerEntries = new ObservableArrayList<>();
    public ObservableField<String> userRole = new ObservableField<>();
    public ObservableField<String> activeGroup = new ObservableField<>();
    public ObservableField<String> distance = new ObservableField<>();

    public ObservableField<String> familyNameError = new ObservableField<>();
    public ObservableField<String> givenNameError = new ObservableField<>();
    public ObservableField<String> displayNameError = new ObservableField<>();
    public ObservableField<String> phoneError = new ObservableField<>();

    private String mActiveGroupUuid;

    private String mUserUuid = "";
    private UserDetailsNavigator mNavigator;

    public UserDetailsViewModel(Context context,
                                String currentUserUuid,
                                FamilyTrackDataSource dataSource) {
        super(context, currentUserUuid, dataSource);
        setupSpinnerEntries();
    }

    private void setupSpinnerEntries() {
        spinnerEntries.addAll(Arrays.asList(mContext.getResources().getStringArray(R.array.spinnerEntries)));
    }


    /**
     * Starts loading data from contact list of the phone
     */
    public void start() {
        if (mUserUuid.equals("")) {
            Timber.e(mContext.getString(R.string.ex_useruuid_is_empty));
            return;
        }

        if (isCreatedFromViewHolder()) {
            return;
        }

        isDataLoading.set(true);


        mRepository.getUserByUuid(mCurrentUserUuid, new FamilyTrackDataSource.GetUserByUuidCallback() {
            @Override
            public void onGetUserByUuidCompleted(FirebaseResult<User> result) {
                if (result.getData() == null) {
                    Timber.v(String.format(mContext.getString(R.string.ex_user_with_uuid_not_found), mCurrentUserUuid));
                    isDataLoading.set(false);
                    return;
                }
                mCurrentUser = result.getData();
                adminPermissions.set(mCurrentUser.getActiveMembership().getRoleId() ==
                        Membership.ROLE_ADMIN);

                mRepository.getUserByUuid(mUserUuid, new FamilyTrackDataSource.GetUserByUuidCallback() {
                    @Override
                    public void onGetUserByUuidCompleted(FirebaseResult<User> result) {
                        isDataLoading.set(false);
                        User u = result.getData();
                        if (u == null) {
                            Timber.v(String.format(mContext.getString(R.string.ex_user_with_uuid_not_found), mUserUuid));
                            return;
                        }
                        if (u.getActiveMembership() != null) {
                            activeGroup.set(u.getActiveMembership().getGroupName());
                            mActiveGroupUuid = u.getActiveMembership().getGroupUuid();
                            userRole.set(u.getActiveMembership().getRoleName());
                        } else {
                            activeGroup.set(mContext.getString(R.string.group_not_set));
                        }
                        user.set(u);
                        distance.set(Location.getDistance(mCurrentUser, u));
                    }
                });
            }
        });
    }

    public void updateUser() {
        if (!NetworkUtil.networkUp(mContext)) {
            snackbarText.set(mContext.getString(R.string.network_not_available));
            return;
        }

        if (!validateUserData()) {
            return;
        }
        mRepository.updateUser(user.get(), new FamilyTrackDataSource.UpdateUserCallback() {
            @Override
            public void onUpdateUserCompleted(FirebaseResult<String> result) {
                mNavigator.dbopCompleted(result.getData(), mContext.getString(R.string.msg_user_details_updated));
            }
        });
    }

    public boolean validateUserData() {
        int nErrors = 0;
        if (user.get() == null) {
            return true;
        }
        if (user.get().getFamilyName().equals("")) {
            familyNameError.set(mContext.getString(R.string.msg_first_name_is_empty));
            nErrors++;
        } else {
            familyNameError.set(null);
        }
        if (user.get().getGivenName().equals("")) {
            givenNameError.set(mContext.getString(R.string.msg_given_name_is_empty));
            nErrors++;
        } else {
            givenNameError.set(null);
        }
        if (user.get().getDisplayName().equals("")) {
            displayNameError.set(mContext.getString(R.string.msg_display_name_is_empty));
            nErrors++;
        } else {
            displayNameError.set(null);
        }
        if (user.get().getPhone().equals("")) {
            phoneError.set(mContext.getString(R.string.msg_phone_is_empty));
            nErrors++;
        } else {
            phoneError.set(null);
        }
        return (nErrors == 0);
    }

    public void test() {
        int i = 0;
    }

    /**
     * Removes current user from group.
     * Membership records will be physically deleted from
     *  - registered_users/<userUuid>/memberships/<groupUuid>
     *  - groups/<groupUuid>/members/<userUuid>
     */
    public void removeUser() {
        mRepository.removeUserFromGroup(mActiveGroupUuid,
                user.get().getUserUuid(),
                new FamilyTrackDataSource.RemoveUserFromGroupCallback() {
                    @Override
                    public void onRemoveUserFromGroupCompleted(FirebaseResult<String> result) {
                        mNavigator.dbopCompleted(result.getData(), "User removed");
                    }
                }
        );
    }

    public UserDetailsNavigator getNavigator() {
        return mNavigator;
    }

    public void setNavigator(UserDetailsNavigator navigator) {
        this.mNavigator = navigator;
    }

    public String getUserUuid() {
        return mUserUuid;
    }

    public void setUserUuid(String userUuid) {
        this.mUserUuid = userUuid;
    }

}
