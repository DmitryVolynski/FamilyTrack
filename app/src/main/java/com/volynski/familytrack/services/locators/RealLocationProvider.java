package com.volynski.familytrack.services.locators;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by DmitryVolynski on 23.10.2017.
 */

public class RealLocationProvider implements LocationProvider {
    @Override
    public void getCurrentLocation(Context context,
                                   GoogleApiClient googleApiClient,
                                   @NonNull int accuracyLevel,
                                   @NonNull GetCurrentLocationCallback callback) {

        callback.onGetCurrentLocationCompleted(null);
/*    try {
        FusedLocationProviderClient mFusedLocationClient =
                LocationServices.getFusedLocationProviderClient(mContext);
        // TODO доделать определение точных координат
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(final @NonNull Task<Location> task) {
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
    }*/

    }
}
