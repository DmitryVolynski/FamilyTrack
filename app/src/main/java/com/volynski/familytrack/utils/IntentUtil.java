package com.volynski.familytrack.utils;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 12.09.2017.
 */

public class IntentUtil {
    public static String extractValueFromIntent(Intent intent, String key) {
        String result = "";
        if (intent != null) {
            if (intent.hasExtra(key)) {
                result = intent.getStringExtra(key);
            } else {
                Timber.e("String with key=" + key + " not found in intent");
            }
        } else {
            Timber.e("Can't extract key=" + key + " from null intent");
        }
        return result;
    }

    public static String extractValueFromBundle(Bundle bundle, String key) {
        String result = "";
        if (bundle != null) {
            if (bundle.containsKey(key)) {
                result = bundle.getString(key);
            } else {
                Timber.e("String with key=" + key + " not found in intent");
            }
        } else {
            Timber.e("Can't extract key=" + key + " from null bundle");
        }
        return result;
    }

}
