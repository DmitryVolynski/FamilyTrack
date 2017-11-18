package com.volynski.familytrack.data.models.firebase;

import com.google.android.gms.location.Geofence;
import com.google.firebase.database.Exclude;

import java.util.Calendar;

/**
 * Created by DmitryVolynski on 25.10.2017.
 */

public class GeofenceEvent {
    private String mEventUuid;
    private long mTimestamp;
    private String mUserUuid;
    private String mDisplayName;
    private String mFamilyName;
    private String mGivenName;
    private Zone mZone;
    private int mEventTypeId;

    public GeofenceEvent() {}

    public GeofenceEvent(long mTimestamp, String mUserUuid, String mDisplayName,
                         String mFamilyName, String mGivenName, Zone mZone,
                         int mEventTypeId) {
        this.mTimestamp = mTimestamp;
        this.mUserUuid = mUserUuid;
        this.mDisplayName = mDisplayName;
        this.mFamilyName = mFamilyName;
        this.mGivenName = mGivenName;
        this.mZone = mZone;
        this.mEventTypeId = mEventTypeId;
    }

    public GeofenceEvent(String mUserUuid, String mDisplayName,
                         String mFamilyName, String mGivenName, Zone mZone,
                         int mEventTypeId) {
        this.mTimestamp = Calendar.getInstance().getTimeInMillis();
        this.mUserUuid = mUserUuid;
        this.mDisplayName = mDisplayName;
        this.mFamilyName = mFamilyName;
        this.mGivenName = mGivenName;
        this.mZone = mZone;
        this.mEventTypeId = mEventTypeId;
    }
    public String getEventUuid() {
        return mEventUuid;
    }

    public void setEventUuid(String mEventUuid) {
        this.mEventUuid = mEventUuid;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long mTimestamp) {
        this.mTimestamp = mTimestamp;
    }

    public String getUserUuid() {
        return mUserUuid;
    }

    public void setUserUuid(String mUserUuid) {
        this.mUserUuid = mUserUuid;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String mDisplayName) {
        this.mDisplayName = mDisplayName;
    }

    public String getFamilyName() {
        return mFamilyName;
    }

    public void setFamilyName(String mFirstName) {
        this.mFamilyName = mFirstName;
    }

    public String getGivenName() {
        return mGivenName;
    }

    public void setGivenName(String mGivenName) {
        this.mGivenName = mGivenName;
    }

    public Zone getZone() {
        return mZone;
    }

    public void setZone(Zone mZone) {
        this.mZone = mZone;
    }

    public int getEventTypeId() {
        return mEventTypeId;
    }

    public void setEventTypeId(int mEventTypeId) {
        this.mEventTypeId = mEventTypeId;
    }

    @Exclude
    public String getTimestampAsString() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(mTimestamp);
        return String.format("%1$tF %1tT", c.getTime());
    }

    @Exclude
    public String getEventTypeName() {
        switch (mEventTypeId) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "Enter";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "Exit";
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return "Dwell";
            default:
                return "Unknown";
        }
    }
}
