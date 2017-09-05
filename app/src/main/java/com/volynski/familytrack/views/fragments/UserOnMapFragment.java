package com.volynski.familytrack.views.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
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

import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.volynski.familytrack.BR;
import com.volynski.familytrack.R;
import com.volynski.familytrack.adapters.RecyclerViewListAdapter;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.databinding.FragmentUserOnMapBinding;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.viewmodels.UserOnMapViewModel;
import com.google.android.gms.location.places.GeoDataClient;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 22.08.2017.
 */

public class UserOnMapFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = UserOnMapFragment.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private UserOnMapViewModel mViewModel;
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private String mCurrentUserUuid;
    private LinearLayoutManager mLayoutManager;
    private static final int DEFAULT_ZOOM = 15;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupMapFragment();
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
        mCurrentUserUuid = SharedPrefsUtil.getCurrentUserUuid(getContext());

        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_user_on_map,
                container,
                false);
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.recyclerviewFrguseronmapUserslist.setLayoutManager(mLayoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(getContext(), mLayoutManager.getOrientation());

        mBinding.recyclerviewFrguseronmapUserslist.addItemDecoration(dividerItemDecoration);
        mAdapter = new RecyclerViewListAdapter(this.getContext(), mViewModel.users,
                R.layout.user_horizontal_list_item, BR.viewmodel);

        mBinding.recyclerviewFrguseronmapUserslist.setAdapter(mAdapter);
        mBinding.setViewmodel(mViewModel);

        return mBinding.getRoot();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getLocationPermission();
        try {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } catch (Exception e) {
            Timber.e(e);
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

    public void moveCameraTo(double latitude, double longitude) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(55.994017, 37.195024), DEFAULT_ZOOM));
    }
}
