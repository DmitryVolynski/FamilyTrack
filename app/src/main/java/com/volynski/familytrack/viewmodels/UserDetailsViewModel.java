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
        /*
        mUserUuid = userUuid;
        mCurrentUserUuid = currentUserUuid;
        mContext = context.getApplicationContext();
        mRepository = dataSource;
        mNavigator = navigator;
*/

        setupSpinnerEntries();
/*
        userRole.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(android.databinding.Observable sender, int propertyId) {
                int i = 0;
            }
        });
*/
    }

    private void setupSpinnerEntries() {
        spinnerEntries.addAll(Arrays.asList(mContext.getResources().getStringArray(R.array.spinnerEntries)));
    }


    /**
     * Starts loading data from contact list of the phone
     */
    public void start() {
        if (mUserUuid.equals("")) {
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
                if (result.getData() == null) {
                    Timber.v("User with uuid=" + mCurrentUserUuid + " not found ");
                    isDataLoading.set(false);
                    return;
                }
                mCurrentUser = result.getData();
                mRepository.getUserByUuid(mUserUuid, new FamilyTrackDataSource.GetUserByUuidCallback() {
                    @Override
                    public void onGetUserByUuidCompleted(FirebaseResult<User> result) {
                        isDataLoading.set(false);
                        User u = result.getData();
                        if (u == null) {
                            Timber.v("User with uuid=" + mUserUuid + " not found ");
                            return;
                        }
                        if (u.getActiveMembership() != null) {
                            activeGroup.set(u.getActiveMembership().getGroupName());
                            mActiveGroupUuid = u.getActiveMembership().getGroupUuid();
                            userRole.set(u.getActiveMembership().getRoleName());
                            adminPermissions.set(u.getActiveMembership().getRoleId() == Membership.ROLE_ADMIN);
                        } else {
                            adminPermissions.set(false);
                            activeGroup.set("Not set");
                        }
                        user.set(u);
                        distance.set(Location.getDistance(mCurrentUser, u));
                    }
                });
            }
        });
    }

    public void updateUser() {
        if (!validateUserData()) {
            return;
        }
        mRepository.updateUser(user.get(), new FamilyTrackDataSource.UpdateUserCallback() {
            @Override
            public void onUpdateUserCompleted(FirebaseResult<String> result) {
                mNavigator.dbopCompleted(result.getData(), "User details updated");
            }
        });
    }

    public boolean validateUserData() {
        int nErrors = 0;
        if (user.get() == null) {
            return true;
        }
        if (user.get().getFamilyName().equals("")) {
            familyNameError.set("First name cannot be empty");
            nErrors++;
        } else {
            familyNameError.set(null);
        }
        if (user.get().getGivenName().equals("")) {
            givenNameError.set("Given name cannot be empty");
            nErrors++;
        } else {
            givenNameError.set(null);
        }
        if (user.get().getDisplayName().equals("")) {
            displayNameError.set("Display name cannot be empty");
            nErrors++;
        } else {
            displayNameError.set(null);
        }
        if (user.get().getPhone().equals("")) {
            phoneError.set("Phone cannot be empty");
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
