package com.volynski.familytrack.views.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.volynski.familytrack.R;
import com.volynski.familytrack.dialogs.SimpleDialogFragment;

/**
 * Created by DmitryVolynski on 11.11.2017.
 */

public class CreateGroupDialogFragment extends DialogFragment {
    private EditText mGroupName;
    private UserMembershipFragment mButtonClickListener;

    @Override
    public void onResume() {
        super.onResume();
        int width = getResources().getDimensionPixelSize(R.dimen.create_group_dialog_width);
        int height = getResources().getDimensionPixelSize(R.dimen.create_group_dialog_height);
        getDialog().getWindow().setLayout(width, height);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_create_group, null))
                // Add action buttons
                .setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mGroupName = (EditText)CreateGroupDialogFragment.this.getDialog().findViewById(R.id.edittext_dialogcreategroup_groupname);
                        if (mGroupName.getText().toString().equals("")) {
                            showPopupDialog("Create group", "Please specify a name for new group");
                        } else {
                            mButtonClickListener.createNewGroupCommand(mGroupName.getText().toString());
                        }
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CreateGroupDialogFragment.this.getDialog().cancel();
                        mButtonClickListener.createNewGroupCanceled();
                    }
                });
        Dialog dialog = builder.create();
        return dialog;
    }


    public static CreateGroupDialogFragment getInstance(UserMembershipFragment userMembershipFragment) {
        CreateGroupDialogFragment dialog = new CreateGroupDialogFragment();
        dialog.setButtonClickListener(userMembershipFragment);
        return dialog;
    }

    public void setButtonClickListener(UserMembershipFragment buttonClickListener) {
        this.mButtonClickListener = buttonClickListener;
    }

    private void showPopupDialog(String title, String message) {
        final SimpleDialogFragment confirmDialog = new SimpleDialogFragment();
        confirmDialog.setParms(title, message, "Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        confirmDialog.dismiss();
                    }
                });
        confirmDialog.show(getActivity().getFragmentManager(), "dialog");
    }
}
