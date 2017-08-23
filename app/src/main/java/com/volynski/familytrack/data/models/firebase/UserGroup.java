package com.volynski.familytrack.data.models.firebase;

import com.google.firebase.database.Exclude;

/**
 * Created by DmitryVolynski on 21.08.2017.
 */

public class UserGroup {
    private String mGroupUuid;
    private int mRoleId;
    private int mStatusId;

    @Exclude
    public String getGroupUuid() {
        return mGroupUuid;
    }

    public void setGroupUuid(String mGroupUuid) {
        this.mGroupUuid = mGroupUuid;
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

    public UserGroup(String groupUuid, int roleId, int statusId) {
        mGroupUuid = groupUuid;
        mRoleId = roleId;
        mStatusId = statusId;
    }
}
