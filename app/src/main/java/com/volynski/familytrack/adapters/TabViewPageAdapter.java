package com.volynski.familytrack.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.volynski.familytrack.views.fragments.InviteUsersDialogFragment;
import com.volynski.familytrack.views.fragments.UserListFragment;
import com.volynski.familytrack.views.fragments.UserOnMapFragment;
import com.volynski.familytrack.views.navigators.UserListNavigator;

/**
 * Created by DmitryVolynski on 22.08.2017.
 */

public class TabViewPageAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 3;
    private String tabTitles[] = new String[] { "Users", "Map", "Invites" };
    private Context context;
    private UserListNavigator mNavigator;

    public TabViewPageAdapter(FragmentManager fm,
                              Context context,
                              UserListNavigator navigator) {
        super(fm);
        this.context = context;
        mNavigator = navigator;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment result = null;
        switch (position) {
            case 0:
                result = UserListFragment.newInstance(context, mNavigator);
                break;
            case 1:
                result = UserOnMapFragment.newInstance(context, mNavigator);
                break;
            case 2:
                result = InviteUsersDialogFragment.newInstance(context, null);
                break;
        }
        return result;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}