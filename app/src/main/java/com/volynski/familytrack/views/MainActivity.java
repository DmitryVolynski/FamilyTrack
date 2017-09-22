package com.volynski.familytrack.views;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.volynski.familytrack.R;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.adapters.TabViewPageAdapter;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.Location;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.views.fragments.InviteUsersDialogFragment;
import com.volynski.familytrack.views.fragments.UserListFragment;
import com.volynski.familytrack.views.fragments.UserOnMapFragment;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.util.Calendar;
import java.util.List;

import timber.log.Timber;

public class MainActivity
        extends AppCompatActivity
        implements UserListNavigator, GoogleApiClient.OnConnectionFailedListener {

    private static final String USER_LIST_FRAGMENT = UserListFragment.class.getSimpleName();
    private static final String USER_ON_MAP_FRAGMENT = UserOnMapFragment.class.getSimpleName();

    private String mCurrentUserUuid = "";
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private InviteUsersDialogFragment mInviteUsersDialog;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent.hasExtra(StringKeys.USER_UUID_KEY)) {
            mCurrentUserUuid = intent.getStringExtra(StringKeys.USER_UUID_KEY);
        } else {
            Timber.e("Current user uuid expected but not found in intent");
            return;
        }
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        
        setupTabView();
        initGoogleApiClient();
    }

    private void setupTabView() {
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        mViewPager = (ViewPager) findViewById(R.id.viewpager_main);
        mViewPager.setAdapter(new TabViewPageAdapter(mCurrentUserUuid,
                getSupportFragmentManager(), this, this));

        // Give the TabLayout the ViewPager
        mTabLayout = (TabLayout) findViewById(R.id.tabs_main);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    /**
     *
     * @param fragmentClassName
     * @return
     */
    // TODO Move this method to utils class
    private Fragment findFragmentByClassName(String fragmentClassName) {
        Fragment result = null;
        List<Fragment> fragments = getSupportFragmentManager().getFragments();

        for (Fragment fragment : fragments) {
            if (fragment.getClass().getSimpleName().equals(fragmentClassName)) {
                result = fragment;
                break;
            }
        }
        return result;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        int i = 0;
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*
        Timber.v("Started at " + Calendar.getInstance().getTime().toString());
        Timber.v("isConnected=" + String.valueOf(mGoogleApiClient.isConnected()));
        PendingResult<PlaceLikelihoodBuffer> result =
                Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {

            @Override
            public void onResult(@NonNull PlaceLikelihoodBuffer placeLikelihoods) {
                Timber.v("isSuccess=" + String.valueOf(placeLikelihoods.getStatus().isSuccess()));
                if (placeLikelihoods.getStatus().isSuccess()) {
                    if (placeLikelihoods.getCount() > 0) {
                        Timber.v(placeLikelihoods.get(0).getPlace().getName().toString());
                        //updateUserLocation(mUserUuid, placeLikelihoods.get(0));
                    } else {
                        Timber.v("=0");
                        //mCallback.onTaskCompleted();
                    }
                } else {
                    Timber.v("!= success");
                    //mCallback.onTaskCompleted();
                }
                placeLikelihoods.release();
            }
        });
        //PlaceLikelihoodBuffer placeLikelihoods = result.await();
        Timber.v("Done");
        */
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void initGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .enableAutoManage(this, this)
                    .build();
            //mGoogleApiClient.connect();
        }
    }

    // ---
    // UserListNavigator implementation
    // ---
    @Override
    public void openUserDetails(String userUuid) {
        Intent intent = new Intent(this, UserDetailsActivity.class);
        intent.putExtra(StringKeys.USER_UUID_KEY, userUuid);
        startActivity(intent);
    }

    @Override
    public void removeUser(String userUuid) {
        Timber.v("Not implemented");
    }

    @Override
    public void showUserOnMap(User user) {
        UserOnMapFragment fragment =
                (UserOnMapFragment)findFragmentByClassName(UserOnMapFragment.class.getSimpleName());
        if (fragment != null) {
            mTabLayout.getTabAt(1).select();
            fragment.userClicked(user);
        }
    }

    @Override
    public void dismissInviteUsersDialog() {
        if (mInviteUsersDialog != null)  {
            mInviteUsersDialog.dismiss();
            mInviteUsersDialog = null;
        }
    }

    /**
     * Replaces user list fragment
     */
    @Override
    public void inviteUsers() {
        Timber.v("Invite mUsers");
        mInviteUsersDialog = InviteUsersDialogFragment.newInstance(this, mCurrentUserUuid, this);
        mInviteUsersDialog.show(getSupportFragmentManager(), "aa");
    }
}
