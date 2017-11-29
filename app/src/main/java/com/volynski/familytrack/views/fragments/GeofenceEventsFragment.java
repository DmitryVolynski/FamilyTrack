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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.adapters.RecyclerViewListAdapter;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.models.firebase.GeofenceEvent;
import com.volynski.familytrack.databinding.FragmentGeofenceEventsBinding;
import com.volynski.familytrack.databinding.FragmentUserListBinding;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.utils.SnackbarUtil;
import com.volynski.familytrack.viewmodels.GeofenceEventListItemViewModel;
import com.volynski.familytrack.viewmodels.GeofenceEventsViewModel;
import com.volynski.familytrack.viewmodels.UserListViewModel;
import com.volynski.familytrack.viewmodels.UserOnMapViewModel;
import com.volynski.familytrack.views.MainActivity;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.util.List;

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
    private LinearLayoutManager mLayoutManager;
    private Circle mCurrentGeofence;
    private Observable.OnPropertyChangedCallback mSnackbarCallback;

    FragmentGeofenceEventsBinding mBinding;
    private RecyclerViewListAdapter mAdapter;

    public static GeofenceEventsFragment newInstance(String currentUserUuid) {
        Bundle args = new Bundle();
        args.putString(StringKeys.CURRENT_USER_UUID_KEY, currentUserUuid);

        GeofenceEventsFragment result = new GeofenceEventsFragment();
        result.setArguments(args);

        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(R.string.toolbar_title_geofence_events);
        if (getArguments() == null) {
            Timber.e(getString(R.string.ex_no_user_uuid_in_intent));
            return;
        }
        if (mViewModel.isCreatedFromViewHolder()) {
            return;
        }
        mViewModel.start(getArguments().getString(StringKeys.CURRENT_USER_UUID_KEY));
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

        View view = setupFragmentContent(inflater, container, savedInstanceState);
        setupMapFragment();
        setupListeners();
        setupSnackbar();

        return view;
    }

    private View setupFragmentContent(LayoutInflater inflater,
                                      ViewGroup container,
                                      Bundle savedInstanceState) {
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
        mBinding.recyclerviewFrggeofenceevents.setAdapter(mAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String eventUuid = ((List<GeofenceEventListItemViewModel>)mAdapter.getViewModels())
                        .get(viewHolder.getAdapterPosition()).event.get().getEventUuid();
                mViewModel.deleteEvent(eventUuid);
            }
        }).attachToRecyclerView(mBinding.recyclerviewFrggeofenceevents);

        mBinding.setViewmodel(mViewModel);

        return mBinding.getRoot();
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

    private void setupListeners() {
        mViewModel.redrawZone.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                showZoneFromSelectedEvent();
            }
        });
    }

    //
    private void showZoneFromSelectedEvent() {
        if (mViewModel.zoneToShow == null) {
            if (mCurrentGeofence != null) {
                // nothing to show, just hide circle if it still visible
                mCurrentGeofence.remove();
                mCurrentGeofence = null;
            }
            return;
        }

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
        if (mViewModel.isCreatedFromViewHolder()) {
            showZoneFromSelectedEvent();
        }

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

    public void eventClicked(GeofenceEvent event) {
        mViewModel.selectEvent(event);
    }

    /**
     * deletes all events for current user
     */
    public void deleteEvents() {
        mViewModel.deleteEvents();
    }
}
