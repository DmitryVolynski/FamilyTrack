package com.volynski.familytrack.data.models.firebase;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

import java.util.Calendar;

/**
 * Created by DmitryVolynski on 24.08.2017.
 *
 * Model class to store current location of user
 *
 */

public class Location {
    public static final String FIELD_TIMESTAMP = "timestamp";

    private long mTimestamp;
    private double mLongitude;
    private double mLatitude;
    private String mKnownLocationName;
    private String mAddress;
    private int mBatteryLevel;

    public Location() {}

    public Location(double longitude, double latitude,
                    String knownLocationName, String address, int batteryLevel) {
        mTimestamp = Calendar.getInstance().getTimeInMillis();
        mLongitude = longitude;
        mLatitude = latitude;
        mKnownLocationName = knownLocationName;
        mBatteryLevel = batteryLevel;
        mAddress = address;
    }

    public Location(long timestamp, double longitude,
                    double latitude, String knownLocationName, String address, int batteryLevel) {
        mTimestamp = timestamp;
        mLongitude = longitude;
        mLatitude = latitude;
        mKnownLocationName = knownLocationName;
        mBatteryLevel = batteryLevel;
        mAddress = address;
    }

    public Location(Calendar calendar, double longitude,
                    double latitude, String knownLocationName, String address, int batteryLevel) {
        mTimestamp = calendar.getTimeInMillis();
        mLongitude = longitude;
        mLatitude = latitude;
        mKnownLocationName = knownLocationName;
        mBatteryLevel = batteryLevel;
        mAddress = address;
    }

    @Exclude
    public Calendar getCalendar() {
        Calendar result = Calendar.getInstance();
        result.setTimeInMillis(mTimestamp);
        return result;
    }

    @Exclude
    public LatLng getLatLng() {
        return new LatLng(mLatitude, mLongitude);
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

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public Location clone() {
        return new Location(mTimestamp, mLongitude,
                mLatitude, mKnownLocationName, mAddress, mBatteryLevel);
    }

    @Exclude
    public String getTextForSnippet() {
        String result = "";
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mTimestamp);
        return String.format("%1$tF %1$tR %2$s(%3$d%%)",
                cal.getTime(), mAddress, mBatteryLevel);
    }

}
