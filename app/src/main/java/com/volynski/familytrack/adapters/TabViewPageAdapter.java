package com.volynski.familytrack.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.volynski.familytrack.views.fragments.UserHistoryChartFragment;
import com.volynski.familytrack.views.fragments.UserListFragment;
import com.volynski.familytrack.views.fragments.UserOnMapFragment;
import com.volynski.familytrack.views.navigators.UserListNavigator;

/**
 * Created by DmitryVolynski on 22.08.2017.
 */

public class TabViewPageAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 3;
    private String tabTitles[] = new String[] { "Users", "Map", "History" };
    private Context context;
    private UserListNavigator mNavigator;
    private String mCurrentUserUuid;

    public TabViewPageAdapter(String currentUserUuid, FragmentManager fm,
                              Context context, UserListNavigator navigator) {
        super(fm);
        this.context = context;
        mNavigator = navigator;
        mCurrentUserUuid = currentUserUuid;
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
                result = UserListFragment.newInstance(context, mCurrentUserUuid, mNavigator);
                break;
            case 1:
                result = UserOnMapFragment.newInstance(context, mCurrentUserUuid, mNavigator);
                break;
            case 2:
                result = UserHistoryChartFragment.newInstance(context, mCurrentUserUuid, mNavigator);
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