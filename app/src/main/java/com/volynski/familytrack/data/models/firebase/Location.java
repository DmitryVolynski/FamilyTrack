package com.volynski.familytrack.data.models.firebase;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

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

    @Exclude
    public String getLatLngAsString() {
        return String.format("%1$f, %2$f", mLatitude, mLongitude);
    }

    @Exclude
    public String getPeriodAsString() {
        String suffix = "day(s)";
        long now = Calendar.getInstance().getTimeInMillis();
        long n = TimeUnit.MILLISECONDS.toDays(now - mTimestamp);
        if (n < 1) {
            n = TimeUnit.MILLISECONDS.toMinutes(now - mTimestamp);
            suffix = "min";
        }
        return String.format("%1$d %2$s", n, suffix);
    }

    @Exclude
    public static String getDistance(User mCurrentUser, User u) {
        String result = "-";
        String suffix = "km";
        if (!(mCurrentUser.getLastKnownLocation() == null ||
                u.getLastKnownLocation() == null)) {
            double d = Math.round(distance(mCurrentUser.getLastKnownLocation().getLatLng(),
                    u.getLastKnownLocation().getLatLng()));
            if (d < 1) {
                d = Math.round(d * 1000);
                suffix = "m";
            } else {
                d = Math.round(d * 10) / 10;
            }
            result = String.format("%1$.1f %2$s", d, suffix);
        }
        return result;
    }

    private static double distance(LatLng p1, LatLng p2) {
        double theta = p1.longitude - p2.longitude;
        double dist = Math.sin(deg2rad(p1.latitude))
                * Math.sin(deg2rad(p2.latitude))
                + Math.cos(deg2rad(p1.latitude))
                * Math.cos(deg2rad(p2.latitude))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
