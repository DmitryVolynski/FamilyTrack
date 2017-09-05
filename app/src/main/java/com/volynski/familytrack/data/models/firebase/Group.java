package com.volynski.familytrack.data.models.firebase;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by DmitryVolynski on 16.08.2017.
 * Model class for a group of users
 */

public class Group {
    public static final String REGISTERED_USERS_GROUP_KEY = "registered_users";
    private String mGroupUuid;
    private String mName;
    private Map<String, User> mMembers;

    @Exclude
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

    public Group(String groupUuid, String name, Map<String, User> members) {
        this.mGroupUuid = groupUuid;
        this.mName = name;
        this.mMembers = members;
    }

    public Map<String, User> getMembers() {
        return mMembers;
    }

    public void setMembers(Map<String, User> mMembers) {
        this.mMembers = mMembers;
    }

    public void addUser(User user) {
        if (mMembers == null) {
            mMembers = new HashMap<>();
        }
        mMembers.put(user.getUserUuid(), user);
    }
}
