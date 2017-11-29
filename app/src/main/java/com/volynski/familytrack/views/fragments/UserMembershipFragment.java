package com.volynski.familytrack.views.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.text.Editable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import com.volynski.familytrack.BR;
import com.volynski.familytrack.R;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.adapters.GroupsAndUsersItemType;
import com.volynski.familytrack.adapters.RecyclerViewListAdapter;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.models.MembershipListItem;
import com.volynski.familytrack.databinding.FragmentUserMembershipBinding;
import com.volynski.familytrack.dialogs.SimpleDialogFragment;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.utils.SnackbarUtil;
import com.volynski.familytrack.viewmodels.UserMembershipViewModel;
import com.volynski.familytrack.views.MainActivity;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by DmitryVolynski on 05.10.2017.
 */

public class UserMembershipFragment extends Fragment {
    private UserMembershipViewModel mViewModel;
    private GridLayoutManager mLayoutManager;

    FragmentUserMembershipBinding mBinding;
    private RecyclerViewListAdapter mAdapter;
    private Observable.OnPropertyChangedCallback mSnackbarCallback;
    private View mPopupView;
    private String mGroupName;
    private boolean mIsPopupViewVisible = false;
    private CreateGroupDialogFragment mCreateGroupDialog;

    public static UserMembershipFragment newInstance(Context context,
                                                       String currentUserUuid,
                                                       UserListNavigator navigator) {
        UserMembershipFragment result = new UserMembershipFragment();

        UserMembershipViewModel viewModel = new UserMembershipViewModel(context, currentUserUuid,
                new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(context), context));

        viewModel.setNavigator(navigator);
        result.setViewModel(viewModel);
        return result;
    }

    public void setViewModel(UserMembershipViewModel viewModel) {
        this.mViewModel = viewModel;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null &&
                savedInstanceState.containsKey(StringKeys.NEW_GROUP_DIALOG_VISIBILITY_KEY) &&
                savedInstanceState.getBoolean(StringKeys.NEW_GROUP_DIALOG_VISIBILITY_KEY)) {
            mIsPopupViewVisible = true;
            mCreateGroupDialog = (CreateGroupDialogFragment) getActivity()
                    .getSupportFragmentManager()
                    .findFragmentByTag(CreateGroupDialogFragment.class.getSimpleName());
            mCreateGroupDialog.setButtonClickListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(R.string.toolbar_title_membership);
        mViewModel.start();
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_user_membership,
                container,
                false);

        setupCustomListeners();
        setupSnackbar();

        int nCols = getResources().getInteger(R.integer.membership_list_ncols);
        mLayoutManager = new GridLayoutManager(this.getContext(), nCols);
        //mLayoutManager.setMeasuredDimension();
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (mAdapter.getItemViewType(position)) {
                    case MembershipListItem.TYPE_GROUP:
                        return mLayoutManager.getSpanCount();
                    case MembershipListItem.TYPE_USER:
                        return 1;
                    default:
                        return -1;
                }
            }
        });

        mBinding.recyclerviewFragmentusermembership.setLayoutManager(mLayoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mBinding.recyclerviewFragmentusermembership.getContext(),
                        mLayoutManager.getOrientation());

        mBinding.recyclerviewFragmentusermembership.addItemDecoration(dividerItemDecoration);
        mAdapter = new RecyclerViewListAdapter(this.getContext(), mViewModel.viewModels,
                new GroupsAndUsersItemType(), BR.viewmodel);

        mBinding.recyclerviewFragmentusermembership.setAdapter(mAdapter);
        mBinding.setViewmodel(mViewModel);

        return mBinding.getRoot();
    }

    private void setupCustomListeners() {
        mViewModel.showLeaveGroupWarningDialog
                .addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                showLeaveGroupWarningDialog();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(StringKeys.NEW_GROUP_DIALOG_VISIBILITY_KEY, mIsPopupViewVisible);
    }

    private void showLeaveGroupWarningDialog() {
        final SimpleDialogFragment confirmDialog = new SimpleDialogFragment();
        confirmDialog.setParms(getString(R.string.leave_group_dialog_title),
                getString(R.string.leave_group_dialog_message),
                getString(R.string.label_button_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        confirmDialog.dismiss();
                    }
                });
        confirmDialog.show(getActivity().getFragmentManager(), getString(R.string.dialog_tag));
    }

    public void createNewGroupDialog(String groupName) {
            mIsPopupViewVisible = true;
            mCreateGroupDialog = CreateGroupDialogFragment.getInstance(this);
            mCreateGroupDialog.show(getActivity().getSupportFragmentManager(),
                    CreateGroupDialogFragment.class.getSimpleName());

    }

    public void createNewGroupCommand(String text) {
        mViewModel.createNewGroup(text);
    }

    public void createNewGroupCanceled() {
        mIsPopupViewVisible = false;
    }
}


