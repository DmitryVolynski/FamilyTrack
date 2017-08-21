package com.volynski.familytrack.data;

import com.google.firebase.database.DataSnapshot;
import com.volynski.familytrack.data.model.firebase.User;

/**
 * Created by DmitryVolynski on 21.08.2017.
 */

public class FirebaseUtil {
    public static User getUserFromSnapshot(DataSnapshot snapshot) {
        User user = (User)snapshot.getValue(User.class);
        user.setUserUuid(snapshot.getKey());
        return user;
    }
}
