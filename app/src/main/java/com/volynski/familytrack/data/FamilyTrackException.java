package com.volynski.familytrack.data;

/**
 * Created by DmitryVolynski on 25.08.2017.
 */

public class FamilyTrackException extends Exception {
    public static final int FT_ERROR_USER_ALREADY_EXISTS = -1000;

    private int mErrorCode;
    private Exception mInnerException;
    private String mErrorMessage;
}
