package com.volynski.familytrack.data.models.firebase;

import com.google.firebase.database.Exclude;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by DmitryVolynski on 12.09.2017.
 */

public class HistoryItem {
    public static final String FIELD_LOCATIONS = "locations";

    private String mUserUuid;
    private Map<String, Location> mLocations;

    public HistoryItem() {}

    public HistoryItem(String uuid, Map<String, Location> locations) {
        this.mUserUuid = uuid;
        this.mLocations = locations;
    }

    @Exclude
    public String getUserUuid() {
        return mUserUuid;
    }

    public void setUserUuid(String uuid) {
        this.mUserUuid = uuid;
    }

    public void addLocation(Location location) {
        if (mLocations == null) {
            mLocations = new HashMap<>();
        }
        mLocations.put(String.valueOf(location.getCalendar().getTimeInMillis()), location);
    }
}

