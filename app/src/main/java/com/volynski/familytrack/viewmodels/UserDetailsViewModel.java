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
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.views.navigators.UserDetailsNavigator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 07.09.2017.
 */

public class UserDetailsViewModel extends BaseObservable {
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

    private User mCurrentUser;
    private final Context mContext;
    private String mUserUuid = "";
    private String mCurrentUserUuid = "";
    private boolean mIsDataLoading = false;
    //private UserListNavigator mNavigator;
    private FamilyTrackDataSource mRepository;
    private UserDetailsNavigator mNavigator;

    public UserDetailsViewModel(Context context,
                                String currentUserUuid,
                                String userUuid,
                                FamilyTrackDataSource dataSource,
                                UserDetailsNavigator navigator) {
        mUserUuid = userUuid;
        mCurrentUserUuid = currentUserUuid;
        mContext = context.getApplicationContext();
        mRepository = dataSource;
        mNavigator = navigator;
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

        mIsDataLoading = true;


        mRepository.getUserByUuid(mCurrentUserUuid, new FamilyTrackDataSource.GetUserByUuidCallback() {
            @Override
            public void onGetUserByUuidCompleted(FirebaseResult<User> result) {
                if (result.getData() == null) {
                    Timber.v("User with uuid=" + mCurrentUserUuid + " not found ");
                    return;
                }
                mCurrentUser = result.getData();
                mRepository.getUserByUuid(mUserUuid, new FamilyTrackDataSource.GetUserByUuidCallback() {
                    @Override
                    public void onGetUserByUuidCompleted(FirebaseResult<User> result) {
                        User u = result.getData();
                        if (u == null) {
                            Timber.v("User with uuid=" + mUserUuid + " not found ");
                            return;
                        }
                        if (u.getActiveMembership() != null) {
                            activeGroup.set(u.getActiveMembership().getGroupName());
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
                mNavigator.updateCompleted(result.getData());
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
}
