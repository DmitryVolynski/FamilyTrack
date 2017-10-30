package com.volynski.familytrack.views.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.volynski.familytrack.BR;
import com.volynski.familytrack.R;
import com.volynski.familytrack.adapters.RecyclerViewListAdapter;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.models.firebase.GeofenceEvent;
import com.volynski.familytrack.databinding.FragmentGeofenceEventsBinding;
import com.volynski.familytrack.databinding.FragmentUserListBinding;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.viewmodels.GeofenceEventsViewModel;
import com.volynski.familytrack.viewmodels.UserListViewModel;
import com.volynski.familytrack.viewmodels.UserOnMapViewModel;
import com.volynski.familytrack.views.MainActivity;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 30.10.2017.
 */

public class GeofenceEventsFragment extends Fragment
        implements OnMapReadyCallback {

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private GeofenceEventsViewModel mViewModel;
    private String mCurrentUserUuid;
    private UserListNavigator mUserListNavigator;
    private LinearLayoutManager mLayoutManager;
    private Circle mCurrentGeofence;

    FragmentGeofenceEventsBinding mBinding;
    private RecyclerViewListAdapter mAdapter;

    public static GeofenceEventsFragment newInstance(Context context,
                                               String currentUserUuid,
                                               UserListNavigator navigator) {

        GeofenceEventsFragment result = new GeofenceEventsFragment();

        // TODO проверить это место. может быть создание модели именно здесь неверно.
        GeofenceEventsViewModel viewModel = new GeofenceEventsViewModel(context, currentUserUuid,
                new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(context), context));
        viewModel.setNavigator(navigator);
        result.setViewModel(viewModel);
        result.setCurrentUserUuid(currentUserUuid);
        result.setNavigator(navigator);
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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_geofence_events,
                container,
                false);
        mLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        mBinding.recyclerviewFrggeofenceevents.setLayoutManager(mLayoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mBinding.recyclerviewFrggeofenceevents.getContext(),
                        mLayoutManager.getOrientation());

        mBinding.recyclerviewFrggeofenceevents.addItemDecoration(dividerItemDecoration);
        mAdapter = new RecyclerViewListAdapter(this.getContext(), mViewModel.viewModels,
                R.layout.geofence_event_list_item, BR.viewmodel);
        //mAdapter.enablePopupMenu(R.menu.user_popup_menu, R.id.imageview_userslistitem_popupsymbol);

        mBinding.recyclerviewFrggeofenceevents.setAdapter(mAdapter);
        mBinding.setViewmodel(mViewModel);

        ((MainActivity)getActivity()).setFabStyle(MainActivity.FAB_STYLE_REMOVE_ITEM);

        setupMapFragment();
        setupListeners();
        return mBinding.getRoot();
    }

    private void setupListeners() {
        mViewModel.redrawZone.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                showZoneFromSelectedEvent();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mViewModel.redrawZone.removeOnPropertyChangedCallback();
    }

    //
    private void showZoneFromSelectedEvent() {
        if (mCurrentGeofence == null) {
            CircleOptions circleOptions = new CircleOptions()
                    .center(mViewModel.zoneToShow.getLatLng())
                    .clickable(true)
                    .fillColor(getResources().getColor(R.color.colorEditGeofenceFill, null))
                    .strokeColor(getResources().getColor(R.color.colorEditGeofenceStroke, null))
                    .strokeWidth(UserOnMapFragment.GEOFENCE_STROKE_WIDTH)
                    .radius(mViewModel.zoneToShow.getRadius());
            mCurrentGeofence = mMap.addCircle(circleOptions);
        } else {
            mCurrentGeofence.setCenter(mViewModel.zoneToShow.getLatLng());
            mCurrentGeofence.setRadius(mViewModel.zoneToShow.getRadius());
        }
        moveCameraTo(mViewModel.zoneToShow.getLatLng());
    }

    public void moveCameraTo(LatLng loc) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, UserOnMapFragment.DEFAULT_ZOOM));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void setupMapFragment() {
        mMapFragment = SupportMapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.map_frggeofenceevents, mMapFragment);
        fragmentTransaction.commit();
        mMapFragment.getMapAsync(this);
    }

    public void setViewModel(GeofenceEventsViewModel mViewModel) {
        this.mViewModel = mViewModel;
    }


    public void refreshList() {
        mViewModel.start();
    }

    public void setCurrentUserUuid(String currentUserUuid) {
        this.mCurrentUserUuid = currentUserUuid;
    }

    public UserListNavigator getNavigator() {
        return mUserListNavigator;
    }

    public void setNavigator(UserListNavigator userListNavigator) {
        this.mUserListNavigator = userListNavigator;
    }

    public void eventClicked(GeofenceEvent event) {
        mViewModel.selectEvent(event);
    }
}