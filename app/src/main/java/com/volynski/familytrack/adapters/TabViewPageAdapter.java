package com.volynski.familytrack.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.volynski.familytrack.views.fragments.UsersListFragment;
import com.volynski.familytrack.views.fragments.UsersOnMapFragment;

/**
 * Created by DmitryVolynski on 22.08.2017.
 */

public class TabViewPageAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] { "Users", "Map" };
    private Context context;

    public TabViewPageAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
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
                result = UsersListFragment.newInstance();
                break;
            case 1:
                result = UsersOnMapFragment.newInstance();
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