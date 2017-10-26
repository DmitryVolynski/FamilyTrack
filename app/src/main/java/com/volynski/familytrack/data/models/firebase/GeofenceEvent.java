package com.volynski.familytrack.data.models.firebase;

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
    private String mZoneUuid;
    private String mZoneName;
    private int mEventTypeId;

    public GeofenceEvent() {}

    public GeofenceEvent(long mTimestamp, String mUserUuid, String mDisplayName,
                         String mFamilyName, String mGivenName, String mZoneUuid,
                         String mZoneName, int mEventTypeId) {
        this.mTimestamp = mTimestamp;
        this.mUserUuid = mUserUuid;
        this.mDisplayName = mDisplayName;
        this.mFamilyName = mFamilyName;
        this.mGivenName = mGivenName;
        this.mZoneUuid = mZoneUuid;
        this.mZoneName = mZoneName;
        this.mEventTypeId = mEventTypeId;
    }

    public GeofenceEvent(String mUserUuid, String mDisplayName,
                         String mFamilyName, String mGivenName, String mZoneUuid,
                         String mZoneName, int mEventTypeId) {
        this.mTimestamp = Calendar.getInstance().getTimeInMillis();
        this.mUserUuid = mUserUuid;
        this.mDisplayName = mDisplayName;
        this.mFamilyName = mFamilyName;
        this.mGivenName = mGivenName;
        this.mZoneUuid = mZoneUuid;
        this.mZoneName = mZoneName;
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

    public String getZoneUuid() {
        return mZoneUuid;
    }

    public void setZoneUuid(String mZoneUuid) {
        this.mZoneUuid = mZoneUuid;
    }

    public String getZoneName() {
        return mZoneName;
    }

    public void setZoneName(String mZoneName) {
        this.mZoneName = mZoneName;
    }

    public int getEventTypeId() {
        return mEventTypeId;
    }

    public void setEventTypeId(int mEventTypeId) {
        this.mEventTypeId = mEventTypeId;
    }
}
