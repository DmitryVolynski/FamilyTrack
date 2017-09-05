package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Observable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableList;
import android.util.Log;
import android.view.View;

import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 31.08.2017.
 */


public class InviteUsersViewModel
        extends BaseObservable
        implements View.OnClickListener {

    private final static String TAG = UserListViewModel.class.getSimpleName();
    private final Context mContext;
    private String mCurrentUserUuid = "";
    private User mCurrentUser;
    private boolean mIsDataLoading = false;
    private UserListNavigator mNavigator;
    private FamilyTrackDataSource mRepository;

    private List<User> mUnfilteredUserList = new ArrayList<>();

    public final ObservableBoolean showDialog = new ObservableBoolean(false);
    public final ObservableList<UserListItemViewModel> viewModels = new ObservableArrayList<>();
    public final ObservableField<String> searchString = new ObservableField<>("");

    public InviteUsersViewModel(Context context, String currentuserUuid,
                             FamilyTrackDataSource dataSource) {
        mCurrentUserUuid = currentuserUuid;
        mContext = context.getApplicationContext();
        mRepository = dataSource;
        searchString.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                filterUserList();
            }
        });
    }

    private void filterUserList() {
        viewModels.clear();
        for (User user : mUnfilteredUserList) {
            if (user.getDisplayName().contains(searchString.get()) ||
                    user.getFamilyName().contains(searchString.get()) ||
                    user.getGivenName().contains(searchString.get()) ||
                    user.getEmail().contains(searchString.get()) ||
                    user.getPhone().contains(searchString.get())) {
                viewModels.add(new UserListItemViewModel(mContext, user, null));
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
                    }
                });
        mNavigator.dismissInviteUsersDialog();
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
    public void start() {
        if (mCurrentUserUuid.equals("")) {
            Timber.e("Can't start viewmodel. UserUuid is empty");
            return;
        }

        mIsDataLoading = true;
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
        mRepository.getContactsToInvite(new FamilyTrackDataSource.GetContactsToInvite() {
            @Override
            public void onGetContactsToInviteCompleted(FirebaseResult<List<User>> result) {
                populateUserListFromDbResult(result);
                mIsDataLoading = false;
            }
        });
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
                viewModels.add(new UserListItemViewModel(mContext, user, null));
                mUnfilteredUserList.add(user);
            }
        }
        notifyChange();
    }

    public void setNavigator(UserListNavigator mNavigator) {
        this.mNavigator = mNavigator;
    }
}
