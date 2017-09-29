package com.volynski.familytrack.utils;

/**
 * Created by DmitryVolynski on 29.09.2017.
 */

import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Provides a method to show a Snackbar.
 */
public class SnackbarUtil {

    public static void showSnackbar(View v, String snackbarText) {
        if (v == null || snackbarText == null) {
            return;
        }
        Snackbar.make(v, snackbarText, Snackbar.LENGTH_LONG).show();
    }
}
