package com.volynski.familytrack.data;

import android.support.constraint.solver.widgets.Snapshot;

import com.google.firebase.database.DataSnapshot;
import com.volynski.familytrack.data.models.firebase.GeofenceEvent;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.Location;
import com.volynski.familytrack.data.models.firebase.User;

/**
 * Created by DmitryVolynski on 21.08.2017.
 */

public class FirebaseUtil {

    /**
     *
     * @param snapshot
     * @return
     */
    public static User getUserFromSnapshot(DataSnapshot snapshot) {
        User user = (User)snapshot.getValue(User.class);
        user.setUserUuid(snapshot.getKey());
        return user;
    }

    public static Location getLocationFromSnapshot(DataSnapshot snapshot) {
        Location location = (Location) snapshot.getValue(Location.class);
        return location;
    }

    public static Group getGroupFromSnapshot(DataSnapshot snapshot) {
        Group group = (Group)snapshot.getValue(Group.class);
        group.setGroupUuid(snapshot.getKey());

        for (DataSnapshot userSnapshot : snapshot.child("members").getChildren()) {
            String userKey = userSnapshot.getKey();
            group.getMembers().get(userKey).setUserUuid(userKey);
        }
        return group;
    }

    public static GeofenceEvent getGeofenceEventFromSnapshot(DataSnapshot snapshot) {
        GeofenceEvent event = (GeofenceEvent) snapshot.getValue(GeofenceEvent.class);
        return event;
    }
}
