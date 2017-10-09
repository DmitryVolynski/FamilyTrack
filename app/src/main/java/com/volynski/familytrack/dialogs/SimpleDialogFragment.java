package com.volynski.familytrack.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.volynski.familytrack.R;

/**
 * Created by DmitryVolynski on 29.09.2017.
 */

public class SimpleDialogFragment extends DialogFragment {
    private String mTitle;
    private String mMessage;
    private String mPositiveButtonTitle;
    private String mNegativeButtonTitle;
    private DialogInterface.OnClickListener mPbClickListener;
    private DialogInterface.OnClickListener mNbClickListener;


    public void setParms(String title, String message, String positiveButtonTitle,
                         DialogInterface.OnClickListener pbClickListener) {
        mTitle = title;
        mMessage = message;
        mPositiveButtonTitle = positiveButtonTitle;
        mNegativeButtonTitle = null;
        mPbClickListener = pbClickListener;
        mNbClickListener = null;
    }

    public void setParms(String title, String message, String positiveButtonTitle, String negativeButtonTitle,
                         DialogInterface.OnClickListener pbClickListener, DialogInterface.OnClickListener nbClickListener) {
        mTitle = title;
        mMessage = message;
        mPositiveButtonTitle = positiveButtonTitle;
        mNegativeButtonTitle = negativeButtonTitle;
        mPbClickListener = pbClickListener;
        mNbClickListener = nbClickListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (mPositiveButtonTitle != null && mNegativeButtonTitle != null) {
            builder.setMessage(mMessage)
                    .setTitle(mTitle)
                    .setPositiveButton(mPositiveButtonTitle, mPbClickListener)
                    .setNegativeButton(mNegativeButtonTitle, mNbClickListener);
        } else {
            builder.setMessage(mMessage)
                    .setTitle(mTitle)
                    .setPositiveButton(mPositiveButtonTitle, mPbClickListener);
        }
        return builder.create();
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String mMessage) {
        this.mMessage = mMessage;
    }

    public String getPositiveButtonTitle() {
        return mPositiveButtonTitle;
    }

    public void setPositiveButtonTitle(String mPositiveButtonTitle) {
        this.mPositiveButtonTitle = mPositiveButtonTitle;
    }

    public String getNegativeButtonTitle() {
        return mNegativeButtonTitle;
    }

    public void setNegativeButtonTitle(String mNegativeButtonTitle) {
        this.mNegativeButtonTitle = mNegativeButtonTitle;
    }
}