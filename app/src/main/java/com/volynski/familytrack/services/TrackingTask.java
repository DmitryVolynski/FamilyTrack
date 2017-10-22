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
import com.volynski.familytrack.utils.SharedPrefsUtil;

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
        if (mSettings == null) {
            // user is not a member of any group - do nothing
            Timber.v("mSettings == null. TrackingTask will not run");
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
            // now tracking is off - do nothing
            Timber.v("Tracking is off. Idle...");
            mCallback.onTaskCompleted(mRescheduleFlag);
            return;
        }

        initGoogleApiClient();
    }

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

    private void doWork() {
        Timber.v("doWork started");
        if (!mGoogleApiClient.isConnected()) {
            Timber.v("mGoogleApiClient not connected");
            mCallback.onTaskCompleted(mRescheduleFlag);
            return;
        }

        try {
            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
            // TODO доделать определение точных координат
            mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
                @Override
                public void onComplete(final @NonNull Task<android.location.Location> task) {
                    if (!task.isSuccessful()) {
                        Timber.v("mFusedLocationClient.getLastLocation() != success");
                        mCallback.onTaskCompleted(mRescheduleFlag);
                    }
                    PendingResult<PlaceLikelihoodBuffer> result =
                            Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
                    result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                        @Override
                        public void onResult(@NonNull PlaceLikelihoodBuffer placeLikelihoods) {
                            mGoogleApiClient.disconnect();
                            Timber.v("isSuccess=" + String.valueOf(placeLikelihoods.getStatus().isSuccess()));
                            if (placeLikelihoods.getStatus().isSuccess() && placeLikelihoods.getCount() > 0) {
                                Timber.v(placeLikelihoods.get(0).getPlace().getName().toString());
                                updateUserLocation(mUserUuid, task.getResult(), placeLikelihoods.get(0));
                            } else {
                                Timber.v("=0 or not isSuccess");
                                mCallback.onTaskCompleted(mRescheduleFlag);
                            }
                            placeLikelihoods.release();
                        }
                    });
                }
            });
        } catch (Exception ex) {
            if (mGoogleApiClient != null) {
                mGoogleApiClient.disconnect();
            }
            Timber.e(ex);
            mCallback.onTaskCompleted(mRescheduleFlag);
        }
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
