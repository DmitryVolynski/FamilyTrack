package com.volynski.familytrack.data.model.firebase;

import com.google.firebase.database.Exclude;

/**
 * Created by DmitryVolynski on 16.08.2017.
 * Model class for a User
 */

public class User {
    public static final int ADMIN_ROLE = 1;
    public static final int MEMBER_ROLE = 2;

    public static final int USER_INVITED = 1;
    public static final int USER_JOINED = 2;

    private String mGroupUuid;
    private String mFamilyName;
    private String mGivenName;
    private String mPhotoUrl;
    private String mEmail;
    private String mPhone;
    private String mUserUuid;
    private int mRoleId;
    private int mStatusId;

    public static User getTestUser() {
        return new User("Volynsky", "Dmitry", "http://someurl", "volynski@hotmail.com", "1234567", ADMIN_ROLE, "");
    }

    public User() {}

    public User(String familyName, String givenName, String photoUrl,
                String email, String phone, int roleId, String groupUuid) {
        //this.mUserUuid = UUID.randomUUID().toString();
        this.mFamilyName = familyName;
        this.mGivenName = givenName;
        this.mPhotoUrl = photoUrl;
        this.mEmail = email;
        this.mPhone = phone;
        this.mRoleId = roleId;
        this.mGroupUuid = groupUuid;
    }

    public User(String uuid, String familyName, String givenName, String photoUrl,
                String email, String phone, int roleId, String groupUuid) {
        this.mUserUuid = uuid;
        this.mFamilyName = familyName;
        this.mGivenName = givenName;
        this.mPhotoUrl = photoUrl;
        this.mEmail = email;
        this.mPhone = phone;
        this.mRoleId = roleId;
        this.mGroupUuid = groupUuid;
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

    @Exclude
    public int getRoleId() {
        return mRoleId;
    }

    public void setRoleId(int roleId) {
        this.mRoleId = roleId;
    }

    @Exclude
    public String getGroupUuid() {
        return mGroupUuid;
    }

    public void setGroupUuid(String mGroupUuid) {
        this.mGroupUuid = mGroupUuid;
    }

    @Exclude
    public int getStatusId() {
        return mStatusId;
    }

    public void setStatusId(int mStatusId) {
        this.mStatusId = mStatusId;
    }
}
