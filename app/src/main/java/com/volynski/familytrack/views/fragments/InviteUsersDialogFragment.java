package com.volynski.familytrack.views.fragments;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.view.FrameMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.volynski.familytrack.BR;
import com.volynski.familytrack.R;
import com.volynski.familytrack.adapters.RecyclerViewListAdapter;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.databinding.FragmentInviteUsersBinding;
import com.volynski.familytrack.databinding.FragmentUserListBinding;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.viewmodels.InviteUsersViewModel;
import com.volynski.familytrack.viewmodels.UserListViewModel;
import com.volynski.familytrack.views.navigators.UserListNavigator;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;

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
        mCurrentUser = SharedPrefsUtil.getCurrentUser(getContext());

        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_invite_users,
                container,
                false);
        mLayoutManager = new GridLayoutManager(this.getContext(), 1);
        mBinding.recyclerViewDialoginviteusers.setLayoutManager(mLayoutManager);

        //DividerItemDecoration dividerItemDecoration =
        //        new DividerItemDecoration(mBinding.recyclerViewDialoginviteusers.getContext(),
        //                mLayoutManager.getOrientation());

        //mBinding.recyclerViewDialoginviteusers.addItemDecoration(dividerItemDecoration);

        mAdapter = new RecyclerViewListAdapter(this.getContext(), mViewModel.users,
                R.layout.invite_user_list_item, BR.viewmodel);

        mBinding.recyclerViewDialoginviteusers.setAdapter(mAdapter);
        mBinding.setViewmodel(mViewModel);

        mRootView = mBinding.getRoot();
        return mRootView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public static InviteUsersDialogFragment newInstance(Context context,
                                                        UserListNavigator navigator) {
        InviteUsersDialogFragment result = new InviteUsersDialogFragment();

        InviteUsersViewModel viewModel =
                new InviteUsersViewModel(context,
                        new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(context), context));
        viewModel.setNavigator(navigator);
        result.setViewModel(viewModel);
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getActivity().findViewById(android.R.id.content).getLayoutParams();
        mRootView.setLayoutParams(new FrameLayout.LayoutParams(800, 800));
        mViewModel.start(mCurrentUser);
    }

    public void setViewModel(InviteUsersViewModel mViewModel) {
        this.mViewModel = mViewModel;
    }

    public View getRootView() {
        return mRootView;
    }
}

