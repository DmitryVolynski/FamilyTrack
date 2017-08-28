package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;

/**
 * Created by DmitryVolynski on 22.08.2017.
 */

public class UserOnMapViewModel extends BaseObservable {
    private final Context mContext;

    public UserOnMapViewModel(Context context) {
        mContext = context.getApplicationContext();
    }
}
