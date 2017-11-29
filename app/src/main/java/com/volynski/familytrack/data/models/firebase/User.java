package com.volynski.familytrack.data.models.firebase;

import com.google.firebase.database.Exclude;
import com.google.gson.Gson;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by DmitryVolynski on 16.08.2017.
 * Model class for a User
 */

public class User {

    // TODO описать все json-поля таким образов
    public static final String FIELD_ROLE_ID = "roleId";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_LAST_KNOWN_LOCATION = "lastKnownLocation";
    public static final String FIELD_STATUS_ID = "statusId";
    public static final String FIELD_PHONE = "phone";

    private String mFamilyName;
    private String mGivenName;
    private String mDisplayName;
    private String mPhotoUrl;
    private String mEmail;
    private String mPhone;
    private String mUserUuid;
    private Map<String, Membership> mMemberships;
    private Location mLastKnownLocation;

    public User() {}

    public User(String uuid, String familyName, String givenName, String displayName, String photoUrl,
                String email, String phone, Map<String, Membership> memberships, Location location) {
        this.mUserUuid = uuid;
        this.mFamilyName = familyName;
        this.mGivenName = givenName;
        this.mDisplayName = displayName;
        this.mPhotoUrl = photoUrl;
        this.mEmail = email;
        this.mPhone = phone;
        this.mLastKnownLocation = location;
    }

    public static User getFakeUser() {
        return new User("1234567890", "Volynski", "Dmitry", "", "", "volynski@hotmail.com",  "0987654321", null, null);
    }

    @Exclude
    public String getUserUuid() {
        return mUserUuid;
    }

    public void setUserUuid(String uuid) {
        this.mUserUuid = uuid;
    }

    public String getFamilyName() {
        return mFamilyName;
    }

    public void setFamilyName(String familyName) {
        this.mFamilyName = familyName;
    }

    public String getGivenName() {
        return mGivenName;
    }

    public void setGivenName(String givenName) {
        this.mGivenName = givenName;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.mPhotoUrl = photoUrl;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        this.mEmail = email;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String phone) {
        this.mPhone = phone;
    }

    public String ToJson() {
        return (new Gson()).toJson(this);
    }

    public static User getInstanceFromJson(String jsonUser) {
        return (new Gson()).fromJson(jsonUser, User.class);
    }

    /*
    public User clone() {
        HashMap<String, Membership> map = new HashMap<>();
        for (Membership membership : mMemberships)
        return new User(mUserUuid, mFamilyName,
                mGivenName, mDisplayName, mPhotoUrl, mEmail, mPhone,
                map, mLastKnownLocation.clone());
    }
    */

    public Location getLastKnownLocation() {
        return mLastKnownLocation;
    }

    public void setLastKnownLocation(Location mLastKnownLocation) {
        this.mLastKnownLocation = mLastKnownLocation;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String mDisplayName) {
        this.mDisplayName = mDisplayName;
    }

    public Map<String, Membership> getMemberships() {
        return mMemberships;
    }

    public void setMemberships(Map<String, Membership> mMemberships) {
        this.mMemberships = mMemberships;
    }

    @Exclude
    public Membership getActiveMembership() {
        Membership result = null;
        if (mMemberships != null) {
            for (String key : mMemberships.keySet()) {
                Membership membership = mMemberships.get(key);
                if (membership.getStatusId() == Membership.USER_JOINED) {
                    result = membership;
                    break;
                }
            }
        }
        return result;
    }

    public void addMembership(Membership membership) {
        if (mMemberships == null) {
            mMemberships = new HashMap<>();
        }
        mMemberships.put(membership.getGroupUuid(), membership);
    }

    @Exclude
    public String getTextForSnippet() {
        String result = "";
        if (mLastKnownLocation != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(mLastKnownLocation.getTimestamp());
            result = String.format("%1$tF %1$tR %2$s(%3$d%%)",
                    cal.getTime(), mLastKnownLocation.getAddress(), mLastKnownLocation.getBatteryLevel());
        }
        return result;
    }

    @Exclude
    public Membership getMembershipForGroup(String groupUuid) {
        Membership result = null;
        if (mMemberships != null) {
            for (String key : mMemberships.keySet()) {
                Membership membership = mMemberships.get(key);
                if (membership.getGroupUuid().equals(groupUuid)) {
                    result = membership;
                    break;
                }
            }
        }
        return result;
    }

    @Exclude
    public User cloneForGroupNode(String groupUuid) {
        HashMap<String, Membership> map = new HashMap<>();

        User clonedUser = new User(mUserUuid, mFamilyName, mGivenName, mDisplayName, mPhotoUrl, mEmail, mPhone,
                map, mLastKnownLocation);

        if (mMemberships != null) {
            for (String key : mMemberships.keySet()) {
                Membership membership = mMemberships.get(key);
                if (membership.getGroupUuid().equals(groupUuid)) {
                    clonedUser.addMembership(membership.clone());
                    break;
                }
            }
        }
        return clonedUser;
    }
}
