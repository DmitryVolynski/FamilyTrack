package com.volynski.familytrack.data;

import com.google.firebase.database.DatabaseError;

/**
 * Created by DmitryVolynski on 21.08.2017.
 *
 * Universal class to return result from db
 *      If operation succeed getData will return the result of T type
 *      If operation failed getResultCode returns an error code and getException return Exception
 *
 */

public class FirebaseResult<T> {
    private T mData;
    private FamilyTrackException mException;

    public T getData() {
        return mData;
    }

    public void setData(T mResult) {
        this.mData = mResult;
    }

    public FamilyTrackException getException() {
        return mException;
    }

    public void setException(FamilyTrackException mException) {
        this.mException = mException;
    }

    public FirebaseResult(T data) {
        this.mData = data;
        this.mException = null;
    }

    public FirebaseResult(FamilyTrackException e) {
        this.mData = null;
        this.mException = e;
    }

    public FirebaseResult(DatabaseError error) {
        this.mData = null;
        this.mException = new FamilyTrackException(error.getCode(), error.getMessage());
    }
}
