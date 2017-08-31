package com.volynski.familytrack.views;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.volynski.familytrack.R;
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

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mViewPager.setAdapter(new TabViewPageAdapter(getSupportFragmentManager(),
                this, this));

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
        Timber.v("Not implemented");
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

    /**
     * Replaces user list fragment
     */
    @Override
    public void inviteUsers() {
        Timber.v("Invite users");
        InviteUsersDialogFragment dialogFragment = InviteUsersDialogFragment.newInstance(this, this);
        dialogFragment.show(getSupportFragmentManager(), "aa");


        //CoordinatorLayout layout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout_main);

        // inflate the layout of the popup window
        //LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        //final View popupView = inflater.inflate(R.layout.fragment_invite_users, null);

        // create the popup window
        //int width =  (int) Math.round(0.8 * layout.getWidth());  //LinearLayout.LayoutParams.WRAP_CONTENT;
        //int height = (int) Math.round(0.8 * layout.getHeight());  //LinearLayout.LayoutParams.WRAP_CONTENT;

        //boolean focusable = true; // lets taps outside the popup also dismiss it
        //final PopupWindow popupWindow =
        //        new PopupWindow(dialogFragment.getRootView(), width, height, focusable);

        // show the popup window
        //popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);

    }
}
