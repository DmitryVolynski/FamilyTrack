package com.volynski.familytrack.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.volynski.familytrack.R;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.adapters.TabViewPageAdapter;
import com.volynski.familytrack.views.fragments.InviteUsersDialogFragment;
import com.volynski.familytrack.views.fragments.UserListFragment;
import com.volynski.familytrack.views.fragments.UserOnMapFragment;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.util.List;

import timber.log.Timber;

public class MainActivity
        extends AppCompatActivity
        implements UserListNavigator {

    private static final String USER_LIST_FRAGMENT = UserListFragment.class.getSimpleName();
    private static final String USER_ON_MAP_FRAGMENT = UserOnMapFragment.class.getSimpleName();

    private String mCurrentUserUuid = "";
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private InviteUsersDialogFragment mInviteUsersDialog;

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
    public void showUserOnMap(double latitude, double longitude) {
        UserOnMapFragment fragment =
                (UserOnMapFragment)findFragmentByClassName(UserOnMapFragment.class.getSimpleName());
        if (fragment != null) {
            mTabLayout.getTabAt(1).select();
            fragment.moveCameraTo(latitude, longitude);
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
