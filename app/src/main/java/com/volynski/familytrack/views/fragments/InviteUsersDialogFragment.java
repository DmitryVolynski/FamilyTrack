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

import com.volynski.familytrack.BR;
import com.volynski.familytrack.R;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.adapters.RecyclerViewListAdapter;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.databinding.FragmentInviteUsersBinding;
import com.volynski.familytrack.viewmodels.InviteUsersViewModel;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 31.08.2017.
 */

public class InviteUsersDialogFragment extends DialogFragment {
    private InviteUsersViewModel mViewModel;
    private User mCurrentUser;
    private GridLayoutManager mLayoutManager;
    private View mRootView;
    FragmentInviteUsersBinding mBinding;
    private RecyclerViewListAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_invite_users,
                container,
                false);
        mLayoutManager = new GridLayoutManager(this.getContext(), 1);
        mBinding.recyclerViewDialoginviteusers.setLayoutManager(mLayoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(getContext(), mLayoutManager.getOrientation());

        mBinding.recyclerViewDialoginviteusers.addItemDecoration(dividerItemDecoration);

        mAdapter = new RecyclerViewListAdapter(this.getContext(), mViewModel.viewModels,
                R.layout.invite_user_list_item, BR.viewmodel);

        mBinding.recyclerViewDialoginviteusers.setAdapter(mAdapter);
        mBinding.setViewmodel(mViewModel);

        mRootView = mBinding.getRoot();
        return mRootView;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null &&
                savedInstanceState.containsKey(StringKeys.INVITE_USERS_LAYOUT_POSITION_KEY)) {
            mLayoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(StringKeys.INVITE_USERS_LAYOUT_POSITION_KEY));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(StringKeys.INVITE_USERS_DIALOG_SHOW_KEY, true);
        outState.putParcelable(StringKeys.INVITE_USERS_LAYOUT_POSITION_KEY, mLayoutManager.onSaveInstanceState());
        outState.putBundle(StringKeys.INVITE_USERS_VIEWMODEL_BUNDLE_KEY, mViewModel.saveToBundle());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Select users to invite");
        //dialog.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        return dialog;
    }

    public static InviteUsersDialogFragment newInstance(String currentUserUuid) {
        Bundle args = new Bundle();
        args.putString(StringKeys.CURRENT_USER_UUID_KEY, currentUserUuid);

        InviteUsersDialogFragment result = new InviteUsersDialogFragment();
        result.setArguments(args);
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getArguments() == null) {
            Timber.e("No arguments found. Expected " + StringKeys.CURRENT_USER_UUID_KEY);
            return;
        }
        if (!mViewModel.isCreatedFromViewHolder()) {
            mViewModel.start(getArguments().getString(StringKeys.CURRENT_USER_UUID_KEY));
        }
    }

    public void setViewModel(InviteUsersViewModel mViewModel) {
        this.mViewModel = mViewModel;
    }

    public View getRootView() {
        return mRootView;
    }
}

