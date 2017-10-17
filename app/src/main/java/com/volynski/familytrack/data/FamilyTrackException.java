package com.volynski.familytrack.data;

import android.content.Context;

/**
 * Created by DmitryVolynski on 25.08.2017.
 */

public class FamilyTrackException extends Exception {
    //public static final int FT_ERROR_USER_ALREADY_EXISTS = -1000;
    public static final int DB_GROUP_NOT_FOUND = -1001;
    public static final int DB_USER_BY_UUID_NOT_FOUND = -1002;
    public static final int DB_REMOVE_USER_FAILED = -1003;

    private int mErrorCode;
    private Exception mInnerException;
    private String mErrorMessage;

    public FamilyTrackException(int code, String message) {
        mErrorCode = code;
        mErrorMessage = message;
        mInnerException = null;
    }

    public FamilyTrackException(int code, String message, Exception innerException) {
        mErrorCode = code;
        mErrorMessage = message;
        mInnerException = innerException;
    }

    public static FamilyTrackException getInstance(Context context, int errorCode) {
            String packageName = context.getPackageName();
            int resId = context.getResources().getIdentifier("E" + Math.abs(errorCode), "string", packageName);
            return new FamilyTrackException(errorCode, context.getResources().getString(resId));
    }
}
