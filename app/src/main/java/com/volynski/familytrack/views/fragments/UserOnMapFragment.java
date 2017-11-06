package com.volynski.familytrack.views.fragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.util.TypedValue;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.volynski.familytrack.BR;
import com.volynski.familytrack.R;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.adapters.RecyclerViewListAdapter;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.models.firebase.Location;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.data.models.firebase.Zone;
import com.volynski.familytrack.databinding.FragmentUserOnMapBinding;
import com.volynski.familytrack.dialogs.SimpleDialogFragment;
import com.volynski.familytrack.utils.ResourceUtil;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.utils.SnackbarUtil;
import com.volynski.familytrack.viewmodels.UserOnMapViewModel;
import com.google.android.gms.location.places.GeoDataClient;
import com.volynski.familytrack.views.MainActivity;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 22.08.2017.
 */

public class UserOnMapFragment
            extends Fragment
            implements OnMapReadyCallback, View.OnClickListener {
    private static final String TAG = UserOnMapFragment.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int ANIMATION_DURATION = 300;

    public static final float GEOFENCE_STROKE_WIDTH = 2;
    public static final int DEFAULT_ZOOM = 15;

    private UserOnMapViewModel mViewModel;
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    //private String mCurrentUserUuid;
    private LinearLayoutManager mLayoutManager;
    private HashMap<String, Marker> mMarkers = new HashMap<>();
    private HashMap<String, Circle> mCircles = new HashMap<>();
    private boolean mGeofenceEditingMode = false;
    private Circle mCurrentGeofence;
    private Observable.OnPropertyChangedCallback mSnackbarCallback;

    FragmentUserOnMapBinding mBinding;
    private RecyclerViewListAdapter mAdapter;
    private LinearLayoutManager mGeoLayoutManager;
    private RecyclerViewListAdapter mAdapterGeo;
    private Polyline mPolyline;

    public static UserOnMapFragment newInstance(String currentUserUuid) {
        Bundle args = new Bundle();
        args.putString(StringKeys.CURRENT_USER_UUID_KEY, currentUserUuid);

        UserOnMapFragment result = new UserOnMapFragment();
        result.setArguments(args);

        return result;
    }

    @Override
    public void onDestroy() {
        if (mSnackbarCallback != null) {
            mViewModel.snackbarText.removeOnPropertyChangedCallback(mSnackbarCallback);
        }
        super.onDestroy();
    }

    private void setupSnackbar() {
        mSnackbarCallback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                SnackbarUtil.showSnackbar(((MainActivity)getActivity()).getViewForSnackbar(),
                        mViewModel.snackbarText.get());
            }
        };
        mViewModel.snackbarText.addOnPropertyChangedCallback(mSnackbarCallback);
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
        //mCurrentUserUuid = SharedPrefsUtil.getCurrentUserUuid(getContext());

        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_user_on_map,
                container,
                false);

        setupMapFragment();
        setupToggleButtons();
        setupSnackbar();
        setupCustomListeners();

        mLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        mBinding.recyclerviewFrguseronmapUserslist.setLayoutManager(mLayoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(getContext(), mLayoutManager.getOrientation());

        mBinding.recyclerviewFrguseronmapUserslist.addItemDecoration(dividerItemDecoration);
        mAdapter = new RecyclerViewListAdapter(this.getContext(), mViewModel.viewModels,
                R.layout.user_horizontal_list_item, BR.viewmodel);

        mBinding.recyclerviewFrguseronmapUserslist.setAdapter(mAdapter);

        mAdapterGeo = new RecyclerViewListAdapter(this.getContext(), mViewModel.viewModels,
                R.layout.user_list_item_dwell_control, BR.viewmodel);

        mGeoLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        mBinding.recyclerviewFrguseronmapGeosettings.setLayoutManager(mGeoLayoutManager);
        mBinding.recyclerviewFrguseronmapGeosettings.setAdapter(mAdapterGeo);

        mBinding.setViewmodel(mViewModel);

        mBinding.conslayoutFrguseronmapEditgeofence.setVisibility(View.INVISIBLE);
        return mBinding.getRoot();
    }

    private void setupCustomListeners() {
        // redraw all users locations
        mViewModel.redrawMarkers.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                UserOnMapFragment.this.redrawMarkers();
            }
        });

        // redraw path when needed
        mViewModel.redrawPath.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                UserOnMapFragment.this.redrawPath();
            }
        });

        // cancel geofence editing
        mBinding.buttonFrguseronmapCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.cancelZoneEdit();
                switchToNormalMode();
            }
        });

        // click on 'remove' image button
        mBinding.imageviewFrguseronmapRemovezone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRemoveZone();
            }
        });

        // redraw circle whet user changed it radius
        mViewModel.zoneRadius.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                mCurrentGeofence.setRadius(mViewModel.zoneRadius.get());
            }
        });

        // redraw all geofences
        mViewModel.redrawZones.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                UserOnMapFragment.this.redrawZones();
            }
        });

        // zone created/updated - switch to normal mode with list of users
        mViewModel.zoneDbOpCompleted.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                switchToNormalMode();
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        //getFragmentManager().beginTransaction().remove(mMapFragment).commit();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getLocationPermission();
        try {
            //mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            redrawZones();
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

        mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(Circle circle) {
                if (mViewModel.zoneEditMode.get() == UserOnMapViewModel.EM_NONE) {
                    startEditGeofence(circle);
                }
            }
        });
    }

    /**
     * Updates geofence center when user edit geofence data and clicks new point on map
     * Changed center displays on map & gos to viewmodel
     * @param latLng - new center of editable geofence
     */
    private void updateGeofenceCenter(LatLng latLng) {
        if (mViewModel.zoneEditMode.get() == UserOnMapViewModel.EM_NONE) {
            return;
        }
        moveCameraTo(latLng);

        if (mCurrentGeofence == null) {
            CircleOptions circleOptions = new CircleOptions()
                    .center(latLng)
                    .clickable(true)
                    .fillColor(getResources().getColor(R.color.colorEditGeofenceFill, null))
                    .strokeColor(getResources().getColor(R.color.colorEditGeofenceStroke, null))
                    .strokeWidth(GEOFENCE_STROKE_WIDTH)
                    .radius(mViewModel.zoneRadius.get());
            mCurrentGeofence = mMap.addCircle(circleOptions);
        } else {
            mCurrentGeofence.setCenter(latLng);
        }
        mViewModel.zoneCenterLatitude.set(latLng.latitude);
        mViewModel.zoneCenterLongitude.set(latLng.longitude);
    }

    /**
     * Redraws the path from viewmodel
     * Previous path will be removed from map
     */
    private void redrawPath() {
        if (mViewModel.path == null) {
            return;
        }
        //redrawMarkers();
        PolylineOptions options = new PolylineOptions();
        User user = mViewModel.getSelectedUser();
        for (Location location : mViewModel.path) {
            options.add(location.getLatLng());
/*
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(location.getLatLng())
                    .title(user.getDisplayName())
                    .snippet(location.getTextForSnippet())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

            mMap.addMarker(markerOptions);
*/
        }
        if (mPolyline != null) {
            mPolyline.remove();
        }
        mPolyline = mMap.addPolyline(options);
        mPolyline.setColor(Color.RED);
        mPolyline.setWidth(5);
    }

    /**
     * Redraws set of markers - set of last known locations for all users
     * in group.
     */
    private void redrawMarkers() {
        LatLng newCameraPos = null;

        List<User> users = mViewModel.users;
        if (users == null) {
            return;
        }
        for (String key : mMarkers.keySet()) {
            mMarkers.get(key).remove();
        }
        mMarkers.clear();
        for (User user : users) {
            Location location = user.getLastKnownLocation();
            if (location != null) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(location.getLatLng())
                        .title(user.getDisplayName())
                        .snippet(user.getTextForSnippet())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                if (mViewModel.getSelectedUser() != null &&
                        mViewModel.getSelectedUser().getUserUuid().equals(user.getUserUuid()))
                    newCameraPos = location.getLatLng();
                mMarkers.put(user.getUserUuid(), mMap.addMarker(markerOptions));
            }
        }
        if (newCameraPos != null) {
            if (!mMap.getProjection().getVisibleRegion().latLngBounds.contains(newCameraPos)) {
                // if marker of selected user is out of bounds - center map to
                // appropriate coordinates (current location of selected user)
                moveCameraTo(newCameraPos);
            }
        }
        redrawPath();
    }

    /**
     * Redraws all geofence zones
     * Previous set of zones will be revoved
     */
    private void redrawZones() {
        if (!isAdded()) {
            return;
        }
        Map<String, Zone> zones = mViewModel.zones;
        for (String key : mCircles.keySet()) {
            mCircles.get(key).remove();
        }
        mCircles.clear();
        for (String key : zones.keySet()) {
            Zone zone = zones.get(key);
            CircleOptions circleOptions = new CircleOptions()
                    .center(zone.getLatLng())
                    .clickable(true)
                    .fillColor(getResources().getColor(R.color.colorGeofenceFill, null))
                    .strokeColor(getResources().getColor(R.color.colorGeofenceStroke, null))
                    .strokeWidth(GEOFENCE_STROKE_WIDTH)
                    .radius(zone.getRadius());
            mCircles.put(key, mMap.addCircle(circleOptions));
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
            moveCameraTo(loc);
        }

        mViewModel.selectUser(user);
    }

    private void changeUiLayout(boolean isForEditMode) {
        boolean isLandscape = getContext().getResources().getBoolean(R.bool.is_landscape);

        int trans1;
        int trans2;

        TypedValue from = ResourceUtil.getTypedValue(getContext(),
                R.dimen.map_guideline_view_mode_percent);
        TypedValue to = ResourceUtil.getTypedValue(getContext(),
                R.dimen.map_guideline_edit_mode_percent);

        // move layouts
        if (!isLandscape) {
            int width = mBinding.linlayoutFrguseronmapUsers.getWidth();
            trans1 = width * (isForEditMode ? -1 : 0);
            trans2 = width * (isForEditMode ? 0 : 1);

            mBinding.conslayoutFrguseronmapEditgeofence
                    .setX(mBinding.linlayoutFrguseronmapUsers.getWidth());
            mBinding.conslayoutFrguseronmapEditgeofence.setVisibility(isForEditMode ? View.VISIBLE : View.INVISIBLE);
            mBinding.linlayoutFrguseronmapUsers
                    .animate()
                    .translationX(trans1)
                    .setDuration(ANIMATION_DURATION)
                    .alpha(1)
                    .start();
            mBinding.conslayoutFrguseronmapEditgeofence
                    .animate()
                    .translationX(trans2)
                    .setDuration(ANIMATION_DURATION)
                    .start();
        } else {
            int height = mBinding.linlayoutFrguseronmapUsers.getHeight();
            trans1 = height * (isForEditMode ? -1 : 0);
            trans2 = height * (isForEditMode ? 0 : 1);

            mBinding.conslayoutFrguseronmapEditgeofence
                    .setY(mBinding.linlayoutFrguseronmapUsers.getHeight());
            mBinding.conslayoutFrguseronmapEditgeofence.setVisibility(isForEditMode ? View.VISIBLE : View.INVISIBLE);
            mBinding.linlayoutFrguseronmapUsers
                    .animate()
                    .translationY(trans1)
                    .setDuration(ANIMATION_DURATION)
                    .alpha(1)
                    .start();
            mBinding.conslayoutFrguseronmapEditgeofence
                    .animate()
                    .translationY(trans2)
                    .setDuration(ANIMATION_DURATION)
                    .start();
        }

        // move guide line to free more space for zone edit
        final ConstraintLayout.LayoutParams lp =
                (ConstraintLayout.LayoutParams) mBinding.verticalOneThird.getLayoutParams();

        ValueAnimator animation;
        if (isForEditMode) {
            animation = ValueAnimator.ofFloat(from.getFloat(), to.getFloat());
            ((MainActivity) getActivity()).hideFab();
        } else {
            animation = ValueAnimator.ofFloat(to.getFloat(), from.getFloat());
            ((MainActivity) getActivity()).restoreFab();
        }

        animation.setDuration(ANIMATION_DURATION);
        animation.start();
        //mMap.setLatLngBoundsForCameraTarget();

        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator updatedAnimation) {
                lp.guidePercent = (float)updatedAnimation.getAnimatedValue();
                mBinding.verticalOneThird.setLayoutParams(lp);
            }

        });

        // and finally center camera on editable geofence
        if (mCurrentGeofence != null) {
            moveCameraTo(mCurrentGeofence.getCenter());
        }

    }
    private void switchToNormalMode() {
        changeUiLayout(false);
        if (mCurrentGeofence != null) {
            mCurrentGeofence.remove();
        }
        mCurrentGeofence = null;
        redrawZones();
    }

    public void startAddingGeofence() {
        changeUiLayout(true);
        mViewModel.startNewZone();
    }

    /**
     * Show confirmation dialog and asks viewmodel to remove zone if user confirms
     */
    private void startRemoveZone() {
        final SimpleDialogFragment confirmDialog = new SimpleDialogFragment();
        confirmDialog.setParms("Removing zone", "Are you sure you want to remove zone '" + mViewModel.zoneName.get() + "'?",
                "Ok", "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mViewModel.removeZone();
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        confirmDialog.dismiss();
                    }
                });
        confirmDialog.show(getActivity().getFragmentManager(), "dialog");
    }


    private void startEditGeofence(Circle circle) {
        changeUiLayout(true);
        mCurrentGeofence = circle;
        mCurrentGeofence.setFillColor(getResources().getColor(R.color.colorEditGeofenceFill, null));
        mCurrentGeofence.setStrokeColor(getResources().getColor(R.color.colorEditGeofenceStroke, null));
        for (String key : mCircles.keySet()) {
            if (mCircles.get(key).equals(circle)) {
                mViewModel.startEditZone(key);
                break;
            }
        }
    }

}
