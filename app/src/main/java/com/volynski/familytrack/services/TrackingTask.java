package com.volynski.familytrack.services;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.utils.SharedPrefsUtil;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 12.09.2017.
 */


public class TrackingTask extends Thread {
    private final TrackingTaskCallback mCallback;
    private String mUserUuid;
    private FusedLocationProviderClient mFusedLocationClient;
    private Context mContext;

    public TrackingTask(String userUuid, Context context, @NonNull TrackingTaskCallback callback) {
        mUserUuid = userUuid;
        mContext = context;
        mCallback = callback;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
    }

    @Override
    public void run() {
        Timber.v("Started at " + Calendar.getInstance().getTime().toString());
        try {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    Geocoder geocoder = new Geocoder(mContext, Locale.ENGLISH);
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null) {
                            updateUserLocation(mUserUuid, location, addresses);
                        }
                    } catch (IOException ex) {
                        Timber.e(ex);
                    }
                    mCallback.onTaskCompleted();
                }
            });
        } catch (SecurityException ex) {
            Timber.e(ex);
        }
    }

    private void updateUserLocation(String userUuid, Location location, List<Address> addresses) {
        FamilyTrackDataSource dataSource =
                new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(mContext), mContext);
        com.volynski.familytrack.data.models.firebase.Location userLocation =
                new com.volynski.familytrack.data.models.firebase.Location(location.getLongitude(),
                        location.getLatitude(), addresses.get(0).getFeatureName(), 3);
        dataSource.updateUserLocation(userUuid, userLocation, null);
    }

    private void checkObject(Object o) {
        int i = 0;
    }

}
