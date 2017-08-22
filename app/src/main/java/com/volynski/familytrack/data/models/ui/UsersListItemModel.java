package com.volynski.familytrack.data.models.ui;

import com.volynski.familytrack.data.models.firebase.User;

/**
 * Created by DmitryVolynski on 22.08.2017.
 */

public class UsersListItemModel {
    private User mUser;
    private long mDistance;
    private String mLocationName;
    private int mBatteryLevel;

    public User getUser() {
        return mUser;
    }

    public void setUser(User mUser) {
        this.mUser = mUser;
    }

    public long getDistance() {
        return mDistance;
    }

    public void setDistance(long mDistance) {
        this.mDistance = mDistance;
    }

    public String getLocationName() {
        return mLocationName;
    }

    public void setLocationName(String mLocationName) {
        this.mLocationName = mLocationName;
    }

    public int getBatteryLevel() {
        return mBatteryLevel;
    }

    public void setBatteryLevel(int mBatteryLevel) {
        this.mBatteryLevel = mBatteryLevel;
    }
}
