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
    private String mCurrentUserUuid;
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

/*    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mInviteUsersDialog != null &&
                mInviteUsersDialog.getDialog() != null &&
                mInviteUsersDialog.getDialog().isShowing()) {
            // if invite dialog is visible - save the state of dialog & data
            mInviteUsersDialog.onSaveInstanceState(outState);
        }
    }*/

    public void setViewModel(UserMembershipViewModel viewModel) {
        this.mViewModel = viewModel;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null &&
                savedInstanceState.containsKey(StringKeys.NEW_GROUP_NAME_KEY)) {
            mGroupName = savedInstanceState.getString(StringKeys.NEW_GROUP_NAME_KEY);
            mIsPopupViewVisible = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.start();
        if (mIsPopupViewVisible) {
            createNewGroupDialog(mGroupName);
        }
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
        mCurrentUserUuid = SharedPrefsUtil.getCurrentUserUuid(getContext());

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

        //mAdapter.enablePopupMenu(R.menu.user_popup_menu, R.id.imageview_userslistitem_popupsymbol);

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
        if (mIsPopupViewVisible) {
            EditText et = (EditText)mPopupView
                    .findViewById(R.id.edittext_dialogcreategroup_groupname);
            outState.putString(StringKeys.NEW_GROUP_NAME_KEY, et.getText().toString());
        }
    }

    private void showLeaveGroupWarningDialog() {
        final SimpleDialogFragment confirmDialog = new SimpleDialogFragment();
        confirmDialog.setParms("Leaving group",
                "Please note that you are the one and only admin in group.\n\n" +
                "Delegate admin role to somebody else before leaving this group",
                "Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        confirmDialog.dismiss();
                    }
                });
        confirmDialog.show(getActivity().getFragmentManager(), "dialog");
    }

    public void createNewGroupDialog(String groupName) {
        // get a reference to the already created main layout

/*
        mCreateGroupDialog = (CreateGroupDialogFragment) getActivity()
                .getSupportFragmentManager()
                .findFragmentByTag(CreateGroupDialogFragment.class.getSimpleName());
        if (mCreateGroupDialog == null) {
*/
            mCreateGroupDialog = new CreateGroupDialogFragment();
            mCreateGroupDialog.show(getActivity().getSupportFragmentManager(),
                    CreateGroupDialogFragment.class.getSimpleName());

/*
        FrameLayout mainLayout = mBinding.framelayoutFragmentusermembership;

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        mPopupView = inflater.inflate(R.layout.dialog_create_group, null);

        // create the popup window
        int width =  (int) Math.round(0.8 * mainLayout.getWidth());  //LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = (int) Math.round(0.8 * mainLayout.getHeight());  //LinearLayout.LayoutParams.WRAP_CONTENT;

        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(mPopupView, width, height, focusable);

        mIsPopupViewVisible = true;

        if (groupName != null) {
            EditText et = (EditText)mPopupView
                    .findViewById(R.id.edittext_dialogcreategroup_groupname);
            et.setText(groupName);
        }

        mPopupView.findViewById(R.id.button_dialogcreategroup_cancel)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                        mIsPopupViewVisible = false;
                    }
                });

        mPopupView.findViewById(R.id.button_dialogcreategroup_create)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText editText = (EditText) mPopupView
                                .findViewById(R.id.edittext_dialogcreategroup_groupname);
                        mViewModel.createNewGroup(editText.getText().toString());
                        popupWindow.dismiss();
                        mIsPopupViewVisible = false;
                    }
                });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mIsPopupViewVisible = false;
            }

        });
        new Handler().postDelayed(new Runnable(){
            public void run() {
                // show the popup window
                popupWindow.showAtLocation(mPopupView, Gravity.CENTER, 0, 0);
            }

        }, 100L);
*/
    }
}


