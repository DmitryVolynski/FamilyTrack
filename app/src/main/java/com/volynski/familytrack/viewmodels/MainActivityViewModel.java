package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.User;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 04.10.2017.
 */

public class MainActivityViewModel extends BaseObservable {
    public ObservableField<User> user = new ObservableField<User>();

    public ObservableBoolean isDataLoading = new ObservableBoolean(false);
    public ObservableArrayList<String> spinnerEntries = new ObservableArrayList<>();
    public ObservableField<String> activeGroup = new ObservableField<>();

    private final Context mContext;
    private String mUserUuid = "";
    private boolean mIsDataLoading = false;
    private FamilyTrackDataSource mRepository;

    public MainActivityViewModel(Context context,
                                String userUuid,
                                FamilyTrackDataSource dataSource) {
        mUserUuid = userUuid;
        mContext = context.getApplicationContext();
        mRepository = dataSource;
    }

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

}
