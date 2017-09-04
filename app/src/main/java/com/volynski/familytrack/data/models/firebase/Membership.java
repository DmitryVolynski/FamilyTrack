package com.volynski.familytrack.data.models.firebase;

/**
 * Created by DmitryVolynski on 04.09.2017.
 */

public class Membership {
    public static final int ROLE_ADMIN = 1;
    public static final int ROLE_MEMBER = 2;
    public static final int ROLE_UNDEFINED = 3;

    public static final int USER_INVITED = 1;
    public static final int USER_JOINED = 2;
    public static final int USER_CREATED = 3;
    public static final int USER_DEPARTED = 3;

    private String mGroupUuid;
    private String mGroupName;
    private int mRoleId;
    private int mStatusId;

    public Membership() {}

    public Membership(String groupUuid, String groupName, int roleId, int statusId) {
        mGroupUuid = groupUuid;
        mGroupName = groupName;
        mRoleId = roleId;
        mStatusId = statusId;
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

    public void setGroupName(String mName) {
        this.mGroupName = mName;
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

    public Membership clone() {
        return new Membership(mGroupUuid, mGroupName, mRoleId, mStatusId);
    }
}
