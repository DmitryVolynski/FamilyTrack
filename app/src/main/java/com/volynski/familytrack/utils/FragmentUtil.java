package com.volynski.familytrack.utils;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import java.util.List;

/**
 * Created by DmitryVolynski on 04.10.2017.
 */

public class FragmentUtil {
    public static Fragment findFragmentByClassName(FragmentActivity activity, String fragmentClassName) {
        Fragment result = null;
        List<Fragment> fragments = activity.getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment.getClass().getSimpleName().equals(fragmentClassName)) {
                result = fragment;
                break;
            }
        }
        return result;
    }

}
