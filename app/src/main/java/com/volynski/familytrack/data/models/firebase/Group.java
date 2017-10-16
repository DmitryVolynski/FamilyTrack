package com.volynski.familytrack.data.models.firebase;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by DmitryVolynski on 16.08.2017.
 * Model class for a group of mUsers
 */

public class Group {
    public static final String REGISTERED_USERS_GROUP_KEY = "registered_users";

    public static final String FIELD_SETTINGS = "settings";
    private String mGroupUuid;
    private String mName;
    private Settings mSettings;
    private Map<String, User> mMembers;
    private Map<String, Zone> mGeofences;

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
        this.mSettings = Settings.getDefaultInstance();
        mMembers = new HashMap<>();
    }

    public Group(String groupUuid, String name) {
        this.mGroupUuid = groupUuid;
        this.mName = name;
        this.mMembers = null;
        this.mSettings = Settings.getDefaultInstance();
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

    public void addMember(User user) {
        if (mMembers == null) {
            mMembers = new HashMap<>();
        }
        mMembers.put(user.getUserUuid(), user);
    }

    @Exclude
    public int getMembersCount() {
        return (mMembers == null ? 0 : mMembers.size());
    }

    @Exclude
    public int getAdminsCount(String excludedUuid) {
        int count = 0;
        if (mMembers != null) {
            for (String key : mMembers.keySet()) {
                User user = mMembers.get(key);
                if (user.getActiveMembership() != null &&
                        user.getActiveMembership().getStatusId() == Membership.USER_JOINED &&
                        user.getActiveMembership().getRoleId() == Membership.ROLE_ADMIN &&
                        !user.getUserUuid().equals(excludedUuid)) {
                    count++;
                }
            }
        }
        return count;
    }

    public Map<String, Zone> getGeofences() {
        return mGeofences;
    }

    public void setGeofences(Map<String, Zone> geofences) {
        this.mGeofences = geofences;
    }

    public Settings getSettings() {
        return mSettings;
    }

    public void setSettings(Settings settings) {
        this.mSettings = mSettings;
    }
}
