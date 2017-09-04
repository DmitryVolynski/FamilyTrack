package com.volynski.familytrack.views.fragments;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.volynski.familytrack.R;
import com.volynski.familytrack.adapters.RecyclerViewListAdapter;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.databinding.FragmentFirstTimeUserBinding;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.viewmodels.FirstTimeUserViewModel;
import com.volynski.familytrack.views.navigators.LoginNavigator;

/**
 * Created by DmitryVolynski on 02.09.2017.
 */

public class FirstTimeUserDialogFragment extends DialogFragment {
    private FirstTimeUserViewModel mViewModel;
    private GridLayoutManager mLayoutManager;
    private View mRootView;
    FragmentFirstTimeUserBinding mBinding;
    private RecyclerViewListAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_first_time_user,
                container,
                false);
        /*
        mLayoutManager = new GridLayoutManager(this.getContext(), 1);
        mBinding.recyclerViewDialoginviteusers.setLayoutManager(mLayoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(getContext(), mLayoutManager.getOrientation());

        mBinding.recyclerViewDialoginviteusers.addItemDecoration(dividerItemDecoration);

        mAdapter = new RecyclerViewListAdapter(this.getContext(), mViewModel.viewModels,
                R.layout.invite_user_list_item, BR.viewmodel);

        mBinding.recyclerViewDialoginviteusers.setAdapter(mAdapter);
        */
        mBinding.setViewmodel(mViewModel);
        mRootView = mBinding.getRoot();
        return mRootView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Select users to invite");
        //dialog.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        return dialog;
    }

    public static FirstTimeUserDialogFragment newInstance(Context context,
                                                          GoogleSignInAccount signInAccount,
                                                          LoginNavigator navigator) {

        FirstTimeUserDialogFragment result = new FirstTimeUserDialogFragment();

        FirstTimeUserViewModel viewModel =
                new FirstTimeUserViewModel(context, signInAccount,
                        new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(context), context));

        viewModel.setNavigator(navigator);
        result.setViewModel(viewModel);
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
