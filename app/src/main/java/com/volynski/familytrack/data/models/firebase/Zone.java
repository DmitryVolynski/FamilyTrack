package com.volynski.familytrack.data.models.firebase;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.volynski.familytrack.StringKeys;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by DmitryVolynski on 26.09.2017.
 */

public class Zone {
    public static final int DEFAULT_RADIUS = 500;

    private String mName;
    private double mLatitude;
    private double mLongitude;
    private int mRadius;
    private long mTimestamp;
    private String mUuid;
    private List<String> mTrackedUsers = new ArrayList<>();

    public Zone() {
        //mRadius = DEFAULT_RADIUS;
        //mName = "New zone";
    }

    public Zone(String key, String name, LatLng loc, int radius, List<String> trackedUsers) {
        mUuid = key;
        mName = name;
        mLatitude = loc.latitude;
        mLongitude = loc.longitude;
        mRadius = radius;
        mTimestamp = Calendar.getInstance().getTimeInMillis();
        mTrackedUsers = trackedUsers;
    }

    public Zone(String name, LatLng loc, int radius, List<String> trackedUsers) {
        mName = name;
        mLatitude = loc.latitude;
        mLongitude = loc.longitude;
        mRadius = radius;
        mTimestamp = Calendar.getInstance().getTimeInMillis();
        mTrackedUsers = trackedUsers;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }

    public int getRadius() {
        return mRadius;
    }

    public void setRadius(int mRadius) {
        this.mRadius = mRadius;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long mTimestamp) {
        this.mTimestamp = mTimestamp;
    }

    @Exclude
    public String getUuid() {
        return mUuid;
    }

    public void setUuid(String mUuid) {
        this.mUuid = mUuid;
    }

    @Exclude
    public static String getLatLngAsString(double latitude, double longitude) {
        return String.format("%1$f/%2$f", latitude, longitude);
    }

    @Exclude
    public static String getRadiusAsString(int radius) {
        return String.format("R:%1$dm", radius);
    }

    @Exclude
    public LatLng getLatLng() {
        return new LatLng(mLatitude, mLongitude);
    }

    public List<String> getTrackedUsers() {
        return mTrackedUsers;
    }

    public void setTrackedUsers(List<String> trackedUsers) {
        this.mTrackedUsers = trackedUsers;
    }
}
