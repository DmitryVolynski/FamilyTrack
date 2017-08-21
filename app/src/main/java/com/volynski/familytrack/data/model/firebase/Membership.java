package com.volynski.familytrack.data.model.firebase;

/**
 * Created by DmitryVolynski on 21.08.2017.
 */

public class Membership {
    private String mUserUuid;
    private int mRoleId;
    private int mStatusId;

    public String getUserUuid() {
        return mUserUuid;
    }

    public void setUserUuid(String mUserUuid) {
        this.mUserUuid = mUserUuid;
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
}
