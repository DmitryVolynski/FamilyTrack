package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;

/**
 * Created by DmitryVolynski on 22.08.2017.
 */

public class UsersOnMapViewModel extends BaseObservable {
    private final Context mContext;

    public UsersOnMapViewModel(Context context) {
        mContext = context.getApplicationContext();
    }
}
