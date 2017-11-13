package com.volynski.familytrack.services;

import android.app.IntentService;
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
import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.Settings;
import com.volynski.familytrack.utils.SharedPrefsUtil;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 13.11.2017.
 */

public class TrackingService
        extends IntentService
        implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {

    public static final String COMMAND_START = "START";
    public static final String COMMAND_STOP = "STOP";
    Settings mOldSettings;
    String mCurrentUserUuid;

    private GoogleApiClient mGoogleApiClient;
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

    public TrackingService() { super("TrackingService");}

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            Timber.v("Null intent received");
            return;
        }

        if (intent.hasExtra(StringKeys.SETTINGS_CHANGED_FLAG_KEY)) {
            Settings newSettings = SharedPrefsUtil.getSettings(this);
            reconfigTracking(newSettings);
            mOldSettings = newSettings;
            return;
        }

        String command = intent.getAction();
        if (command == null) {
            Timber.e("Null command received");
            return;
        }

        if (command.equals(COMMAND_START)) {
            if (!intent.hasExtra(StringKeys.CURRENT_USER_UUID_KEY)) {
                Timber.v("Current user uuid was expected but not found. Can't start tracking service");
                return;
            }
            mCurrentUserUuid = intent.getStringExtra(StringKeys.CURRENT_USER_UUID_KEY);
            initGoogleApiClient();
        } else {
            if (command.equals(COMMAND_STOP)) {
                stopTracking();
            } else {
                Timber.e("Intent contains unknown command: " + command);
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startTracking();
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
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(TrackingService.this);

        fusedLocationClient.removeLocationUpdates(mLocationCallback);
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
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
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
                    updateUserLocation(mCurrentUserUuid,
                            locationResult,
                            placeLikelihoods.get(0));
                } else {
                    Timber.v("=0 or not isSuccess");
                }
                placeLikelihoods.release();
            }
        });
    }

    private void updateUserLocation(String userUuid,
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

    private void reconfigTracking(Settings newSettings) {
        if (!newSettings.getIsTrackingOn()) {
            // turn off tracking mode
        }
    }
}
