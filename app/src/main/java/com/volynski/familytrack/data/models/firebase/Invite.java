package com.volynski.familytrack.data.models.firebase;

/**
 * Created by DmitryVolynski on 05.09.2017.
 */

public class Invite {
    private String mPhone;
    private String mEmail;
    private String mGroupUuid;
    private String mGroupName;


    public Invite() { }
    public Invite (String phone, String email, String groupUuid, String groupName) {
        mPhone = phone;
        mEmail = email;
        mGroupUuid = groupUuid;
        mGroupName = groupName;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String mPhone) {
        this.mPhone = mPhone;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String mEmail) {
        this.mEmail = mEmail;
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
}
