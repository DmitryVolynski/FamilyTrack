package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableField;

import com.volynski.familytrack.R;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.Location;
import com.volynski.familytrack.data.models.firebase.Settings;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.views.navigators.SettingsNavigator;
import com.volynski.familytrack.views.navigators.UserDetailsNavigator;

import java.util.Arrays;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 13.10.2017.
 */

public class SettingsViewModel extends AbstractViewModel {

    private SettingsNavigator mNavigator;
    public ObservableField<Settings> settings = new ObservableField<>();

    public SettingsViewModel(Context context,
                             String currentUserUuid,
                             FamilyTrackDataSource dataSource) {
        super(context, currentUserUuid, dataSource);
    }

    /**
     * Starts loading data from contact list of the phone
     */
    public void start() {
        if (mCurrentUserUuid.equals("")) {
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
                if (mCurrentUser.getActiveMembership() != null) {
                    String groupUuid = mCurrentUser.getActiveMembership().getGroupUuid();
                    mRepository.getSettingsByGroupUuid(groupUuid,
                            new FamilyTrackDataSource.GetSettingsByGroupUuidCallback() {
                        @Override
                        public void onGetSettingsByGroupUuidCompleted(FirebaseResult<Settings> result) {
                            settings.set(result.getData());
                            isDataLoading.set(false);
                        }
                    });
                }
                isDataLoading.set(false);
            }
        });
    }

    public void updateSettings() {
        if (mCurrentUser.getActiveMembership() != null) {
            String groupUuid = mCurrentUser.getActiveMembership().getGroupUuid();
            mRepository.updateSettingsByGroupUuid(groupUuid, settings.get(),
                    new FamilyTrackDataSource.UpdateSettingsByGroupUuidCallback() {
                        @Override
                        public void onUpdateSettingsByGroupUuidCompleted(FirebaseResult<String> result) {
                            if (result.getData().equals(FirebaseResult.RESULT_OK)) {
                                mNavigator.updateCompleted(result.getData());
                            }
                        }
                    });
        }
    }

    public SettingsNavigator getNavigator() {
        return mNavigator;
    }

    public void setNavigator(SettingsNavigator navigator) {
        this.mNavigator = navigator;
    }


}
