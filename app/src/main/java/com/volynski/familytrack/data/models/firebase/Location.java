package com.volynski.familytrack.data.models.firebase;

import com.google.firebase.database.Exclude;

import java.util.Calendar;

/**
 * Created by DmitryVolynski on 24.08.2017.
 *
 * Model class to store current location of user
 *
 */

public class Location {
    private long mTimestamp;
    private double mLongitude;
    private double mLatitude;
    private String mKnownLocationName;
    private int mBatteryLevel;

    public Location() {}

    public Location(double longitude, double latitude,
                    String knownLocationName, int batteryLevel) {
        mTimestamp = Calendar.getInstance().getTimeInMillis();
        mLongitude = longitude;
        mLatitude = latitude;
        mKnownLocationName = knownLocationName;
        mBatteryLevel = batteryLevel;
    }

    public Location(long timestamp, double longitude,
                    double latitude, String knownLocationName, int batteryLevel) {
        mTimestamp = timestamp;
        mLongitude = longitude;
        mLatitude = latitude;
        mKnownLocationName = knownLocationName;
        mBatteryLevel = batteryLevel;
    }

    public Location(Calendar calendar, double longitude,
                    double latitude, String knownLocationName, int batteryLevel) {
        mTimestamp = calendar.getTimeInMillis();
        mLongitude = longitude;
        mLatitude = latitude;
        mKnownLocationName = knownLocationName;
        mBatteryLevel = batteryLevel;
    }

    @Exclude
    public Calendar getCalendar() {
        Calendar result = Calendar.getInstance();
        result.setTimeInMillis(mTimestamp);
        return result;
    }


    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
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

    public Location clone() {
        return new Location(mTimestamp, mLongitude,
                mLatitude, mKnownLocationName, mBatteryLevel);
    }
}
