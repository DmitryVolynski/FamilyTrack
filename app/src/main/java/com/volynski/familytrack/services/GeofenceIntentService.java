package com.volynski.familytrack.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.volynski.familytrack.R;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.models.firebase.GeofenceEvent;
import com.volynski.familytrack.data.models.firebase.Settings;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.data.models.firebase.Zone;
import com.volynski.familytrack.utils.SharedPrefsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 02.11.2017.
 */

public class GeofenceIntentService extends IntentService {
    public static final String COMMAND_RECONFIG_GEOFENCES = "RECONFIG_GEOFENCES";
    public static final String COMMAND_UNREGISTER_GEOFENCES = "UNREGISTER_GEOFENCES";

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (intent == null) {
            Timber.v("Intent is null, can't proceed service query");
            return;
        }

        String command = intent.getAction();
        if (command != null) {
            if (!intent.hasExtra(StringKeys.CURRENT_USER_UUID_KEY)) {
                Timber.v(getString(R.string.ex_no_user_uuid_in_intent));
                return;
            }

            String currentUserUuid = intent.getStringExtra(StringKeys.CURRENT_USER_UUID_KEY);

            if (command.equals(COMMAND_RECONFIG_GEOFENCES)) {
                reconfigGeofences(currentUserUuid);
                return;
            }

            if (command.equals(COMMAND_UNREGISTER_GEOFENCES)) {
                unregisterGeofences();
                return;
            }

            Timber.e(getString(R.string.ex_intent_unknown_command) + command);
            return;
        }

        // otherwise try to parse geofence events
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Timber.e(getString(R.string.ex_geofence_event_error) + geofencingEvent.getErrorCode());
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
                Timber.v(getString(R.string.ex_geofence_error));
                return;
            }

            if (currentUser.getActiveMembership() == null) {
                Timber.v(getString(R.string.ex_no_active_membership_no_geofence));
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
            Timber.e(getString(R.string.ex_unknown_geofence_transition) + geofenceTransition);
        }

    }

    public GeofenceIntentService() {
        super("GeofenceIntentService");
    }

    private void reconfigGeofences(String currentUserUuid) {
        GeofencingClient geofencingClient =
                LocationServices.getGeofencingClient(this);
        Map<String, Zone> zones = SharedPrefsUtil.getGeofences(this);

        Settings settings = SharedPrefsUtil.getSettings(this);
        if (settings == null) {
            Timber.v(getString(R.string.ex_no_settings_found));
            unregisterGeofences();
            return;
        }

        if (settings.getIsSimulationOn() || !settings.getIsTrackingOn()) {
            Timber.v(getString(R.string.ex_no_geofence_in_simulation_mode));
            unregisterGeofences();
            return;
        }

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            GeofencingRequest request = getGeofencingRequest(currentUserUuid, zones);
            if (request != null) {
                geofencingClient.addGeofences(request, getGeofencePendingIntent())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                int i = 0;
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Timber.e(getString(R.string.ex_check_location_mode));
                            }
                        });
            } else {
                // request == null, just delete geofences
                unregisterGeofences();
            }
        } else {
            Timber.v(getString(R.string.no_access_fine_location));
        }
    }
    /**
     *
     * @param zones
     * @return
     */
    private GeofencingRequest getGeofencingRequest(String currentUserUuid, Map<String, Zone> zones) {
        GeofencingRequest result = null;
        if (zones != null) {
            List<Geofence> geofences = new ArrayList<>();
            for (String key : zones.keySet()) {
                Zone zone = zones.get(key);
                if (zone.getTrackedUsers().contains(currentUserUuid)) {
                    geofences.add(new Geofence.Builder()
                            .setRequestId(zone.getUuid())
                            .setCircularRegion(zone.getLatitude(),
                                    zone.getLongitude(), zone.getRadius())
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL |
                                    Geofence.GEOFENCE_TRANSITION_EXIT |
                                    Geofence.GEOFENCE_TRANSITION_ENTER)
                            .setLoiteringDelay(5000)
                            .build());
                }
            }

            if (geofences.size() > 0) {
                GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
                builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL |
                        GeofencingRequest.INITIAL_TRIGGER_ENTER);
                builder.addGeofences(geofences);
                result = builder.build();
            }
        }
        return result;
    }

    private void unregisterGeofences() {
        GeofencingClient geofencingClient =
                LocationServices.getGeofencingClient(this);
        geofencingClient.removeGeofences(getGeofencePendingIntent());
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(this, GeofenceIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }


}
