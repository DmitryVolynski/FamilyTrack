package com.volynski.familytrack.data.models.firebase;

import com.google.firebase.database.Exclude;
import com.google.gson.Gson;

/**
 * Created by DmitryVolynski on 16.08.2017.
 * Model class for a User
 */

public class User {
    public static final int ROLE_ADMIN = 1;
    public static final int ROLE_MEMBER = 2;
    public static final int ROLE_UNDEFINED = 3;

    public static final int USER_INVITED = 1;
    public static final int USER_JOINED = 2;
    public static final int USER_CREATED = 3;


    private String mFamilyName;
    private String mGivenName;
    private String mPhotoUrl;
    private String mEmail;
    private String mPhone;
    private String mUserUuid;
    private int mRoleId;
    private int mStatusId;
    private String mGroupUuid;
    private UserLocation mLastKnownLocation;

    public User() {}

    public User(String uuid, String familyName, String givenName, String photoUrl,
                String email, String phone, int roleId, int statusId, String groupUuid, UserLocation location) {
        this.mUserUuid = uuid;
        this.mFamilyName = familyName;
        this.mGivenName = givenName;
        this.mPhotoUrl = photoUrl;
        this.mEmail = email;
        this.mPhone = phone;
        this.mRoleId = roleId;
        this.mStatusId = statusId;
        this.mGroupUuid = groupUuid;
        this.mLastKnownLocation = location;
    }

    public static User getFakeUser() {
        return new User("1234567890", "Volynski", "Dmitry", "", "jkdg",  "123", 1, 1, "0987654321", null);
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

    public User clone() {
        return new User(mUserUuid, mFamilyName,
                mGivenName, mPhotoUrl, mEmail, mPhone,
                mRoleId, mStatusId, mGroupUuid, mLastKnownLocation);
    }

    public UserLocation getLastKnownLocation() {
        return mLastKnownLocation;
    }

    public void setLastKnownLocation(UserLocation mLastKnownLocation) {
        this.mLastKnownLocation = mLastKnownLocation;
    }

    public int getRoleId() {
        return mRoleId;
    }

    public void setRoleId(int mRoleId) {
        this.mRoleId = mRoleId;
    }

    public int getStatusId() {
        return mStatusId;
    }

    public void setStatusId(int mStatusId) {
        this.mStatusId = mStatusId;
    }

    public String getGroupUuid() {
        return mGroupUuid;
    }

    public void setGroupUuid(String groupUuid) {
        this.mGroupUuid = groupUuid;
    }

}
