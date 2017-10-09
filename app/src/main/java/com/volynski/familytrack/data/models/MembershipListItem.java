package com.volynski.familytrack.data.models;

import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DmitryVolynski on 08.10.2017.
 */

public class MembershipListItem {
    public static final int TYPE_GROUP = 0;
    public static final int TYPE_USER = 1;

    private String mGroupUuid = "";
    private String mGroupName = "";
    private String mFamilyName = "";
    private String mGivenName = "";
    private String mDisplayName = "";
    private String mPhotoUrl = "";
    private String mEmail = "";
    private String mPhone = "";
    private String mUserUuid = "";
    private int mType = 0;

    public MembershipListItem(Group group) {
        mGroupUuid = group.getGroupUuid();
        mGroupName = group.getName();
        mType = TYPE_GROUP;
    }

    public MembershipListItem(User user) {
        mFamilyName = user.getFamilyName();
        mGivenName = user.getGivenName();
        mDisplayName = user.getDisplayName();
        mPhotoUrl = user.getPhotoUrl();
        mEmail = user.getEmail();
        mPhone = user.getPhone();
        mUserUuid = user.getUserUuid();
        mType = TYPE_USER;
    }

    public String getGroupUuid() {
        return mGroupUuid;
    }

    public void setGroupUuid(String mGroupUuid) {
        this.mGroupUuid = mGroupUuid;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public void setGroupName(String mGroupName) {
        this.mGroupName = mGroupName;
    }

    public String getFamilyName() {
        return mFamilyName;
    }

    public void setFamilyName(String mFamilyName) {
        this.mFamilyName = mFamilyName;
    }

    public String getGivenName() {
        return mGivenName;
    }

    public void setGivenName(String mGivenName) {
        this.mGivenName = mGivenName;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String mDisplayName) {
        this.mDisplayName = mDisplayName;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void setPhotoUrl(String mPhotoUrl) {
        this.mPhotoUrl = mPhotoUrl;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String mPhone) {
        this.mPhone = mPhone;
    }

    public String getUserUuid() {
        return mUserUuid;
    }

    public void setUserUuid(String mUserUuid) {
        this.mUserUuid = mUserUuid;
    }

    public int getType() {
        return mType;
    }

    public void setType(int mType) {
        this.mType = mType;
    }

    public static List<MembershipListItem> createListFromGroup(Group group) {
        List<MembershipListItem> result = new ArrayList<>();
        result.add(new MembershipListItem(group));
        if (group.getMembers() != null) {
            for (String key : group.getMembers().keySet()) {
                result.add(new MembershipListItem(group.getMembers().get(key)));
            }
        }
        return result;
    }
}
