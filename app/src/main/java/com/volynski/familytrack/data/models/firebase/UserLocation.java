package com.volynski.familytrack.data.models.firebase;

import java.security.Timestamp;
import java.util.Calendar;

/**
 * Created by DmitryVolynski on 24.08.2017.
 *
 * Model class to store current location of user
 *
 */

public class UserLocation {
    private Timestamp mTimestamp;
    private double mLongitude;
    private double mLatitude;
    private String mKnownLocationName;
    private int mBatteryLevel;

    public Timestamp getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(Timestamp mTimestamp) {
        this.mTimestamp = mTimestamp;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public String getKnownLocationName() {
        return mKnownLocationName;
    }

    public void setKnownLocationName(String mKnownLocationName) {
        this.mKnownLocationName = mKnownLocationName;
    }

    public int getBatteryLevel() {
        return mBatteryLevel;
    }

    public void setBatteryLevel(int mBatteryLevel) {
        this.mBatteryLevel = mBatteryLevel;
    }
}
