package com.volynski.familytrack.views.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.volynski.familytrack.BR;
import com.volynski.familytrack.R;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.adapters.RecyclerViewListAdapter;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.databinding.FragmentUserListBinding;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.viewmodels.UserListViewModel;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import timber.log.Timber;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by DmitryVolynski on 22.08.2017.
 */

public class UserListFragment
        extends Fragment {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    private UserListViewModel mViewModel;
    private String mCurrentUserUuid;
    private UserListNavigator mUserListNavigator;
    private GridLayoutManager mLayoutManager;

    FragmentUserListBinding mBinding;
    private RecyclerViewListAdapter mAdapter;
    private InviteUsersDialogFragment mInviteUsersDialog;

    public static UserListFragment newInstance(String currentUserUuid) {
        Bundle args = new Bundle();
        args.putString(StringKeys.CURRENT_USER_UUID_KEY, currentUserUuid);

        UserListFragment result = new UserListFragment();
        result.setArguments(args);

        return result;
    }

/*
    public static UserListFragment newInstance(String currentUserUuid,
                                               UserListViewModel viewModel) {
        UserListFragment fragment = newInstance(currentUserUuid);
        fragment.setViewModel(viewModel);
        return fragment;
    }
*/

    @Override
    public void onResume() {
        super.onResume();
        if (getArguments() == null) {
            Timber.e("No arguments found. Expected " + StringKeys.CURRENT_USER_UUID_KEY);
            return;
        }
        mViewModel.start(getArguments().getString(StringKeys.CURRENT_USER_UUID_KEY));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_user_list,
                container,
                false);
        mLayoutManager = new GridLayoutManager(this.getContext(), 1);
        mBinding.recyclerviewFragmentuserslistUserslist.setLayoutManager(mLayoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mBinding.recyclerviewFragmentuserslistUserslist.getContext(),
                        mLayoutManager.getOrientation());

        mBinding.recyclerviewFragmentuserslistUserslist.addItemDecoration(dividerItemDecoration);

        mAdapter = new RecyclerViewListAdapter(this.getContext(), mViewModel.viewModels,
                R.layout.user_list_item, BR.viewmodel);
        mAdapter.enablePopupMenu(R.menu.user_popup_menu, R.id.imageview_userslistitem_popupsymbol);
        mBinding.recyclerviewFragmentuserslistUserslist.setAdapter(mAdapter);
        mBinding.setViewmodel(mViewModel);

        return mBinding.getRoot();
    }

    public void setViewModel(UserListViewModel mViewModel) {
        this.mViewModel = mViewModel;
    }

    public void inviteUser() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        }

        mInviteUsersDialog = InviteUsersDialogFragment.newInstance(getContext(),
                mCurrentUserUuid, mUserListNavigator);
        mInviteUsersDialog.show(getActivity().getSupportFragmentManager(), "aa");
    }

    public void dismissInviteUsersDialog() {
        if (mInviteUsersDialog != null) {
            mInviteUsersDialog.dismiss();
        }
    }

    public void refreshList() {
        mViewModel.start(getArguments().getString(StringKeys.CURRENT_USER_UUID_KEY));
    }

    public void setCurrentUserUuid(String currentUserUuid) {
        this.mCurrentUserUuid = currentUserUuid;
    }

    public UserListNavigator getNavigator() {
        return mUserListNavigator;
    }

    public void setNavigator(UserListNavigator userListNavigator) {
        this.mUserListNavigator = userListNavigator;
    }
}
