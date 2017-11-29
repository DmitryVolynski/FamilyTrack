package com.volynski.familytrack.services;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.volynski.familytrack.R;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FamilyTrackDbRefsHelper;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.GeofenceEvent;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.Settings;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.data.models.firebase.Zone;
import com.volynski.familytrack.utils.NotificationUtil;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.widget.FamilyTrackWidgetProvider;

import java.util.HashMap;
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
            GoogleApiClient.OnConnectionFailedListener {

    private static final String FIREBASE_CONNECTION_STATUS_REF = ".info/connected";

    private DatabaseReference mSettingsRef;
    private DatabaseReference mUserRef;
    private DatabaseReference mGroupRef;
    private DatabaseReference mGeofenceEventsRef;
    private DatabaseReference mGroupGeofencesRef;
    private DatabaseReference mFirebaseConnectionStatusRef;

    private FamilyTrackDataSource mDataSource;
    private String mActiveGroupUuid = "";
    private String mCurrentUserUuid;

    private ValueEventListener mCurrentUserListener;        // listen to current user changes (track group changes)
    private ValueEventListener mGroupListener;              // listen to current user's group changes (track group changes)
    private ValueEventListener mGeofenceEventsListener;     // listen to new geofence events to create notifications
    private ValueEventListener mSettingsListener;           // listen to settings changes
    private ValueEventListener mGroupGeofencesListener;     // listen to active group changes (zone list)
    private ValueEventListener mConnectionStatusListener;   // listen to firebase connection status

    private GoogleApiClient mGoogleApiClient;

    public FirebaseListenersService() {
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        createUserListener(mCurrentUserUuid);
        createGroupListener(mActiveGroupUuid);
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
        mGroupRef.removeEventListener(mGroupListener);
        mSettingsRef.removeEventListener(mSettingsListener);
        mGeofenceEventsRef.removeEventListener(mGeofenceEventsListener);
        mFirebaseConnectionStatusRef.removeEventListener(mConnectionStatusListener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = START_REDELIVER_INTENT;

        if (intent != null && intent.hasExtra(StringKeys.CURRENT_USER_UUID_KEY)) {
            mCurrentUserUuid = intent.getStringExtra(StringKeys.CURRENT_USER_UUID_KEY);
        } else {
            mCurrentUserUuid = "";
        }

        if (mCurrentUserUuid.equals("")) {
            Timber.v(getString(R.string.ex_no_current_user_uuid_key));
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
                    Timber.v(String.format(getString(R.string.ex_user_not_found), mCurrentUserUuid));
                    SharedPrefsUtil.removeActiveGroup(FirebaseListenersService.this);
                }
            }
        });
        return result;
    }

    /**
     * Creates listener for geofence events of specifyed group
     * @param groupUuid - group key to observe events
     */
    private void createGroupGeofencesListener(String groupUuid) {
        if (mGroupGeofencesListener != null) {
            mGroupGeofencesRef.removeEventListener(mGroupGeofencesListener);
            mGroupGeofencesListener = null;
        }

        if (groupUuid == null || groupUuid.equals("")) {
            return;
        }
        mGroupGeofencesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Zone> zones = new HashMap<>();
                if (dataSnapshot.getChildren() != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Zone zone = (Zone)snapshot.getValue(Zone.class);
                        zone.setUuid(snapshot.getKey());
                        zones.put(zone.getUuid(), zone);
                    }
                }
                SharedPrefsUtil.setGeofences(FirebaseListenersService.this, zones);
                runGeofenceIntentServiceCommand(GeofenceIntentService.COMMAND_RECONFIG_GEOFENCES,
                        mCurrentUserUuid);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mGroupGeofencesRef = mDataSource.getFirebaseConnection()
                .getReference(FamilyTrackDbRefsHelper.zonesOfGroup(groupUuid));

        mGroupGeofencesRef.addValueEventListener(mGroupGeofencesListener);
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
                    SharedPrefsUtil.setCurrentUser(FirebaseListenersService.this, user);
                    if (user.getActiveMembership() == null) {
                        // user has left the group - clear current settings
                        SharedPrefsUtil.removeSettings(FirebaseListenersService.this);
                        mActiveGroupUuid = "";
                        createGroupListener(mActiveGroupUuid);
                        createSettingsListener(mActiveGroupUuid);
                        createGroupGeofencesListener(mActiveGroupUuid);
                    } else {
                        if (!mActiveGroupUuid.equals(user.getActiveMembership().getGroupUuid())) {
                            // get setting for new user group
                            mActiveGroupUuid = user.getActiveMembership().getGroupUuid();
                            createSettingsListener(mActiveGroupUuid);
                            createGroupListener(mActiveGroupUuid);
                            createGroupGeofencesListener(mActiveGroupUuid);
                        }
                    }
                } else {
                    Timber.v(String.format(getString(R.string.ex_user_not_found),userUuid));
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

    private void createGroupListener(String groupUuid) {
        if (mGroupListener != null) {
            mGroupRef.removeEventListener(mGroupListener);
            mGroupListener = null;
        }

        if (groupUuid == null || groupUuid.equals("")) {
            Timber.v(getString(R.string.ex_group_uuid_is_empty));
            SharedPrefsUtil.removeActiveGroup(FirebaseListenersService.this);
            notifyWidgets();
            return;
        }

        mGroupListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getValue(Group.class);
                SharedPrefsUtil.setActiveGroup(FirebaseListenersService.this, group);
                notifyWidgets();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mGroupRef = mDataSource.getFirebaseConnection()
                .getReference(FamilyTrackDbRefsHelper.groupRef(groupUuid));

        mGroupRef.addValueEventListener(mGroupListener);
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
                Timber.e(getString(R.string.ex_connection_cancelled) + databaseError.getMessage());
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
        if (mGeofenceEventsListener != null) {
            mGeofenceEventsRef.removeEventListener(mGeofenceEventsListener);
            mGeofenceEventsListener = null;
        }

        if (userUuid == null || userUuid.equals("")) {
            return;
        }

        mGeofenceEventsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, GeofenceEvent> result = new HashMap<>();
                GenericTypeIndicator<Map<String, GeofenceEvent>> genericTypeIndicator =
                        new GenericTypeIndicator<Map<String, GeofenceEvent>>() {};
                Map<String, GeofenceEvent> map = dataSnapshot.getValue(genericTypeIndicator);
                result = dataSnapshot.getValue(genericTypeIndicator);

                if (result != null) {
                    //Timber.v("Creating notifications for " + result.size() + " events ");
                    NotificationUtil.createNotifications(FirebaseListenersService.this,
                            mCurrentUserUuid, result);
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
        if (mSettingsListener != null) {
            mSettingsRef.removeEventListener(mSettingsListener);
            mSettingsListener = null;
        }

        if (activeGroupUuid == null || activeGroupUuid.equals("")) {
            return;
        }

        mSettingsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Settings oldSettings = SharedPrefsUtil.getSettings(FirebaseListenersService.this);
                Settings settings = dataSnapshot.getValue(Settings.class);
                updateTrackingServiceState(oldSettings, settings);
                if (settings != null) {
                    SharedPrefsUtil.setSettings(FirebaseListenersService.this, settings);
                } else {
                    Timber.v(getString(R.string.ex_null_settings_received) + activeGroupUuid);
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

    /**
     * Updates tracking service state
     * - stops service if trackingMode is off and was on
     * - starts simulation tracking service (TrackingJobService) if trackingMode is on, was off and simulationMode is on
     * - starts real tracking service (TrackingService) if trackingMode is on, was off and simulationMode is off
     * @param oldSettings
     * @param settings
     */
    private void updateTrackingServiceState(Settings oldSettings, Settings settings) {
        if (settings == null || !settings.getIsTrackingOn()) {
            // user not a member of any group - just stop tracking
            TrackingJobService.stopJobService(this);
            stopTrackingService(mCurrentUserUuid);
            return;
        }

        if (settings.getIsTrackingOn()) {
            if (!settings.getIsSimulationOn()) {
                startTrackingService(mCurrentUserUuid);
                TrackingJobService.stopJobService(this);
            } else {
                stopTrackingService(mCurrentUserUuid);
                TrackingJobService.startJobService(this, mCurrentUserUuid,
                        0, 5);
            }
        }
    }

    private void stopTrackingService(String currentUserUuid) {
        runGeofenceIntentServiceCommand(GeofenceIntentService.COMMAND_UNREGISTER_GEOFENCES,
                currentUserUuid);
        stopService(new Intent(this, TrackingService.class));
    }

    private void startTrackingService(String currentUserUuid) {
        runGeofenceIntentServiceCommand(GeofenceIntentService.COMMAND_RECONFIG_GEOFENCES,
                currentUserUuid);
        Intent intent = new Intent(this, TrackingService.class);
        intent.putExtra(StringKeys.CURRENT_USER_UUID_KEY, currentUserUuid);
        startService(intent);
    }

    private void runGeofenceIntentServiceCommand(String command,
                                                 String currentUserUuid) {
        Intent intent = new Intent(this, GeofenceIntentService.class);
        intent.setAction(command);
        intent.putExtra(StringKeys.CURRENT_USER_UUID_KEY, currentUserUuid);
        this.startService(intent);
    }


    /**
     * Notify widgets to update list of users
     */
    private void notifyWidgets() {
        Intent intent = new Intent(this, FamilyTrackWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(StringKeys.SHARED_PREFS_CURRENT_USER_ACTIVE_GROUP_KEY, "ss");
        sendBroadcast(intent);
    }
}
