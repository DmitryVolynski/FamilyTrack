package com.volynski.familytrack.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.Settings;
import com.volynski.familytrack.data.models.firebase.Zone;
import com.volynski.familytrack.utils.SharedPrefsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 13.11.2017.
 */

public class TrackingService
        extends IntentService
        implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            ResultCallback<Status> {

    public static final String COMMAND_START = "START";
    public static final String COMMAND_STOP = "STOP";
    public static final String COMMAND_RECONFIG_GEOFENCES = "RECONFIG_GEOFENCES";

    private Settings mOldSettings;
    private String mCurrentUserUuid;
    private PendingIntent mGeofencePendingIntent;
    private Intent mIntentToHandle;

    private GoogleApiClient mGoogleApiClient;
    private GeofencingClient mGeofencingClient;

    private final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            saveUserLocation(locationResult);
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }
    };

    private boolean mIsTrackingOn = false;

    public TrackingService() { super("TrackingService");}

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mIntentToHandle = intent;
        initGoogleApiClient();
    }

    private void reconfigTracking(Settings newSettings) {
        Timber.v("Not implemented yet");
    }

    @Override
    public void onResult(@NonNull Status status) {
        int i = 0;
    }

    private void reconfigGeofences() {
        mGeofencingClient = LocationServices.getGeofencingClient(this);
        Map<String, Zone> zones = SharedPrefsUtil.getGeofences(this);

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
            GeofencingRequest request = getGeofencingRequest(zones);
            if (request != null) {
                mGeofencingClient.addGeofences(request, getGeofencePendingIntent())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                int i = 0;
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                int i = 0;
                            }
                });

                /*
                LocationServices.GeofencingApi.addGeofences(
                        mGoogleApiClient,
                        request,
                        getGeofencePendingIntent()
                ).setResultCallback(this);
                */
            } else {
                // request == null, just delete geofences
                unregisterGeofences();
            }
        } else {
            Timber.v("ACCESS_FINE_LOCATION not granted. Unable to use geofences");
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mIntentToHandle == null) {
            Timber.v("Null intent received");
            return;
        }

        if (mIntentToHandle.hasExtra(StringKeys.SETTINGS_CHANGED_FLAG_KEY)) {
            Settings newSettings = SharedPrefsUtil.getSettings(this);
            reconfigTracking(newSettings);
            mOldSettings = newSettings;
            return;
        }

        String command = mIntentToHandle.getAction();
        if (command == null) {
            Timber.e("Null command received");
            return;
        }

        if (command.equals(COMMAND_START)) {
            if (!mIntentToHandle.hasExtra(StringKeys.CURRENT_USER_UUID_KEY)) {
                Timber.v("Current user uuid was expected but not found. Can't start tracking service");
                return;
            }
            mCurrentUserUuid = mIntentToHandle.getStringExtra(StringKeys.CURRENT_USER_UUID_KEY);
            startTracking();
        } else {
            if (command.equals(COMMAND_STOP)) {
                stopTracking();
            } else {
                if (command.equals(COMMAND_RECONFIG_GEOFENCES)) {
                    reconfigGeofences();
                } else {
                    Timber.e("Intent contains unknown command: " + command);
                }
            }

        }
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
                    .addApi(Places.PLACE_DETECTION_API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();
        } else {
            onConnected(null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     *
     */
    private void stopTracking() {
        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(TrackingService.this);

        fusedLocationClient.removeLocationUpdates(mLocationCallback);
        unregisterGeofences();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        mIsTrackingOn = false;
    }

    /**
     *
     */
    private void startTracking() {
        Settings settings = SharedPrefsUtil.getSettings(this);
        if (settings == null) {
            Timber.v("Null settings. Can't start location service");
            return;
        }

        if (!settings.getIsTrackingOn()) {
            return;
        }

        final LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(settings.getLocationUpdateInterval() * 1000);
        mLocationRequest.setFastestInterval(settings.getLocationUpdateInterval() * 1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(10);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        LocationSettingsRequest.Builder settingsBuilder = new LocationSettingsRequest.Builder();


        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(settingsBuilder.build());

        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                FusedLocationProviderClient mFusedLocationClient =
                        LocationServices.getFusedLocationProviderClient(TrackingService.this);
                if (ContextCompat.checkSelfPermission(TrackingService.this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                            mLocationCallback, null);
                    mIsTrackingOn = true;
                    reconfigGeofences();
                } else {
                    Timber.e("android.Manifest.permission.ACCESS_FINE_LOCATION not granted");
                }
            }
        });
    }

    private void saveUserLocation(final LocationResult locationResult) {
        if (ContextCompat.checkSelfPermission(TrackingService.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Timber.e("android.Manifest.permission.ACCESS_FINE_LOCATION not granted");
            return;
        }

        PendingResult<PlaceLikelihoodBuffer> result =
                Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(@NonNull PlaceLikelihoodBuffer placeLikelihoods) {
                //mGoogleApiClient.disconnect();
                if (placeLikelihoods.getStatus().isSuccess() &&
                        placeLikelihoods.getCount() > 0) {
                    Timber.v("Place is: " + placeLikelihoods.get(0).getPlace().getName().toString());
                    Timber.v("Address is: " + placeLikelihoods.get(0).getPlace().getAddress().toString());
                    updateUserLocationInDb(mCurrentUserUuid,
                            locationResult,
                            placeLikelihoods.get(0));
                } else {
                    Timber.v("=0 or not isSuccess");
                }
                placeLikelihoods.release();
            }
        });
    }

    private void updateUserLocationInDb(String userUuid,
                                        LocationResult locationResult,
                                        PlaceLikelihood placeLikelihood) {
        FamilyTrackRepository dataSource =
                new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(this), this);
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int batteryPct = Math.round((level / (float)scale) * 100);

        //locationResult.getLastLocation().
        com.volynski.familytrack.data.models.firebase.Location loc =
                com.volynski.familytrack.data.models.firebase.Location.getInstanceFromLastLocation(locationResult.getLastLocation());
        loc.setAddress(placeLikelihood.getPlace().getAddress().toString());
        loc.setKnownLocationName(placeLikelihood.getPlace().getName().toString());
        loc.setBatteryLevel(batteryPct);
        dataSource.updateUserLocation(userUuid, loc,
                new FamilyTrackDataSource.UpdateUserLocationCallback() {
            @Override
            public void onUpdateUserLocationCompleted(FirebaseResult<String> result) {
                //mCallback.onTaskCompleted(mRescheduleFlag);
            }
        });
    }

    /**
     *
     * @param zones
     * @return
     */
    private GeofencingRequest getGeofencingRequest(Map<String, Zone> zones) {
        GeofencingRequest result = null;
        if (zones != null) {
            List<Geofence> geofences = new ArrayList<>();
            for (String key : zones.keySet()) {
                Zone zone = zones.get(key);
                if (zone.getTrackedUsers().contains(mCurrentUserUuid)) {
                    geofences.add(new Geofence.Builder()
                            .setRequestId(zone.getUuid())
                            .setCircularRegion(zone.getLatitude(), zone.getLongitude(), zone.getRadius())
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_ENTER)
                            .setLoiteringDelay(10000)
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
        if (mGeofencingClient != null) {
            mGeofencingClient.removeGeofences(getGeofencePendingIntent());
        }
/*
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient,
                getGeofencePendingIntent());
*/
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }

        Intent intent = new Intent(this, GeofenceIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);

        return mGeofencePendingIntent;
    }


}
