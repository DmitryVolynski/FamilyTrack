package com.volynski.familytrack.views;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.volynski.familytrack.R;
import com.volynski.familytrack.adapters.TabViewPageAdapter;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import timber.log.Timber;

public class MainActivity
        extends AppCompatActivity
        implements UserListNavigator {

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
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager_main);
        viewPager.setAdapter(new TabViewPageAdapter(getSupportFragmentManager(),
                MainActivity.this));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs_main);
        tabLayout.setupWithViewPager(viewPager);
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
    public void showUserOnMap(String userUuid) {
        Timber.v("Not implemented");
    }

    @Override
    public void inviteUser() {
        Timber.v("Not implemented");
    }
}
