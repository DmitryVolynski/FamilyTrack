package com.volynski.familytrack.views.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.volynski.familytrack.BR;
import com.volynski.familytrack.R;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.adapters.RecyclerViewListAdapter;
import com.volynski.familytrack.databinding.FragmentUserListBinding;
import com.volynski.familytrack.utils.SnackbarUtil;
import com.volynski.familytrack.viewmodels.InviteUsersViewModel;
import com.volynski.familytrack.viewmodels.UserListItemViewModel;
import com.volynski.familytrack.viewmodels.UserListViewModel;
import com.volynski.familytrack.views.MainActivity;
import com.volynski.familytrack.views.FragmentsUtil;

import java.util.List;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 22.08.2017.
 */

public class UserListFragment
        extends Fragment {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    private UserListViewModel mViewModel;
    private String mCurrentUserUuid;
    //private UserListNavigator mUserListNavigator;
    private GridLayoutManager mLayoutManager;

    FragmentUserListBinding mBinding;
    private RecyclerViewListAdapter mAdapter;
    private InviteUsersDialogFragment mInviteUsersDialog;
    private Observable.OnPropertyChangedCallback mSnackbarCallback;

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
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(R.string.toolbar_title_user_list);
        if (getArguments() == null) {
            Timber.e("No arguments found. Expected " + StringKeys.CURRENT_USER_UUID_KEY);
            return;
        }
        mCurrentUserUuid = getArguments().getString(StringKeys.CURRENT_USER_UUID_KEY);
        mViewModel.start(mCurrentUserUuid);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null &&
                savedInstanceState.containsKey(StringKeys.INVITE_USERS_DIALOG_SHOW_KEY)) {
            showInviteUsersDialog(savedInstanceState);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = setupFragmentContent(inflater, container, savedInstanceState);
        setupSnackbar();
        return view;
    }

    private View setupFragmentContent(LayoutInflater inflater,
                                      ViewGroup container,
                                      Bundle savedInstanceState) {
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

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String userUuid = ((List<UserListItemViewModel>)mAdapter.getViewModels())
                        .get(viewHolder.getAdapterPosition()).getUser().getUserUuid();
                mViewModel.excludeUser(userUuid);
            }
        }).attachToRecyclerView(mBinding.recyclerviewFragmentuserslistUserslist);

        mBinding.setViewmodel(mViewModel);

        return mBinding.getRoot();
    }


    @Override
    public void onDestroy() {
        if (mSnackbarCallback != null) {
            mViewModel.snackbarText.removeOnPropertyChangedCallback(mSnackbarCallback);
        }
        super.onDestroy();
    }

    private void setupSnackbar() {
        mSnackbarCallback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                SnackbarUtil.showSnackbar(((MainActivity)getActivity()).getViewForSnackbar(),
                        mViewModel.snackbarText.get());
            }
        };
        mViewModel.snackbarText.addOnPropertyChangedCallback(mSnackbarCallback);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mInviteUsersDialog != null &&
                mInviteUsersDialog.getDialog() != null &&
                mInviteUsersDialog.getDialog().isShowing()) {
            // if invite dialog is visible - save the state of dialog & data
            mInviteUsersDialog.onSaveInstanceState(outState);
        }
    }

    public void setViewModel(UserListViewModel mViewModel) {
        this.mViewModel = mViewModel;
    }

    public void showInviteUsersDialog(Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        }

        InviteUsersViewModel viewModel =
                (InviteUsersViewModel) FragmentsUtil.findOrCreateViewModel(
                        (AppCompatActivity)getActivity(),
                        MainActivity.CONTENT_INVITE_USERS,
                        mCurrentUserUuid,
                        mViewModel.getNavigator());

        //viewModel.restoreFromBundle(savedInstanceState);

        mInviteUsersDialog = (InviteUsersDialogFragment) getActivity()
                .getSupportFragmentManager()
                .findFragmentByTag(InviteUsersDialogFragment.class.getSimpleName());
        if (mInviteUsersDialog == null) {
            mInviteUsersDialog = InviteUsersDialogFragment.newInstance(mCurrentUserUuid);
            mInviteUsersDialog.show(getActivity().getSupportFragmentManager(),
                    InviteUsersDialogFragment.class.getSimpleName());
            mInviteUsersDialog.setViewModel(viewModel);
        } else {
            mInviteUsersDialog.setViewModel(viewModel);
        }
        //mInviteUsersDialog.show();

/*
        InviteUsersViewModel viewModel = new InviteUsersViewModel(
                getContext(),
                mCurrentUserUuid,
                new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(getContext()), getContext()),
                mViewModel.getNavigator());
*/

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

/*
    public UserListNavigator getNavigator() {
        return mUserListNavigator;
    }

    public void setNavigator(UserListNavigator userListNavigator) {
        this.mUserListNavigator = userListNavigator;
    }
*/
}
