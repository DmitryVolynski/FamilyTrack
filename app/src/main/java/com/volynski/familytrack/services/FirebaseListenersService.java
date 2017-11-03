package com.volynski.familytrack.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FamilyTrackDbRefsHelper;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.FirebaseUtil;
import com.volynski.familytrack.data.models.firebase.GeofenceEvent;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.Settings;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.data.models.firebase.Zone;
import com.volynski.familytrack.utils.NotificationUtil;
import com.volynski.familytrack.utils.SharedPrefsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * FirebaseListenersService provides a listener service for app settings
 * It will listen to following nodes:
 *      - active group settings (groups/<activeUserGroupUuid>/settings)
 *      - current user membership (registered_users/<userUuid>/memberships)
 * If active group has changed - service will listen to new settings node
 * If settings has changed - service will replace it in SharedPreferences
 *
 * Алгоритм работы с геозонами:
 *  - при старте сервиса создаем listener на зоны и создаем их
 *  - при смене группы удаляем старые зоны, считываем новые и создаем их
 *  - при изменении реквизитов зоны пересоздаем их
 */
public class FirebaseListenersService
        extends Service
        implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            ResultCallback<Status> {

    private static final String FIREBASE_CONNECTION_STATUS_REF = ".info/connected";

    private DatabaseReference mSettingsRef;
    private DatabaseReference mUserRef;
    private DatabaseReference mGeofenceEventsRef;
    private DatabaseReference mGroupGeofencesRef;
    private DatabaseReference mFirebaseConnectionStatusRef;

    private FamilyTrackDataSource mDataSource;
    private String mActiveGroupUuid = "";
    private String mCurrentUserUuid;

    private ValueEventListener mCurrentUserListener;        // listen to current user changes (track group changes)
    private ValueEventListener mGeofenceEventsListener;     // listen to new geofence events to create notifications
    private ValueEventListener mSettingsListener;           // listen to settings changes
    private ValueEventListener mGroupGeofencesListener;     // listen to active group changes (zone list)
    private ValueEventListener mConnectionStatusListener;   // listen to firebase connection status

    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mGeofencePendingIntent;

    public FirebaseListenersService() {
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        createUserListener(mCurrentUserUuid);
        createSettingsListener(mActiveGroupUuid);
        createGeofenceEventsListener(mCurrentUserUuid);
        createGroupGeofencesListener(mActiveGroupUuid);
        createConnectionStatusListener();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void initGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this.getApplicationContext())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }

        mUserRef.removeEventListener(mCurrentUserListener);
        mSettingsRef.removeEventListener(mSettingsListener);
        mGeofenceEventsRef.removeEventListener(mGeofenceEventsListener);
        mFirebaseConnectionStatusRef.removeEventListener(mConnectionStatusListener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = START_STICKY;

        mCurrentUserUuid = (intent.hasExtra(StringKeys.CURRENT_USER_UUID_KEY) ?
                intent.getStringExtra(StringKeys.CURRENT_USER_UUID_KEY) : "");

        if (mCurrentUserUuid.equals("")) {
            Timber.v("UserUuid from SharedPrefs is empty. Can't start settings listener service");
            return result;
        }

        mDataSource = new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(this),this);
        mDataSource.getUserByUuid(mCurrentUserUuid, new FamilyTrackDataSource.GetUserByUuidCallback() {
            @Override
            public void onGetUserByUuidCompleted(FirebaseResult<User> result) {
                final User user = result.getData();
                if (user != null) {
                    if (result.getData() != null && result.getData().getActiveMembership() != null) {
                        mActiveGroupUuid = result.getData().getActiveMembership().getGroupUuid();
                    }
                    initGoogleApiClient();
                } else {
                    // remove settings if user not found or doesn't exist as member of any group
                    Timber.v(String.format("User '%1$s' not found. Settings cleared", mCurrentUserUuid));
                    SharedPrefsUtil.removeActiveGroup(FirebaseListenersService.this);
                }
            }
        });
        return result;
    }

    /**
     *
     * @param groupUuid
     */
    private void createGroupGeofencesListener(String groupUuid) {
        if (mGroupGeofencesListener != null) {
            mGroupGeofencesRef.removeEventListener(mGroupGeofencesListener);
        }

        mGroupGeofencesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Zone> zones = new ArrayList<Zone>();
                if (dataSnapshot.getChildren() != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        zones.add((Zone)snapshot.getValue(Zone.class));
                    }
                }
                registerGeofences(zones);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mGroupGeofencesRef = mDataSource.getFirebaseConnection()
                .getReference(FamilyTrackDbRefsHelper.zonesOfGroup(groupUuid));

        mGroupGeofencesRef.addValueEventListener(mGroupGeofencesListener);
    }


    /**
     *
     * @param zones
     */
    private void registerGeofences(List<Zone> zones) {
        // remove all previous geofences
        unregisterGeofences();

        Settings settings = SharedPrefsUtil.getSettings(this);
        if (settings == null) {
            Timber.v("No settings found in shared preferences. Can't create geofences");
            return;
        }

        if (settings.getIsSimulationOn() || !settings.getIsTrackingOn()) {
            Timber.v("Simulation is on or tracking is off. No geofences created");
            return;
        }

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(zones),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } else {
            Timber.v("ACCESS_FINE_LOCATION not granted. Unable to use geofences");
        }
    }

    private void unregisterGeofences() {
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                // This is the same pending intent that was used in addGeofences().
                getGeofencePendingIntent()
        ).setResultCallback(this); // Result processed in onResult().
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     *
     * @param userUuid
     */
    private void createUserListener(final String userUuid) {
        mCurrentUserListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    if (user.getActiveMembership() == null) {
                        // user has left the group - clear current settings
                        SharedPrefsUtil.removeSettings(FirebaseListenersService.this);
                        mActiveGroupUuid = "";
                    } else {
                        if (!mActiveGroupUuid.equals(user.getActiveMembership().getGroupUuid())) {
                            // get setting for new user group
                            mActiveGroupUuid = user.getActiveMembership().getGroupUuid();
                            createSettingsListener(mActiveGroupUuid);
                        }
                    }
                } else {
                    Timber.v("Unexpected error: null user received for key " + userUuid);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mUserRef = mDataSource.getFirebaseConnection()
                .getReference(FamilyTrackDbRefsHelper.userRef(userUuid));

        mUserRef.addValueEventListener(mCurrentUserListener);
    }

    /**
     *
     */
    private void createConnectionStatusListener() {
        mConnectionStatusListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SharedPrefsUtil.setFirebaseConnectionStatus(FirebaseListenersService.this,
                        dataSnapshot.getValue(Boolean.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Timber.e("Connection canceled: " + databaseError.getMessage());
            }
        };

        mFirebaseConnectionStatusRef = mDataSource.getFirebaseConnection()
                .getReference(FIREBASE_CONNECTION_STATUS_REF);
        mFirebaseConnectionStatusRef.addValueEventListener(mConnectionStatusListener);
    }

    /**
     *
     * @param userUuid
     */
    private void createGeofenceEventsListener(final String userUuid) {
        mGeofenceEventsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, GeofenceEvent> result = new HashMap<>();
                GenericTypeIndicator<Map<String, GeofenceEvent>> genericTypeIndicator =
                        new GenericTypeIndicator<Map<String, GeofenceEvent>>() {};
                Map<String, GeofenceEvent> map = dataSnapshot.getValue(genericTypeIndicator);
                result = dataSnapshot.getValue(genericTypeIndicator);

                if (result != null) {
                    Timber.v("Creating notifications for " + result.size() + " events ");
                    NotificationUtil.createNotifications(FirebaseListenersService.this, result);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mGeofenceEventsRef = mDataSource.getFirebaseConnection()
                .getReference(FamilyTrackDbRefsHelper.geofenceEventsRef(userUuid));

        mGeofenceEventsRef.addValueEventListener(mGeofenceEventsListener);

    }

    /**
     *
     * @param activeGroupUuid
     */
    private void createSettingsListener(final String activeGroupUuid) {
        mSettingsRef = null;
        if (!mActiveGroupUuid.equals("")) {
            mSettingsListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Settings oldSettings = SharedPrefsUtil.getSettings(FirebaseListenersService.this);
                    Settings settings = dataSnapshot.getValue(Settings.class);
                    if (oldSettings != null &&
                            !oldSettings.getIsTrackingOn() &&
                            settings.getIsTrackingOn()) {
                        // if tracking mode was off and now is on - we need to place
                        // group geofences into shared preferences to recreate them
                        createGeofencesInSharedPrefs(activeGroupUuid);
                    }
                    if (settings != null) {
                        SharedPrefsUtil.setSettings(FirebaseListenersService.this, settings);
                    } else {
                        Timber.v("Unexpected error: null settings received for group " + activeGroupUuid);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mSettingsRef = mDataSource.getFirebaseConnection()
                    .getReference(FamilyTrackDbRefsHelper.groupSettingsRef(activeGroupUuid));

            mSettingsRef.addValueEventListener(mSettingsListener);
        }

    }

    /**
     * Reads list of geofences from specified group and stores them in shared preferences
     * @param groupUuid - group uuid which geofences should be read
     */
    private void createGeofencesInSharedPrefs(String groupUuid) {
        mDataSource.getGroupByUuid(groupUuid, false,
                new FamilyTrackDataSource.GetGroupByUuidCallback() {
            @Override
            public void onGetGroupByUuidCompleted(FirebaseResult<Group> result) {
                if (result.getData() != null) {
                    SharedPrefsUtil.setGeofences(FirebaseListenersService.this,
                            result.getData().getGeofences());
                }
            }
        });
    }

    @Override
    public void onResult(@NonNull Status status) {

    }

    /**
     *
     * @param zones
     * @return
     */
    private GeofencingRequest getGeofencingRequest(List<Zone> zones) {
        GeofencingRequest result = null;
        if (zones != null) {
            List<Geofence> geofences = new ArrayList<>();
            for (Zone zone : zones) {
                if (zone.getTrackedUsers().contains(mCurrentUserUuid)) {
                    geofences.add(new Geofence.Builder()
                            .setRequestId(zone.getUuid())
                            .setCircularRegion(zone.getLatitude(), zone.getLongitude(), zone.getRadius())
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                            .build());
                }
            }

            if (geofences.size() > 0) {
                GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
                builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
                builder.addGeofences(geofences);
                result = builder.build();
            }
        }
        return result;
    }
}
