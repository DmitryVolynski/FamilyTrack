package com.volynski.familytrack.services.locators;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.volynski.familytrack.R;
import com.volynski.familytrack.data.models.firebase.Location;
import com.volynski.familytrack.utils.SharedPrefsUtil;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 23.10.2017.
 */

public class SimulatedLocationProvider implements LocationProvider {
    // initial location for geosimulation (Moscow's Red Square)
    public static LatLng RED_SQUARE_LOCATION = new LatLng(55.7554841, 37.6176549);

    private LatLng getNextSimulatedLatLng(LatLng prevLoc) {
        long now = Calendar.getInstance().getTimeInMillis();
        Random rnd = new Random(now);

        double distance = 30 + 50 * rnd.nextDouble();
        double heading = rnd.nextDouble() * 180f;

        // use Google Maps Android API utility library to calculate new point using distance & angle
        return SphericalUtil.computeOffset(prevLoc, distance, heading);
    }
    public SimulatedLocationProvider() {
        //Timber.v("Using SimulatedLocationProvider");
    }

    @Override
    public void getCurrentLocation(Context context,
                                   GoogleApiClient googleApiClient,
                                   @NonNull int accuracyLevel,
                                   @NonNull GetCurrentLocationCallback callback) {
        String address = context.getString(R.string.unknown_address);
        String locName = context.getString(R.string.unknown_location);

        LatLng prevLoc = SharedPrefsUtil.getLastKnownSimulatedLocation(context);
        if (prevLoc == null) {
            prevLoc = RED_SQUARE_LOCATION;
        }

        // get next simulated location
        LatLng newLatLng = getNextSimulatedLatLng(prevLoc);

        // get address by new latlng object
        Geocoder geocoder = new Geocoder(context);

        try {
            List<Address> addresses =
                    geocoder.getFromLocation(newLatLng.latitude, newLatLng.longitude, 5);

            if (addresses != null && addresses.size() > 0) {
                address = addresses.get(0).getAddressLine(0);
                for (int i=1; i < addresses.get(0).getMaxAddressLineIndex()-1; i++) {
                    address = address.concat(", " + addresses.get(0).getAddressLine(i));
                }
                locName = address;
            }
        } catch (IOException ex) {
            Timber.e(ex);
        }

        // save last simulated location in shared preferences
        SharedPrefsUtil.setLastKnownSimulatedLocation(context, newLatLng);

        Timber.v(String.format(context.getString(R.string.new_simulated_location),
                newLatLng.latitude, newLatLng.longitude, address));
        Location newLoc = new Location(Calendar.getInstance().getTimeInMillis(),
                newLatLng.latitude, newLatLng.longitude, locName, address, 0);

        callback.onGetCurrentLocationCompleted(newLoc);
    }
}
