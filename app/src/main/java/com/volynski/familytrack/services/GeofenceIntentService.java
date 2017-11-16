package com.volynski.familytrack.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.volynski.familytrack.R;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.models.firebase.GeofenceEvent;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.data.models.firebase.Zone;
import com.volynski.familytrack.utils.SharedPrefsUtil;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static android.content.ContentValues.TAG;

/**
 * Created by DmitryVolynski on 02.11.2017.
 */

public class GeofenceIntentService extends IntentService {
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
/*
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
*/
            Timber.e("Error getting geofence event. Code=" + geofencingEvent.getErrorCode());
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            Map<String, Zone> zones = SharedPrefsUtil.getGeofences(this);
            User currentUser = SharedPrefsUtil.getCurrentUser(this);

            if (zones == null || currentUser == null) {
                Timber.v("Geofence event triggered but no zones or current user data found in shared preferences");
                return;
            }

            if (currentUser.getActiveMembership() == null) {
                Timber.v("No active membership found. Geofence notification not created");
                return;
            }

            String groupUuid = currentUser.getActiveMembership().getGroupUuid();
            FamilyTrackDataSource dataSource =
                    new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(this),this);
            for (Geofence geofence : triggeringGeofences) {
                if (zones.containsKey(geofence.getRequestId())) {
                    Zone zone = zones.get(geofence.getRequestId());
                    GeofenceEvent event = new GeofenceEvent(currentUser.getUserUuid(),
                            currentUser.getDisplayName(), currentUser.getFamilyName(),
                            currentUser.getGivenName(), zone, geofenceTransition);
                    dataSource.createGeofenceEvent(groupUuid, event, null);
                }
            }
        } else {
            // Log the error.
            Timber.e("unknown geofenceTransition=" + geofenceTransition);
        }

    }


    public GeofenceIntentService() {
        super("GeofenceIntentService");
    }
}
