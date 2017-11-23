package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Observable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 31.08.2017.
 */


public class InviteUsersViewModel
        extends AbstractViewModel
        implements View.OnClickListener {

    public final static String UI_CONTEXT = InviteUsersViewModel.class.getSimpleName();
    private final static String TAG = UserListViewModel.class.getSimpleName();
    private UserListNavigator mNavigator;

    private List<User> mUnfilteredUserList = new ArrayList<>();

    public final ObservableBoolean showDialog = new ObservableBoolean(false);
    public final ObservableList<UserListItemViewModel> viewModels =
            new ObservableArrayList<>();
    public final ObservableField<String> searchString = new ObservableField<>("");

    public InviteUsersViewModel(Context context,
                                String currentUserUuid,
                                FamilyTrackDataSource dataSource,
                                UserListNavigator navigator) {
        super(context, currentUserUuid, dataSource);
        mNavigator = navigator;

        searchString.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                filterUserList();
            }
        });
    }

    private void filterUserList() {
        // store selected users
        ArrayList<String> selectedUsers = new ArrayList<>();
        for (int i=0; i < viewModels.size(); i++) {
            if (viewModels.get(i).checked.get()) {
                selectedUsers.add(viewModels.get(i).getUser().getUserUuid());
            }
        }

        viewModels.clear();
        for (User user : mUnfilteredUserList) {
            if (user.getDisplayName().contains(searchString.get()) ||
                    user.getFamilyName().contains(searchString.get()) ||
                    user.getGivenName().contains(searchString.get()) ||
                    user.getEmail().contains(searchString.get()) ||
                    user.getPhone().contains(searchString.get())) {
                UserListItemViewModel itemViewModel =
                        new UserListItemViewModel(mContext, user, null, UI_CONTEXT);
                if (selectedUsers.contains(user.getUserUuid())) {
                    itemViewModel.checked.set(true);
                }
                viewModels.add(itemViewModel);
            }
        }
        notifyChange();
    }

    public void doInvite() {
        Timber.v("Do invite command");
        mRepository.inviteUsers(mCurrentUser.getActiveMembership().getGroupUuid(),
                getUsersFromViewModels(viewModels), new FamilyTrackDataSource.InviteUsersCallback() {
                    @Override
                    public void onInviteUsersCompleted(FirebaseResult<String> result) {
                        Timber.v("Invite done");
                        mNavigator.inviteCompleted();
                        loadUsersList();
                    }
                });
    }

    private List<User> getUsersFromViewModels(ObservableList<UserListItemViewModel> viewModels) {
        List<User> result = new ArrayList<>();
        for (UserListItemViewModel viewModel : viewModels) {
            if (viewModel.checked.get()) {
                result.add(viewModel.getUser());
            }
        }
        return result;
    }

    public void cancelInvite() {
        mNavigator.dismissInviteUsersDialog();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
        refreshList();
    }

    private void refreshList() {
        Log.v(TAG, "refreshList started");
    }


    /**
     * Starts loading data from contact list of the phone
     */
    public void start(String currentUserUuid) {
        mCurrentUserUuid = currentUserUuid;
        if (mCurrentUserUuid.equals("")) {
            Timber.e("Can't start viewmodel. UserUuid is empty");
            return;
        }

        isDataLoading.set(true);
        mRepository.getUserByUuid(mCurrentUserUuid, new FamilyTrackDataSource.GetUserByUuidCallback() {
            @Override
            public void onGetUserByUuidCompleted(FirebaseResult<User> result) {
                if (result.getData() != null) {
                    mCurrentUser = result.getData();
                    loadUsersList();
                } else {
                    Timber.v("User with uuid=" + mCurrentUserUuid + " not found ");
                }
            }
        });
    }

    private void loadUsersList() {
        if (mCurrentUser != null && mCurrentUser.getActiveMembership() != null) {
            String groupUuid = mCurrentUser.getActiveMembership().getGroupUuid();
            mRepository.getContactsToInvite(groupUuid,
                    new FamilyTrackDataSource.GetContactsToInviteCallback() {
                @Override
                public void onGetContactsToInviteCompleted(FirebaseResult<List<User>> result) {
                    populateUserListFromDbResult(result);
                    isDataLoading.set(false);
                }
            });
        } else {
            Timber.v("mCurrentUser == null || mCurrentUser.getActiveMembership() == null");
        }
    }

    /**
     *
     * @param result
     */
    private void populateUserListFromDbResult(FirebaseResult<List<User>> result) {
        if (result.getData() != null) {
            viewModels.clear();
            mUnfilteredUserList.clear();
            for (User user : result.getData()) {
                viewModels.add(new UserListItemViewModel(mContext, user, null, UI_CONTEXT));
                mUnfilteredUserList.add(user);
            }
        }
        //notifyChange();
    }

    public void setNavigator(UserListNavigator mNavigator) {
        this.mNavigator = mNavigator;
        if (viewModels != null) {
            for (UserListItemViewModel listItemViewModel : viewModels) {
                listItemViewModel.setNavigator(mNavigator);
            }
        }
    }

    // save valuable parms to use them after configuration change
    public Bundle saveToBundle() {
        Bundle bundle = new Bundle();

        ArrayList<String> selectedUsers = new ArrayList<>();
        for (int i=0; i < viewModels.size(); i++) {
            if (viewModels.get(i).checked.get()) {
                selectedUsers.add(viewModels.get(i).getUser().getUserUuid());
            }
        }
        bundle.putString(StringKeys.INVITE_USERS_VM_SEARCH_STRING_KEY, searchString.get());

        String[] selUsers = new String[selectedUsers.size()];
        selectedUsers.toArray(selUsers);
        bundle.putStringArray(StringKeys.INVITE_USERS_VM_SELECTED_USERS_KEY, selUsers);
        return bundle;
    }

    public void restoreFromBundle(Bundle b) {
        if (b != null && b.containsKey(StringKeys.INVITE_USERS_VIEWMODEL_BUNDLE_KEY)) {
            Bundle bundle = b.getBundle(StringKeys.INVITE_USERS_VIEWMODEL_BUNDLE_KEY);
            String[] keys = bundle.getStringArray(StringKeys.INVITE_USERS_VM_SELECTED_USERS_KEY);
            ArrayList<String> selectedUsers = new ArrayList<>(Arrays.asList(keys));
            if (selectedUsers != null) {
                for (int i = 0; i < viewModels.size(); i++) {
                    if (selectedUsers.contains(viewModels.get(i).getUser().getUserUuid())) {
                        viewModels.get(i).checked.set(true);
                    }
                }
            }
            searchString.set(bundle.getString(StringKeys.INVITE_USERS_VM_SEARCH_STRING_KEY, ""));
            //filterUserList();
        }
    }
}
