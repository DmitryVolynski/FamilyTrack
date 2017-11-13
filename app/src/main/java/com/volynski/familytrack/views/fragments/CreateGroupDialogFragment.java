package com.volynski.familytrack.views.fragments;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import com.volynski.familytrack.R;

/**
 * Created by DmitryVolynski on 11.11.2017.
 */

public class CreateGroupDialogFragment extends DialogFragment {
    private EditText mGroupName;

    @Override
    public void onResume() {
        super.onResume();
        int width = getResources().getDimensionPixelSize(R.dimen.create_group_dialog_width);
        int height = getResources().getDimensionPixelSize(R.dimen.create_group_dialog_height);
        getDialog().getWindow().setLayout(width, height);    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_create_group, container);
        mGroupName = (EditText) view.findViewById(R.id.edittext_dialogcreategroup_groupname);
        return view;
    }
}
