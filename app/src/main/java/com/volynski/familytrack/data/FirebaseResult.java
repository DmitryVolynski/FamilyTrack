package com.volynski.familytrack.data;

/**
 * Created by DmitryVolynski on 21.08.2017.
 */

public class FirebaseResult<T> {
    private T mResult;
    private int mResultCode;
    private Exception mException;

    public T getResult() {
        return mResult;
    }

    public void setResult(T mResult) {
        this.mResult = mResult;
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

    public FirebaseResult(T result) {
        this.mResult = result;
        this.mResultCode = 0;
        this.mException = null;
    }

    public FirebaseResult(int resultCode, Exception e) {
        this.mResult = null;
        this.mResultCode = resultCode;
        this.mException = e;
    }
}
