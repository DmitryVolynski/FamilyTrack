package com.volynski.familytrack.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.volynski.familytrack.R;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.adapters.TabViewPageAdapter;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.utils.FragmentUtil;
import com.volynski.familytrack.viewmodels.UserHistoryChartViewModel;
import com.volynski.familytrack.viewmodels.UserOnMapViewModel;
import com.volynski.familytrack.views.fragments.InviteUsersDialogFragment;
import com.volynski.familytrack.views.fragments.UserHistoryChartFragment;
import com.volynski.familytrack.views.fragments.UserListFragment;
import com.volynski.familytrack.views.fragments.UserOnMapFragment;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.util.List;

import timber.log.Timber;

public class OldMainActivity
        extends AppCompatActivity
        implements UserListNavigator, GoogleApiClient.OnConnectionFailedListener {

    private static final String USER_LIST_FRAGMENT = UserListFragment.class.getSimpleName();
    private static final String USER_ON_MAP_FRAGMENT = UserOnMapFragment.class.getSimpleName();

    private String mCurrentUserUuid = "";
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private InviteUsersDialogFragment mInviteUsersDialog;
    private GoogleApiClient mGoogleApiClient;
    private FloatingActionButton mFab;

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
        setContentView(R.layout.activity_main_old);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                proceedFabButton();
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });
        
        setupTabView();
        initGoogleApiClient();
    }

    private void proceedFabButton() {
        int tpos = mTabLayout.getSelectedTabPosition();
        switch (tpos) {
            case 0:
                UserListFragment f0 =
                        (UserListFragment)FragmentUtil.findFragmentByClassName(this, USER_LIST_FRAGMENT);
                if (f0 != null) {
                    f0.inviteUser();
                }
                break;

            case 1:
                UserOnMapFragment f1 =
                        (UserOnMapFragment)FragmentUtil.findFragmentByClassName(this, USER_ON_MAP_FRAGMENT);
                if (f1 != null) {
                    f1.startAddingGeofence();
                    hideFab();

                }
                break;
        }

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


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        int i = 0;
    }

    @Override
    protected void onStart() {
        super.onStart();
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
    public void userClicked(User user, String uiContext) {
        switch(uiContext) {
            case UserOnMapViewModel.UI_CONTEXT :
                UserOnMapFragment f1 =
                        (UserOnMapFragment) FragmentUtil.findFragmentByClassName(this, UserOnMapFragment.class.getSimpleName());
                if (f1 != null) {
                    mTabLayout.getTabAt(1).select();
                    f1.userClicked(user);
                }
                break;
            case UserHistoryChartViewModel.UI_CONTEXT:
                UserHistoryChartFragment f2 =
                        (UserHistoryChartFragment)FragmentUtil.findFragmentByClassName(this, UserHistoryChartFragment.class.getSimpleName());
                if (f2 != null) {
                    mTabLayout.getTabAt(2).select();
                    f2.userClicked(user);
                }
                break;
            default:
                Timber.v("Unexpected case=" + uiContext);
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

    public View getViewForSnackbar() {
        return findViewById(R.id.coordinatorlayout_main);
    }

    public void hideFab() {
        mFab.animate().scaleX(0).scaleY(0).rotation(180).setDuration(200).start();
        mFab.setVisibility(View.INVISIBLE);
    }

    public void restoreFab() {
        mFab.animate().scaleX(1).scaleY(1).rotation(180).setDuration(200).start();
        mFab.setVisibility(View.VISIBLE);
    }
}
