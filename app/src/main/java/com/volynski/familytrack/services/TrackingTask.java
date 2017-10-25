package com.volynski.familytrack.services;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.Location;
import com.volynski.familytrack.data.models.firebase.Settings;
import com.volynski.familytrack.data.models.firebase.Zone;
import com.volynski.familytrack.services.locators.LocationProvider;
import com.volynski.familytrack.services.locators.RealLocationProvider;
import com.volynski.familytrack.services.locators.SimulatedLocationProvider;
import com.volynski.familytrack.utils.SharedPrefsUtil;

import java.util.Map;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 12.09.2017.
 */


public class TrackingTask
        extends Thread
        implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {

    private final TrackingTaskCallback mCallback;
    private final FamilyTrackDataSource mDataSource;
    private String mUserUuid;
    private Context mContext;

    // keeps 0 if no reschedule of location service required, new interval in minutes (>0) if we need rescheduling
    private int mRescheduleFlag;

    private GoogleApiClient mGoogleApiClient;
    private Settings mSettings;

    public TrackingTask(String userUuid, Context context, @NonNull TrackingTaskCallback callback) {
        mUserUuid = userUuid;
        mContext = context.getApplicationContext();
        mCallback = callback;
        mDataSource = new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(mContext), mContext);
        mSettings = SharedPrefsUtil.getSettings(context);
    }

    @Override
    public void run() {
        initGoogleApiClient();
    }

    private void removeGeofences() {
        //LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient,
    }

    private void updateUserLocation(String userUuid, Location loc) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = mContext.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int batteryPct = Math.round((level / (float)scale) * 100);

        loc.setBatteryLevel(batteryPct);
        mDataSource.updateUserLocation(userUuid, loc, new FamilyTrackDataSource.UpdateUserLocationCallback() {
            @Override
            public void onUpdateUserLocationCompleted(FirebaseResult<String> result) {
                mCallback.onTaskCompleted(mRescheduleFlag);
            }
        });
    }
/*
    private void updateUserLocation(String userUuid,
                                    android.location.Location loc,
                                    PlaceLikelihood placeLikelihood) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = mContext.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int batteryPct = Math.round((level / (float)scale) * 100);

        Location userLocation = new Location(loc.getLongitude(),
                loc.getLatitude(),
                placeLikelihood.getPlace().getName().toString(),
                placeLikelihood.getPlace().getAddress().toString(),
                batteryPct);

        mDataSource.updateUserLocation(userUuid, userLocation, new FamilyTrackDataSource.UpdateUserLocationCallback() {
            @Override
            public void onUpdateUserLocationCompleted(FirebaseResult<String> result) {
                mCallback.onTaskCompleted(mRescheduleFlag);
            }
        });
    }
*/

    @Override
    public void onConnectionSuspended(int i) {
        Timber.v("onConnectionSuspended i=" + i);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Timber.v("onConnected");
        doWork();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.v(connectionResult.getErrorMessage());
        int i = 0;
    }

    // do location detection when GoogleApi is ready
    private void doWork() {
        Timber.v("doWork started");

        if (!mGoogleApiClient.isConnected()) {
            // if google api client not ready - do nothing
            Timber.v("mGoogleApiClient not connected");
            mCallback.onTaskCompleted(mRescheduleFlag);
            return;
        }

        if (mSettings == null) {
            // user is not a member of any group - do track user & remove any geofences
            // or current settings not loaded into shared preferences
            Timber.v("mSettings is null. TrackingTask will not run");
            removeGeofences();
            mCallback.onTaskCompleted(0);
            return;
        }

        Timber.v("=== Current settings=" + (new Gson()).toJson(mSettings));

        // checking for reschedule tracking task
        int currentInterval = SharedPrefsUtil.getTrackingInterval(mContext);
        int newInterval = mSettings.getLocationUpdateInterval();
        Timber.v(String.format("Checking old and new intervals: %1$d/%2$d", currentInterval, newInterval));
        mRescheduleFlag = (currentInterval != newInterval ? newInterval : 0);

        if (!mSettings.getIsTrackingOn()) {
            // tracking is off - do nothing
            Timber.v("Tracking is off. Idle...");
            removeGeofences();
            mCallback.onTaskCompleted(mRescheduleFlag);
            return;
        }

        setGeofences(SharedPrefsUtil.getGeofences(mContext));

        // create appropriate location provider
        LocationProvider locationProvider = new SimulatedLocationProvider();
        //LocationProvider locationProvider = (mSettings.getIsSimulationOn() ?
        //    new SimulatedLocationProvider() :
        //    new RealLocationProvider());

        // get location from provider and save it
        locationProvider.getCurrentLocation(mContext,
                mGoogleApiClient,
                mSettings.getAccuracyLevel(),
                new LocationProvider.GetCurrentLocationCallback() {
                    @Override
                    public void onGetCurrentLocationCompleted(Location loc) {
                        updateUserLocation(mUserUuid, loc);
                        mGoogleApiClient.disconnect();
                    }
                });
    }

    private void setGeofences(Map<String, Zone> geofences) {

    }

    @Override
    protected void finalize() throws Throwable {
        Timber.v("finalize");
        super.finalize();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    private void initGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext.getApplicationContext())
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    private void checkObject(Object o) {
        int i = 0;

    }
}
