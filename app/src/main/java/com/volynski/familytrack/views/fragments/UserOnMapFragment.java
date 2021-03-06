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

    private boolean mNewlyCreated = true;

    private UserOnMapViewModel mViewModel;
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;

    private LinearLayoutManager mLayoutManager;
    private HashMap<String, Marker> mMarkers = new HashMap<>();
    private HashMap<String, Circle> mCircles = new HashMap<>();
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
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(R.string.toolbar_title_map);
        if (mViewModel.isCreatedFromViewHolder() || !mNewlyCreated) {
            if (mViewModel.zoneEditMode.get() == UserOnMapViewModel.EM_EDIT ||
                    mViewModel.zoneEditMode.get() == UserOnMapViewModel.EM_NEW) {
                changeUiLayout(true);
            }
            mViewModel.start();
        } else {
            mViewModel.start();
        }
        mNewlyCreated = false;
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
        mNewlyCreated = true;
    }

    private void setupMapFragment() {
        mGeoDataClient = Places.getGeoDataClient(this.getActivity(), null);
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this.getActivity(), null);

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
        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_user_on_map,
                container,
                false);

        setupMapFragment();
        setupToggleButtons();
        setupSnackbar();
        setupCustomListeners();

        boolean isLandscape = getResources().getBoolean(R.bool.is_landscape);

        mLayoutManager = new LinearLayoutManager(getContext(),
                (isLandscape ? LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL), false);
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
                if (mCurrentGeofence != null) {
                    mCurrentGeofence.setRadius(mViewModel.zoneRadius.get());
                }
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
            if (!mBinding.tbutFrguseronmapOff.isChecked() &&
                    mViewModel.getSelectedUser() != null) {
                // redraw path if time period selected & selected user is not null
                redrawPath();
            }

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

        if (mViewModel.zoneEditMode.get() == UserOnMapViewModel.EM_NEW ||
                mViewModel.zoneEditMode.get() == UserOnMapViewModel.EM_EDIT) {
            LatLng latLng = new LatLng(mViewModel.zoneCenterLatitude.get(),
                    mViewModel.zoneCenterLongitude.get());
            CircleOptions circleOptions = new CircleOptions()
                    .center(latLng)
                    .clickable(true)
                    .fillColor(getResources().getColor(R.color.colorEditGeofenceFill, null))
                    .strokeColor(getResources().getColor(R.color.colorEditGeofenceStroke, null))
                    .strokeWidth(GEOFENCE_STROKE_WIDTH)
                    .radius(mViewModel.zoneRadius.get());
            mCurrentGeofence = mMap.addCircle(circleOptions);

            moveCameraTo(latLng);
            return;
        }

        if (mViewModel != null &&
                mViewModel.getSelectedUser() != null &&
                mViewModel.getSelectedUser().getLastKnownLocation() != null) {
            moveCameraTo(mViewModel.getSelectedUser().getLastKnownLocation().getLatLng());
        }
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
        PolylineOptions options = new PolylineOptions();
        for (Location location : mViewModel.path) {
            options.add(location.getLatLng());
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
                Float markerColor = BitmapDescriptorFactory.HUE_AZURE;
                if (mViewModel.getSelectedUser() != null &&
                        mViewModel.zoneEditMode.get() == UserOnMapViewModel.EM_NONE &&
                        mViewModel.getSelectedUser().getUserUuid().equals(user.getUserUuid())) {
                    markerColor = BitmapDescriptorFactory.HUE_ORANGE;
                    newCameraPos = location.getLatLng();
                }
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(location.getLatLng())
                        .title(user.getDisplayName())
                        .snippet(user.getTextForSnippet())
                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor));
                mMarkers.put(user.getUserUuid(), mMap.addMarker(markerOptions));
            }
        }

        if (newCameraPos != null) {
            if (!mMap.getProjection().getVisibleRegion().latLngBounds.contains(newCameraPos) ||
                    mMap.getCameraPosition().zoom < 2.1 ) {
                // if marker of selected user is out of bounds - center map to
                // appropriate coordinates (current location of selected user)
                moveCameraTo(newCameraPos);
            }
        }
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
            if (!(mViewModel.zoneEditMode.get() == UserOnMapViewModel.EM_EDIT &&
                    key.equals(mViewModel.getEditZoneUuid()))) {
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
        mViewModel.selectUser(user, false);
        if (user.getLastKnownLocation() != null) {
            LatLng loc = user.getLastKnownLocation().getLatLng();
            moveCameraTo(loc);
        }
        redrawMarkers();
    }


    private void changeUiLayout(boolean isForEditMode) {
        if (!isAdded()) {
            return;
        }

        boolean isLandscape = getContext().getApplicationContext()
                .getResources().getBoolean(R.bool.is_landscape);

        int trans1;
        int trans2;

        TypedValue fromRightGuideLine = ResourceUtil.getTypedValue(getContext(),
                R.dimen.map_guideline_view_mode_percent);
        TypedValue toRightGuideLine = ResourceUtil.getTypedValue(getContext(),
                R.dimen.map_guideline_edit_mode_percent);

        mBinding.linlayoutFrguseronmapUsers.setVisibility(isForEditMode ? View.INVISIBLE : View.VISIBLE);
        mBinding.conslayoutFrguseronmapEditgeofence.setVisibility(isForEditMode ? View.VISIBLE : View.INVISIBLE);
        // move layouts
        if (!isLandscape) {
            int width = mBinding.linlayoutFrguseronmapUsers.getWidth();
            trans1 = width * (isForEditMode ? -1 : 0);
            trans2 = width * (isForEditMode ? 0 : 1);

            mBinding.conslayoutFrguseronmapEditgeofence
                    .setX(mBinding.linlayoutFrguseronmapUsers.getWidth());
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
        final ConstraintLayout.LayoutParams lpRight =
                (ConstraintLayout.LayoutParams) mBinding.verticalOneThird.getLayoutParams();

        ValueAnimator animationRight;
        ValueAnimator animationLeft;

        if (isForEditMode) {
            animationRight = ValueAnimator.ofFloat(fromRightGuideLine.getFloat(), toRightGuideLine.getFloat());
            ((MainActivity) getActivity()).hideFab();
        } else {
            animationRight = ValueAnimator.ofFloat(toRightGuideLine.getFloat(), fromRightGuideLine.getFloat());
            ((MainActivity) getActivity()).restoreFab();
        }

        if (isLandscape) {
            TypedValue fromLeftGuideLine = ResourceUtil.getTypedValue(getContext(),
                    R.dimen.map_leftguideline_view_mode_percent);
            TypedValue toLeftGuideLine = ResourceUtil.getTypedValue(getContext(),
                    R.dimen.map_leftguideline_edit_mode_percent);

            if (isForEditMode) {
                animationLeft = ValueAnimator.ofFloat(fromLeftGuideLine.getFloat(), toLeftGuideLine.getFloat());
            } else {
                animationLeft = ValueAnimator.ofFloat(toLeftGuideLine.getFloat(), fromLeftGuideLine.getFloat());
            }
            animationLeft.setDuration(ANIMATION_DURATION);
            animationLeft.start();

            final ConstraintLayout.LayoutParams lpLeft =
                    (ConstraintLayout.LayoutParams) mBinding.verticalOneTen.getLayoutParams();

            animationLeft.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator updatedAnimation) {
                    lpLeft.guidePercent = (float)updatedAnimation.getAnimatedValue();
                    mBinding.verticalOneTen.setLayoutParams(lpLeft);
                }

            });

        }

        animationRight.setDuration(ANIMATION_DURATION);
        animationRight.start();

        animationRight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator updatedAnimation) {
                lpRight.guidePercent = (float)updatedAnimation.getAnimatedValue();
                mBinding.verticalOneThird.setLayoutParams(lpRight);
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
        confirmDialog.setParms(getString(R.string.remove_zone_dialog_title),
                String.format(getString(R.string.remove_zone_dialog_mesage), mViewModel.zoneName.get()),
                getString(R.string.label_button_ok),
                getString(R.string.button_cancel_label),
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
        confirmDialog.show(getActivity().getFragmentManager(), getString(R.string.dialog_tag));
    }


    private void startEditGeofence(Circle circle) {
        mCurrentGeofence = circle;
        mCurrentGeofence.setFillColor(getResources().getColor(R.color.colorEditGeofenceFill, null));
        mCurrentGeofence.setStrokeColor(getResources().getColor(R.color.colorEditGeofenceStroke, null));
        for (String key : mCircles.keySet()) {
            if (mCircles.get(key).equals(circle)) {
                mViewModel.startEditZone(key);
                break;
            }
        }
        changeUiLayout(true);
    }

    public UserOnMapViewModel getViewModel() {
        return mViewModel;
    }
}
