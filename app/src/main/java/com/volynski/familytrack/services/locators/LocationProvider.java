package com.volynski.familytrack.services.locators;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.volynski.familytrack.data.models.firebase.Location;

/**
 * Created by DmitryVolynski on 23.10.2017.
 */

public interface LocationProvider {
    interface GetCurrentLocationCallback { void onGetCurrentLocationCompleted(Location loc); }
    void getCurrentLocation(Context context,
                            GoogleApiClient googleApiClient,
                            @NonNull int accuracyLevel,
                            @NonNull GetCurrentLocationCallback callback);
}
