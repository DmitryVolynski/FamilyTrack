package com.volynski.familytrack.data.models.firebase;

import com.google.firebase.database.Exclude;

/**
 * Created by DmitryVolynski on 21.08.2017.
 */

public class GroupUser {
    private String mUserUuid;
    private int mRoleId;
    private int mStatusId;

    @Exclude
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

    public GroupUser(String userUuid, int roleId, int statusId) {
        mUserUuid = userUuid;
        mRoleId = roleId;
        mStatusId = statusId;
    }
}
