package com.volynski.familytrack.data.models.firebase;

import com.google.firebase.database.Exclude;
import com.google.gson.Gson;

/**
 * Created by DmitryVolynski on 16.08.2017.
 * Model class for a User
 */

public class User {
    public static final int ADMIN_ROLE = 1;
    public static final int MEMBER_ROLE = 2;

    public static final int USER_INVITED = 1;
    public static final int USER_JOINED = 2;

    private String mFamilyName;
    private String mGivenName;
    private String mPhotoUrl;
    private String mEmail;
    private String mPhone;
    private String mUserUuid;
    private GroupUser mUserGroup;

    public static User getTestUser() {
        return new User("Volynsky", "Dmitry", "http://someurl", "volynski@hotmail.com", "1234567", null);
    }

    public User() {}

    public User(String familyName, String givenName, String photoUrl,
                String email, String phone, GroupUser groupUser) {
        //this.mUserUuid = UUID.randomUUID().toString();
        this.mFamilyName = familyName;
        this.mGivenName = givenName;
        this.mPhotoUrl = photoUrl;
        this.mEmail = email;
        this.mPhone = phone;
        this.mUserGroup = groupUser;
    }

    public User(String uuid, String familyName, String givenName, String photoUrl,
                String email, String phone, GroupUser groupUser) {
        this.mUserUuid = uuid;
        this.mFamilyName = familyName;
        this.mGivenName = givenName;
        this.mPhotoUrl = photoUrl;
        this.mEmail = email;
        this.mPhone = phone;
        this.mUserGroup = groupUser;
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

    public GroupUser getUserGroup() {
        return mUserGroup;
    }

    public void setUserGroup(GroupUser mUserGroup) {
        this.mUserGroup = mUserGroup;
    }

    public String ToJson() {
        return (new Gson()).toJson(this);
    }

    public static User getInstanceFromJson(String jsonUser) {
        return (new Gson()).fromJson(jsonUser, User.class);
    }
}
