package com.volynski.familytrack.services;

import android.content.Context;
import android.support.annotation.NonNull;

import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.utils.SharedPrefsUtil;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 19.10.2017.
 */

public class SettingsTask extends Thread {
    private final SettingsTaskCallback mCallback;
    private final FamilyTrackDataSource mDataSource;
    private String mUserUuid;
    private Context mContext;

    public SettingsTask(String userUuid, Context context, @NonNull SettingsTaskCallback callback) {
        mUserUuid = userUuid;
        mContext = context.getApplicationContext();
        mCallback = callback;
        mDataSource = new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(mContext), mContext);
    }

    @Override
    public void run() {
        final Group oldRec = SharedPrefsUtil.getActiveGroup(mContext);

        mDataSource.getUserByUuid(mUserUuid, new FamilyTrackDataSource.GetUserByUuidCallback() {
            @Override
            public void onGetUserByUuidCompleted(FirebaseResult<User> result) {
                if (result.getData() != null && result.getData().getActiveMembership() != null) {
                    String groupUuid = result.getData().getActiveMembership().getGroupUuid();
                    mDataSource.getGroupByUuid(groupUuid, new FamilyTrackDataSource.GetGroupByUuidCallback() {
                        @Override
                        public void onGetGroupByUuidCompleted(FirebaseResult<Group> result) {
                            int newInterval = changeSettings(oldRec, result.getData());
                            mCallback.onTaskCompleted(newInterval);
                        }
                    });
                } else {
                    // remove settings if user not found or doesn't exist as member of any group
                    Timber.v(String.format("User '%1$s' not found/not a member of any group. Settings cleared", mUserUuid));
                    SharedPrefsUtil.removeActiveGroup(mContext);
                    mCallback.onTaskCompleted(0);
                }
            }
        });
    }

    private int changeSettings(Group oldRec, Group newRec) {
        int newInterval = 0;

        if (newRec == null) {
            Timber.v(String.format("Unexpected error. User '%1$s' has null active group. Settings not changed", mUserUuid));
        } else {
            Timber.v("Replacing group in preferences");
            SharedPrefsUtil.setActiveGroup(mContext, newRec);
            if (oldRec != null && oldRec.getSettings().getSettingsUpdateInterval() !=
                    newRec.getSettings().getSettingsUpdateInterval()) {
                newInterval = newRec.getSettings().getSettingsUpdateInterval(); // convert minutes to seconds
                Timber.v("New settings interval detected:" + newInterval);
            }
        }
        return newInterval;
    }

/*
    @Override
    protected void finalize() throws Throwable {
        Timber.v("finalize");
        super.finalize();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

*/

}

