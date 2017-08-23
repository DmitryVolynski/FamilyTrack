package com.volynski.familytrack.data;

/**
 * Created by DmitryVolynski on 21.08.2017.
 */

public class FirebaseResult<T> {
    private T mData;
    private int mResultCode;
    private Exception mException;

    public T getData() {
        return mData;
    }

    public void setData(T mResult) {
        this.mData = mResult;
    }

    public Exception getException() {
        return mException;
    }

    public void setException(Exception mException) {
        this.mException = mException;
    }

    public int getResultCode() {
        return mResultCode;
    }

    public void setResultCode(int mResultCode) {
        this.mResultCode = mResultCode;
    }

    public FirebaseResult(T data) {
        this.mData = data;
        this.mResultCode = 0;
        this.mException = null;
    }

    public FirebaseResult(int resultCode, Exception e) {
        this.mData = null;
        this.mResultCode = resultCode;
        this.mException = e;
    }
}
