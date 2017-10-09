package com.volynski.familytrack.views;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.widget.DrawerLayout;

import com.google.android.gms.common.api.GoogleApiClient;
import com.volynski.familytrack.R;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.databinding.ActivityMainBinding;
import com.volynski.familytrack.databinding.NavHeaderMainBinding;
import com.volynski.familytrack.utils.FragmentUtil;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.viewmodels.MainActivityViewModel;
import com.volynski.familytrack.views.fragments.UserHistoryChartFragment;
import com.volynski.familytrack.views.fragments.UserListFragment;
import com.volynski.familytrack.views.fragments.UserMembershipFragment;
import com.volynski.familytrack.views.fragments.UserOnMapFragment;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.util.List;

import timber.log.Timber;

public class MainActivity
        extends AppCompatActivity
        implements
            NavigationView.OnNavigationItemSelectedListener,
            UserListNavigator {

    public static final int CONTENT_MAP = 0;
    public static final int CONTENT_USER_LIST = 1;
    private static final int CONTENT_USER_HISTORY_CHART = 2;
    private static final int CONTENT_MEMBERSHIP = 3;

    private String mCurrentUserUuid;
    private int mContentId;
    private ActivityMainBinding mBinding;
    private MainActivityViewModel mViewModel;
    private GoogleApiClient mGoogleApiClient;
    private FloatingActionButton mFab;
    private int mCurrentMenuId = R.id.drawer_nav_map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readIntentData();
        setupCommonContent();
        setupFragment(mContentId);
    }

    private void setupFragment(int contentId) {
        Fragment newFragment = null;
        switch (contentId) {
            case CONTENT_MAP:
                newFragment = UserOnMapFragment.newInstance(this, mCurrentUserUuid, this);
                break;
            case CONTENT_USER_LIST:
                newFragment = UserListFragment.newInstance(this, mCurrentUserUuid, this);
                break;
            case CONTENT_USER_HISTORY_CHART:
                newFragment = UserHistoryChartFragment.newInstance(this, mCurrentUserUuid, this);
                break;
            case CONTENT_MEMBERSHIP:
                newFragment = UserMembershipFragment.newInstance(this, mCurrentUserUuid, this);
                break;
            default:
                Timber.v("Unsupported content id=" + contentId);
        }
        if (newFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_fcontainer, newFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void readIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(StringKeys.USER_UUID_KEY)) {
                mCurrentUserUuid = intent.getStringExtra(StringKeys.USER_UUID_KEY);
            } else {
                Timber.e("Current user uuid expected but not found in intent");
                return;
            }
            if (intent.hasExtra(StringKeys.MAIN_ACTIVITY_MODE_KEY)) {
                mContentId = intent.getIntExtra(StringKeys.MAIN_ACTIVITY_MODE_KEY, 0);
            }
        } else {
            Timber.e("Intent is null");
        }
    }


    //
    // @UserListNavigator implementation
    //

    @Override
    public void openUserDetails(String userUuid) {

    }

    @Override
    public void removeUser(String userUuid) {

    }

    @Override
    public void inviteUsers() {

    }

    @Override
    public void dismissInviteUsersDialog() {

    }

    @Override
    public void userClicked(User user, String uiContext) {
        switch (mContentId) {
            case CONTENT_USER_HISTORY_CHART:
                UserHistoryChartFragment f0 =
                        (UserHistoryChartFragment) FragmentUtil
                                .findFragmentByClassName(this, UserHistoryChartFragment.class.getSimpleName());
                if (f0 != null) {
                    f0.userClicked(user);
                }
                break;

            case CONTENT_MAP:
                UserOnMapFragment f1 =
                        (UserOnMapFragment)FragmentUtil
                                .findFragmentByClassName(this, UserOnMapFragment.class.getSimpleName());
                if (f1 != null) {
                    f1.userClicked(user);
                }
                break;
            default:
                Timber.v("Unsupported content id=" + mContentId);
        }
    }

    //
    // activity ui setup functions
    //
    private void setupCommonContent() {

        mViewModel = new MainActivityViewModel(this, mCurrentUserUuid,
                new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(this), this));


        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavHeaderMainBinding _bind =
                DataBindingUtil.inflate(getLayoutInflater(),
                        R.layout.nav_header_main, mBinding.navView, false);
        mBinding.navView.addHeaderView(_bind.getRoot());
        _bind.setViewmodel(mViewModel);

        DrawerLayout drawer = mBinding.drawerLayout;  //(DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle); // .addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabButtonClicked();
            }
        });
    }

    private void fabButtonClicked() {
        switch (mContentId) {
            case CONTENT_USER_LIST:
                UserListFragment f0 =
                        (UserListFragment) FragmentUtil
                                .findFragmentByClassName(this, UserListFragment.class.getSimpleName());
                if (f0 != null) {
                    f0.inviteUser();
                }
                break;

            case CONTENT_MAP:
                UserOnMapFragment f1 =
                        (UserOnMapFragment)FragmentUtil
                                .findFragmentByClassName(this, UserOnMapFragment.class.getSimpleName());
                if (f1 != null) {
                    f1.startAddingGeofence();
                    hideFab();
                }
                break;
            case CONTENT_MEMBERSHIP:
                UserMembershipFragment f2 =
                        (UserMembershipFragment) FragmentUtil
                        .findFragmentByClassName(this, UserMembershipFragment.class.getSimpleName());
                if (f2 != null) {
                    f2.createNewGroup();
                }
                break;
            default:
                Timber.v("Unsupported content id=" + mContentId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewModel.start();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id != mCurrentMenuId) {
            switch (id) {
                case (R.id.drawer_nav_map):
                    mContentId = CONTENT_MAP;
                    setupFragment(mContentId);
                    break;
                case (R.id.drawer_nav_users):
                    mContentId = CONTENT_USER_LIST;
                    setupFragment(mContentId);
                    break;
                case (R.id.drawer_nav_chart):
                    mContentId = CONTENT_USER_HISTORY_CHART;
                    setupFragment(mContentId);
                    break;
                case (R.id.drawer_nav_membership):
                    mContentId = CONTENT_MEMBERSHIP;
                    setupFragment(mContentId);
                    break;
                case (R.id.drawer_nav_settings):
                    break;
                case (R.id.drawer_nav_about):
                    break;
                case (R.id.drawer_nav_geofences):
                    break;
                default:
                    Timber.v("Unsupported menu id=" + id);
            }
        }
        mCurrentMenuId = id;
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public View getViewForSnackbar() {
        return findViewById(R.id.root_coordinatorlayout);
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
