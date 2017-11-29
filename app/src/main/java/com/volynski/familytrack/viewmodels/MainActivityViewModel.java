package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import com.volynski.familytrack.R;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.Membership;
import com.volynski.familytrack.data.models.firebase.User;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 04.10.2017.
 */

public class MainActivityViewModel extends AbstractViewModel {
    public ObservableField<User> user = new ObservableField<User>();

    public ObservableArrayList<String> spinnerEntries = new ObservableArrayList<>();
    public ObservableField<String> activeGroup = new ObservableField<>();

    public MainActivityViewModel(Context context,
                                String userUuid,
                                FamilyTrackDataSource dataSource) {
        super(context, userUuid, dataSource);
    }

    public void start() {
        if (mCurrentUserUuid.equals("")) {
            Timber.e(mContext.getString(R.string.ex_useruuid_is_empty));
            return;
        }

        isDataLoading.set(true);

        mRepository.getUserByUuid(mCurrentUserUuid, new FamilyTrackDataSource.GetUserByUuidCallback() {
            @Override
            public void onGetUserByUuidCompleted(FirebaseResult<User> result) {
                User u = result.getData();
                if (u != null) {
                    if (u.getActiveMembership() != null) {
                        activeGroup.set(u.getActiveMembership().getGroupName());
                        adminPermissions.set(u.getActiveMembership().getRoleId() == Membership.ROLE_ADMIN);
                    }
                    user.set(result.getData());
                    notifyChange();
                } else {
                    Timber.v(String.format(mContext.getString(R.string.ex_user_with_uuid_not_found), mCurrentUserUuid));
                }
            }
        });
    }

}
