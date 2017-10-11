package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import com.volynski.familytrack.R;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.User;

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

    private User mCurrentUser;
    private final Context mContext;
    private String mUserUuid = "";
    private String mCurrentUserUuid = "";
    private boolean mIsDataLoading = false;
    //private UserListNavigator mNavigator;
    private FamilyTrackDataSource mRepository;

    public UserDetailsViewModel(Context context,
                                String currentUserUuid,
                                String userUuid,
                                FamilyTrackDataSource dataSource) {
        mUserUuid = userUuid;
        mCurrentUserUuid = currentUserUuid;
        mContext = context.getApplicationContext();
        mRepository = dataSource;
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
                    }
                });
            }
        });
    }

    public void test() {
        int i = 0;
    }
}
