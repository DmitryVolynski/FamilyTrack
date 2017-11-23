package com.volynski.familytrack.views.fragments;

import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.GoogleApiClient;
import com.volynski.familytrack.BR;
import com.volynski.familytrack.R;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.adapters.RecyclerViewListAdapter;
import com.volynski.familytrack.databinding.FragmentFirstTimeUserBinding;
import com.volynski.familytrack.viewmodels.FirstTimeUserViewModel;
import com.volynski.familytrack.views.LoginActivity;

/**
 * Created by DmitryVolynski on 02.09.2017.
 */

public class FirstTimeUserDialogFragment
        extends DialogFragment {
    private FirstTimeUserViewModel mViewModel;
    private GridLayoutManager mLayoutManager;
    private View mRootView;
    FragmentFirstTimeUserBinding mBinding;
    private RecyclerViewListAdapter mAdapter;
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_first_time_user,
                container,
                false);

        mLayoutManager = new GridLayoutManager(this.getContext(), 1);
        mBinding.recyclerViewDialogfirsttimeuser.setLayoutManager(mLayoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(getContext(), mLayoutManager.getOrientation());

        mBinding.recyclerViewDialogfirsttimeuser.addItemDecoration(dividerItemDecoration);

        mAdapter = new RecyclerViewListAdapter(this.getContext(), mViewModel.availableGroups,
                R.layout.join_group_list_item, BR.viewmodel);
        //mAdapter.setItemClickHandler(this);
        mBinding.recyclerViewDialogfirsttimeuser.setAdapter(mAdapter);

        mBinding.setViewmodel(mViewModel);
        mRootView = mBinding.getRoot();

        return mRootView;
    }

    public void setPhoneNumber(String phoneNumber) {
        mViewModel.phoneNumber.set(phoneNumber);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(StringKeys.FIRST_TIME_USER_DIALOG_KEY, true);
    }

/*    @Override
    public void onClick(int itemId, View v) {
        mViewModel.selectGroup(itemId);
    }*/

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        //dialog.setTitle("Select mUsers to invite");
        if (savedInstanceState != null)
            if (savedInstanceState
                    .getBoolean(StringKeys.FIRST_TIME_USER_DIALOG_KEY, false)) {
                mViewModel = ((LoginActivity)getActivity()).findOrCreateViewModel();
            }
        return dialog;
    }

    public static FirstTimeUserDialogFragment newInstance() {

        FirstTimeUserDialogFragment result = new FirstTimeUserDialogFragment();

/*
        FirstTimeUserViewModel viewModel =
                new FirstTimeUserViewModel(context, signInAccount,
                        new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(context), context));
        viewModel.phoneNumber.set(phoneNumber);
        viewModel.setNavigator(navigator);
        result.setViewModel(viewModel);
*/
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.start();
    }

    public void setViewModel(FirstTimeUserViewModel mViewModel) {
        this.mViewModel = mViewModel;
    }

    public View getRootView() {
        return mRootView;
    }


}
