package com.volynski.familytrack.data.models.firebase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by DmitryVolynski on 16.08.2017.
 * Model class for a group of users
 */

public class Group {
    private String mGroupUuid;
    private String mName;
    private Map<String, GroupUser> mMembers;

    public String getGroupUuid() {
        return mGroupUuid;
    }

    public void setGroupUuid(String mGroupUuid) {
        this.mGroupUuid = mGroupUuid;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }



    public Group(String name) {
        this.mName = name;
        mMembers = new HashMap<>();
    }

    public Group(String groupUuid, String name) {
        this.mGroupUuid = groupUuid;
        this.mName = name;
        this.mMembers = null;
    }

    public Group() {}

    public Group(String groupUuid, String name, Map<String, GroupUser> members) {
        this.mGroupUuid = groupUuid;
        this.mName = name;
        this.mMembers = members;
    }

    public Map<String, GroupUser> getMembers() {
        return mMembers;
    }

    public void setMembers(Map<String, GroupUser> mMembers) {
        this.mMembers = mMembers;
    }
}
