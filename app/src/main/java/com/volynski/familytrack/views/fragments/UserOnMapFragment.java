package com.volynski.familytrack.views.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.volynski.familytrack.BR;
import com.volynski.familytrack.R;
import com.volynski.familytrack.adapters.RecyclerViewListAdapter;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.models.firebase.Location;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.databinding.FragmentUserOnMapBinding;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.viewmodels.UserOnMapViewModel;
import com.google.android.gms.location.places.GeoDataClient;
import com.volynski.familytrack.views.MainActivity;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 22.08.2017.
 */

public class UserOnMapFragment
            extends Fragment
            implements OnMapReadyCallback, View.OnClickListener {
    private static final String TAG = UserOnMapFragment.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private UserOnMapViewModel mViewModel;
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private MapView mMapView;
    private String mCurrentUserUuid;
    private LinearLayoutManager mLayoutManager;
    private static final int DEFAULT_ZOOM = 15;
    private HashMap<String, Marker> mMarkers = new HashMap<>();
    private boolean mGeofenceEditingMode = false;
    private Circle mCurrentGeofence;

    FragmentUserOnMapBinding mBinding;
    private RecyclerViewListAdapter mAdapter;

    public static UserOnMapFragment newInstance(Context context,
                                                String currentUserUuid,
                                                UserListNavigator navigator) {
        UserOnMapFragment result = new UserOnMapFragment();

        UserOnMapViewModel viewModel = new UserOnMapViewModel(context, currentUserUuid,
                new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(context), context));

        viewModel.setNavigator(navigator);
        result.setViewModel(viewModel);
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.start();
    }

    @Override
    public void onClick(View v) {
        ToggleButton tb = (ToggleButton)v;
        if (tb != null && mViewModel != null) {
            mViewModel.onToggleButtonClick(tb.getTextOn().toString());
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void setupMapFragment() {
        mGeoDataClient = Places.getGeoDataClient(this.getActivity(), null);
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this.getActivity(), null);
        //mMapView.getMapAsync(this);

        mMapFragment = SupportMapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.map_activitymain, mMapFragment);
        fragmentTransaction.commit();
        mMapFragment.getMapAsync(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mCurrentUserUuid = SharedPrefsUtil.getCurrentUserUuid(getContext());

        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_user_on_map,
                container,
                false);

        setupMapFragment();
        setupToggleButtons();
        setupCustomListeners();

        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.recyclerviewFrguseronmapUserslist.setLayoutManager(mLayoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(getContext(), mLayoutManager.getOrientation());

        mBinding.recyclerviewFrguseronmapUserslist.addItemDecoration(dividerItemDecoration);
        mAdapter = new RecyclerViewListAdapter(this.getContext(), mViewModel.viewModels,
                R.layout.user_horizontal_list_item, BR.viewmodel);

        mBinding.recyclerviewFrguseronmapUserslist.setAdapter(mAdapter);
        mBinding.setViewmodel(mViewModel);

        mBinding.yyy.setVisibility(View.INVISIBLE);
        return mBinding.getRoot();
    }

    private void setupCustomListeners() {
        mViewModel.redrawMarkers.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                UserOnMapFragment.this.redrawMarkers();
            }
        });

        mViewModel.redrawPath.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                UserOnMapFragment.this.redrawPath();
            }
        });

        mBinding.buttonFrguseronmapCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAddingGeofence();
            }
        });

        mViewModel.zoneRadius.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                mCurrentGeofence.setRadius(mViewModel.zoneRadius.get());
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getLocationPermission();
        try {
            //mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            redrawMarkers();
        } catch (Exception e) {
            Timber.e(e);
        }
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                updateGeofenceCenter(latLng);
            }
        });
    }

    private void updateGeofenceCenter(LatLng latLng) {
        if (!mGeofenceEditingMode) {
            return;
        }
        moveCameraTo(latLng);

        if (mCurrentGeofence == null) {
            CircleOptions circleOptions = new CircleOptions()
                    .center(latLng)
                    .clickable(true)
                    .fillColor(R.color.colorEditGeofence)
                    .strokeColor(Color.TRANSPARENT)
                    .radius(mViewModel.zoneRadius.get());
            mCurrentGeofence = mMap.addCircle(circleOptions);
        } else {
            mCurrentGeofence.setCenter(latLng);
        }
        mViewModel.zoneCenterLatitude.set(latLng.latitude);
        mViewModel.zoneCenterLongitude.set(latLng.longitude);
    }

    private void redrawPath() {
        if (mViewModel.path == null) {
            return;
        }
        redrawMarkers();
        PolylineOptions options = new PolylineOptions();
        User user = mViewModel.getSelectedUser();
        for (Location location : mViewModel.path) {
            options.add(location.getLatLng()).color(R.color.colorSecondaryDark);
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(location.getLatLng())
                    .title(user.getDisplayName())
                    .snippet(location.getTextForSnippet())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

            mMap.addMarker(markerOptions);
        }
        mMap.addPolyline(options);
    }

    private void redrawMarkers() {
        List<User> users = mViewModel.users;
        if (users == null) {
            return;
        }
        mMap.clear();
        mMarkers.clear();
        for (User user : users) {
            Location location = user.getLastKnownLocation();
            if (location != null) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(location.getLatLng())
                        .title(user.getDisplayName())
                        .snippet(user.getTextForSnippet())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

                mMarkers.put(user.getUserUuid(), mMap.addMarker(markerOptions));
            }
        }
    }

    public void setViewModel(UserOnMapViewModel mViewModel) {
        this.mViewModel = mViewModel;
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public void moveCameraTo(LatLng loc) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, DEFAULT_ZOOM));
    }

    public void moveCameraTo(double latitude, double longitude) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
            new LatLng(latitude, longitude), DEFAULT_ZOOM));
    }

    private void setupToggleButtons() {
        mBinding.tbutFrguseronmapOff.setOnClickListener(this);
        mBinding.tbutFrguseronmap1h.setOnClickListener(this);
        mBinding.tbutFrguseronmap8h.setOnClickListener(this);
        mBinding.tbutFrguseronmap1d.setOnClickListener(this);
        mBinding.tbutFrguseronmap1w.setOnClickListener(this);
    }

    public void userClicked(User user) {
        if (user.getLastKnownLocation() != null) {
            LatLng loc = user.getLastKnownLocation().getLatLng();
            moveCameraTo(loc.latitude, loc.longitude);
        }

        mViewModel.selectUser(user);
    }

    private void saveGeofence() {
        mViewModel.saveZone();
    }

    private void cancelAddingGeofence() {
        mBinding.xxx.animate().translationX(0).setDuration(300).alpha(1).start();
        mBinding.yyy.animate().translationX(mBinding.xxx.getWidth()).setDuration(300).start();
        ((MainActivity)getActivity()).restoreFab();
        mGeofenceEditingMode = false;
        mCurrentGeofence.remove();
    }

    public void startAddingGeofence() {
        mBinding.yyy.setX(mBinding.xxx.getWidth());
        mBinding.yyy.setVisibility(View.VISIBLE);
        mBinding.xxx.animate().translationX(-mBinding.xxx.getWidth()).setDuration(300).alpha(1).start();
        mBinding.yyy.animate().translationX(0).setDuration(300).start();
        mGeofenceEditingMode = true;
    }
}
