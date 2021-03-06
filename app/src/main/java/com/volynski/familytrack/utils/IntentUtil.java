package com.volynski.familytrack.utils;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 12.09.2017.
 */

public class IntentUtil {
    private final static String KEY_NOT_FOUND = "String with key %1$s not found";
    private final static String NULL_INTENT = "Can't extract key %1$s from null intent";

    public static String extractValueFromIntent(Intent intent, String key) {
        String result = "";
        if (intent != null) {
            if (intent.hasExtra(key)) {
                result = intent.getStringExtra(key);
            } else {
                Timber.e(String.format(KEY_NOT_FOUND, key));
            }
        } else {
            Timber.e(String.format(NULL_INTENT, key));
        }
        return result;
    }

    public static String extractValueFromBundle(Bundle bundle, String key) {
        String result = "";
        if (bundle != null) {
            if (bundle.containsKey(key)) {
                result = bundle.getString(key);
            } else {
                Timber.e(String.format(KEY_NOT_FOUND, key));
            }
        } else {
            Timber.e(String.format(NULL_INTENT, key));
        }
        return result;
    }

}
