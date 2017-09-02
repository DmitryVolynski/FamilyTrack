package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Observable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableList;
import android.util.Log;
import android.view.View;

import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 02.09.2017.
 */

public class FirstTimeUserViewModel extends BaseObservable {

    private final static String TAG = UserListViewModel.class.getSimpleName();
    private final Context mContext;
    private User mCurrentUser;
    private boolean mIsDataLoading = false;
    private UserListNavigator mNavigator;
    private FamilyTrackDataSource mRepository;


    public FirstTimeUserViewModel(Context context,
                                FamilyTrackDataSource dataSource) {
        mContext = context.getApplicationContext();
        mRepository = dataSource;
    }

    /**
     * Starts loading data from contact list of the phone
     */
    public void start(User user) {
        mCurrentUser = user;
        if (mCurrentUser.getStatusId() != User.USER_JOINED) {
            return;
        }

        mIsDataLoading = true;
        //loadUsersList();
    }


    public void setNavigator(UserListNavigator mNavigator) {
        this.mNavigator = mNavigator;
    }
}


