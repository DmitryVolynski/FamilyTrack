package com.volynski.familytrack.data.model.firebase;

import java.util.List;

/**
 * Created by DmitryVolynski on 16.08.2017.
 * Model class for a group of users
 */

public class Group {
    private String mGroupUuid;
    private String mName;
    private List<Membership> mMembers;

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
    }

    public Group(String groupUuid, String name) {
        this.mGroupUuid = groupUuid;
        this.mName = name;
        this.mMembers = null;
    }

    public Group() {}

    public Group(String groupUuid, String name, List<Membership> members) {
        this.mGroupUuid = groupUuid;
        this.mName = name;
        this.mMembers = members;
    }
}
