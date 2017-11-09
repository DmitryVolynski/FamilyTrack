package com.volynski.familytrack.views;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
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
import com.volynski.familytrack.data.models.firebase.GeofenceEvent;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.databinding.ActivityMainBinding;
import com.volynski.familytrack.databinding.NavHeaderMainBinding;
import com.volynski.familytrack.utils.FragmentUtil;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.utils.SnackbarUtil;
import com.volynski.familytrack.viewmodels.MainActivityViewModel;
import com.volynski.familytrack.viewmodels.UserListViewModel;
import com.volynski.familytrack.viewmodels.UserOnMapViewModel;
import com.volynski.familytrack.views.fragments.GeofenceEventsFragment;
import com.volynski.familytrack.views.fragments.UserHistoryChartFragment;
import com.volynski.familytrack.views.fragments.UserListFragment;
import com.volynski.familytrack.views.fragments.UserMembershipFragment;
import com.volynski.familytrack.views.fragments.UserOnMapFragment;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import timber.log.Timber;

public class MainActivity
        extends AppCompatActivity
        implements
            NavigationView.OnNavigationItemSelectedListener,
            UserListNavigator {

    public static final int FAB_STYLE_ADD_ITEM = 0;
    public static final int FAB_STYLE_REMOVE_ITEM = 1;

    public static final int CONTENT_MAP = 0;
    public static final int CONTENT_USER_LIST = 1;
    public static final int CONTENT_USER_HISTORY_CHART = 2;
    public static final int CONTENT_MEMBERSHIP = 3;
    public static final int CONTENT_GEOFENCE_EVENTS = 4;
    public static final int CONTENT_INVITE_USERS = 5;
    public static final int CONTENT_USER_DETAILS = 6;

    private static final int REQUEST_CODE_EDIT_USER_DETAILS = 1000;
    private static final int REQUEST_CODE_EDIT_SETTINGS = 1001;
    private static final String TAG = "viewmodel";

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
        if (savedInstanceState == null) {
            readIntentData();
        } else {
            mContentId = savedInstanceState.getInt(StringKeys.MAIN_ACTIVITY_MODE_KEY);
            mCurrentUserUuid = savedInstanceState.getString(StringKeys.CURRENT_USER_UUID_KEY);
        }
        setupCommonContent();
        setupFragment(mContentId, false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(StringKeys.MAIN_ACTIVITY_MODE_KEY, mContentId);
        outState.putString(StringKeys.CURRENT_USER_UUID_KEY, mCurrentUserUuid);
    }

    private void setupFragment(int contentId, boolean forceRecreate) {
        Fragment newFragment =
                getSupportFragmentManager().findFragmentById(R.id.main_fcontainer);

        Object viewModel = PersistedFragmentsUtil.findOrCreateViewModel(this,
                        contentId, mCurrentUserUuid, this,  forceRecreate);
        switch (contentId) {
            case CONTENT_MAP:
                if (newFragment == null || forceRecreate) {
                    newFragment = UserOnMapFragment.newInstance(mCurrentUserUuid);
                }
                ((UserOnMapFragment) newFragment).setViewModel((UserOnMapViewModel)viewModel);
                break;
            case CONTENT_USER_LIST:
                if (newFragment == null || forceRecreate) {
                    newFragment = UserListFragment.newInstance(mCurrentUserUuid);
                }
                ((UserListFragment)newFragment).setViewModel((UserListViewModel)viewModel);
                break;
            case CONTENT_USER_HISTORY_CHART:
                newFragment = UserHistoryChartFragment.newInstance(this, mCurrentUserUuid, this);
                break;
            case CONTENT_MEMBERSHIP:
                newFragment = UserMembershipFragment.newInstance(this, mCurrentUserUuid, this);
                break;
            case CONTENT_GEOFENCE_EVENTS:
                newFragment = GeofenceEventsFragment.newInstance(this, mCurrentUserUuid, this);
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
            if (intent.hasExtra(StringKeys.CURRENT_USER_UUID_KEY)) {
                mCurrentUserUuid = intent.getStringExtra(StringKeys.CURRENT_USER_UUID_KEY);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        String snackbarText = (data.hasExtra(StringKeys.SNACKBAR_TEXT_KEY) ?
                data.getStringExtra(StringKeys.SNACKBAR_TEXT_KEY) : "");
        switch (requestCode) {
            case REQUEST_CODE_EDIT_USER_DETAILS:
                UserListFragment f =
                        (UserListFragment) FragmentUtil
                                .findFragmentByClassName(this, UserListFragment.class.getSimpleName());
                if (f != null) {
                    f.refreshList();
                }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
        SnackbarUtil.showSnackbar(getViewForSnackbar(), snackbarText);
    }


    //
    // @UserListNavigator implementation
    //

    @Override
    public void editUserDetails(String userUuid, View rootView) {
        Intent intent = new Intent(getApplicationContext(), UserDetailsActivity.class);
        intent.putExtra(StringKeys.USER_UUID_KEY, userUuid);
        intent.putExtra(StringKeys.CURRENT_USER_UUID_KEY, mCurrentUserUuid);

        View transitionImage = rootView.findViewById(R.id.imageview_userlistitem_photo);
        View transitionText = rootView.findViewById(R.id.textview_userlistitem_username);

        Pair<View, String> p1 = new Pair<>(transitionImage, "userPhoto");
        Pair<View, String> p2 = new Pair<>(transitionText, "userName");

        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this, p1, p2);

        startActivityForResult(intent, REQUEST_CODE_EDIT_USER_DETAILS, options.toBundle());
    }

    private void editSettings(String userUuid) {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        intent.putExtra(StringKeys.CURRENT_USER_UUID_KEY, userUuid);
        startActivityForResult(intent, REQUEST_CODE_EDIT_SETTINGS);
    }

    @Override
    public void removeUser(String userUuid) {

    }

    @Override
    public void inviteUsers() {

    }

    @Override
    public void dismissInviteUsersDialog() {
        UserListFragment f =
                (UserListFragment) FragmentUtil
                        .findFragmentByClassName(this, UserListFragment.class.getSimpleName());
        if (f != null) {
            f.dismissInviteUsersDialog();
        }
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

    @Override
    public void eventClicked(GeofenceEvent event, String mUiContext) {
        GeofenceEventsFragment f =
                (GeofenceEventsFragment) FragmentUtil
                        .findFragmentByClassName(this, GeofenceEventsFragment.class.getSimpleName());
        if (f != null) {
            f.eventClicked(event);
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
                    f0.showInviteUsersDialog(null);
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
            case CONTENT_GEOFENCE_EVENTS:
                GeofenceEventsFragment f3 =
                        (GeofenceEventsFragment) FragmentUtil
                        .findFragmentByClassName(this, GeofenceEventsFragment.class.getSimpleName());
                if (f3 != null) {
                    f3.deleteEvents();
                }
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
                    setupFragment(mContentId, true);
                    break;
                case (R.id.drawer_nav_users):
                    mContentId = CONTENT_USER_LIST;
                    setupFragment(mContentId, true);
                    break;
                case (R.id.drawer_nav_chart):
                    mContentId = CONTENT_USER_HISTORY_CHART;
                    setupFragment(mContentId, true);
                    break;
                case (R.id.drawer_nav_membership):
                    mContentId = CONTENT_MEMBERSHIP;
                    setupFragment(mContentId, true);
                    break;
                case (R.id.drawer_nav_settings):
                    editSettings(mCurrentUserUuid);
                    break;
                case (R.id.drawer_nav_about):
                    break;
                case (R.id.drawer_nav_geofences):
                    mContentId = CONTENT_GEOFENCE_EVENTS;
                    setupFragment(mContentId, true);
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

    public void setFabStyle(int fabStyle) {
        int resId = R.drawable.ic_action_add;
        switch (fabStyle) {
            case FAB_STYLE_REMOVE_ITEM:
                resId = R.drawable.ic_delete_inverse;
                break;
        }
        mFab.setImageResource(resId);
    }
}
