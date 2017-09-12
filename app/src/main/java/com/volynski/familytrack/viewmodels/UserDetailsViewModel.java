package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.database.Observable;
import android.databinding.BaseObservable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableList;
import android.util.Log;
import android.view.View;

import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 07.09.2017.
 */

public class UserDetailsViewModel extends BaseObservable {
    public ObservableField<User> user = new ObservableField<User>();

    public ObservableBoolean isDataLoading = new ObservableBoolean(false);
    public ObservableArrayList<String> spinnerEntries = new ObservableArrayList<>();
    public ObservableField<String> activeGroup = new ObservableField<>();

    private final Context mContext;
    private String mUserUuid = "";
    private boolean mIsDataLoading = false;
    //private UserListNavigator mNavigator;
    private FamilyTrackDataSource mRepository;

    public UserDetailsViewModel(Context context,
                                String userUuid,
                                FamilyTrackDataSource dataSource) {
        mUserUuid = userUuid;
        mContext = context.getApplicationContext();
        mRepository = dataSource;
        activeGroup.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(android.databinding.Observable sender, int propertyId) {
                int i = 0;
            }
        });
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

        mRepository.getUserByUuid(mUserUuid, new FamilyTrackDataSource.GetUserByUuidCallback() {
            @Override
            public void onGetUserByUuidCompleted(FirebaseResult<User> result) {
                User u = result.getData();
                if (u != null) {
                    if (u.getMemberships() != null) {
                        for (String key : u.getMemberships().keySet()) {
                            spinnerEntries.add(u.getMemberships().get(key).getGroupName());
                        }
                    }
                    if (u.getActiveMembership() != null) {
                        activeGroup.set(u.getActiveMembership().getGroupName());
                    }
                    user.set(result.getData());
                    notifyChange();
                } else {
                    Timber.v("User with uuid=" + mUserUuid + " not found ");
                }
            }
        });
    }

    public void test() {
        int i = 0;
    }
}
