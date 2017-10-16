package com.volynski.familytrack.utils;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by DmitryVolynski on 16.10.2017.
 */

public class ResourceUtil {
    public static TypedValue getTypedValue(Context context, int resourceId) {
        TypedValue result = new TypedValue();
        context.getResources().getValue(resourceId, result, true);
        return result;
    }
}
