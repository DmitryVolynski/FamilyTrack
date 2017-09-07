package com.volynski.familytrack.data;

/**
 * Created by DmitryVolynski on 06.09.2017.
 */

public class FamilyTrackDbRefsHelper {
    public static final String NODE_GROUPS = "groups/";
    public static final String NODE_USERS = "registered_users/";

    private static final String NODE_USER_FORMAT_STRING = NODE_USERS + "%s/";
    private static final String NODE_GROUP_FORMAT_STRING = NODE_GROUPS + "%s/";

    private static final String NODE_USERS_OF_GROUP_FORMAT_STRING = NODE_GROUPS + "%s/members/";
    private static final String NODE_USER_OF_GROUP_FORMAT_STRING = NODE_GROUPS + "%s/members/%s/";

    private static final String NODE_GROUPS_OF_USER_FORMAT_STRING = NODE_USERS + "%s/memberships/";
    private static final String NODE_GROUP_OF_USER_FORMAT_STRING = NODE_USERS + "%s/memberships/%s/";

    private static final String NODE_USER_MEMBERSHIPS_FORMAT_STRING = NODE_USER_OF_GROUP_FORMAT_STRING + "memberships/";
    private static final String NODE_USER_MEMBERSHIP_FORMAT_STRING = NODE_USER_OF_GROUP_FORMAT_STRING + "memberships/%s/";

    public static String groupRef(String groupUuid) {
        return String.format(NODE_GROUP_FORMAT_STRING, groupUuid);
    }

    public static String userRef(String userUuid) {
        return String.format(NODE_USER_FORMAT_STRING, userUuid);
    }

    public static String usersOfGroupRef(String groupUuid) {
        return String.format(NODE_USERS_OF_GROUP_FORMAT_STRING, groupUuid);
    }

    public static String userOfGroupRef(String groupUuid, String userUuid) {
        return String.format(NODE_USER_OF_GROUP_FORMAT_STRING, groupUuid, userUuid);
    }

    public static String groupsOfUserRef(String userUuid) {
        return String.format(NODE_GROUPS_OF_USER_FORMAT_STRING, userUuid);
    }

    public static String groupOfUserRef(String userUuid, String groupUuid) {
        return String.format(NODE_GROUP_OF_USER_FORMAT_STRING, userUuid, groupUuid);
    }

    // groups -KtGRD00beq29domxQQZ members -KtIE1SKww3I3S21C1dO memberships
    public static String userMembershipsRef(String userUuid, String groupUuid) {
        return String.format(NODE_USER_MEMBERSHIPS_FORMAT_STRING, groupUuid, userUuid);
    }

    // groups -KtGRD00beq29domxQQZ members -KtIE1SKww3I3S21C1dO memberships -KtGRD00beq29domxQQZ
    public static String userMembershipRef(String userUuid, String groupUuid) {
        return String.format(NODE_USER_MEMBERSHIP_FORMAT_STRING, groupUuid, userUuid, groupUuid);
    }
    
}
