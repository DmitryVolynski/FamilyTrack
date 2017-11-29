package com.volynski.familytrack.viewmodels;

import android.app.PendingIntent;
import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Observable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ObservableList;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.volynski.familytrack.R;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.MembershipListItem;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.utils.NetworkUtil;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.views.navigators.LoginNavigator;

import java.util.List;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 02.09.2017.
 */

public class FirstTimeUserViewModel extends AbstractViewModel {
    private final static int STEP_ENTER_YOUR_PHONE_NUMBER = 0;
    private final static int STEP_HOW_TO_START_APP = 1;
    private final static int OPTION_CREATE_NEW_GROUP = 0;
    private final static int OPTION_JOIN_GROUP = 1;
    private final static int OPTION_DECIDE_LATER = 2;

    private final static String TAG = UserListViewModel.class.getSimpleName();
    private GoogleSignInAccount mGoogleSignInAccount;
    private LoginNavigator mNavigator;

    // model fields
    public final ObservableField<String> phoneNumber =
             new ObservableField<>("");
    public final ObservableInt  dialogStepNo = new ObservableInt(STEP_ENTER_YOUR_PHONE_NUMBER);
    public final ObservableBoolean createNewGroupOption = new ObservableBoolean(true);
    public final ObservableBoolean joinExistingGroupOption = new ObservableBoolean(false);
    public final ObservableBoolean decideLaterOption = new ObservableBoolean(false);
    public final ObservableField<String> newGroupName = new ObservableField<>("My group");
    public final ObservableList<GroupListItemViewModel> availableGroups =
            new ObservableArrayList<>();
    private int mSelectedGroupIndex = -1;
    private String mSelectedGroupUuid = "";

    public FirstTimeUserViewModel(Context context,
                                  GoogleSignInAccount googleSignInAccount,
                                  FamilyTrackDataSource dataSource) {
        super(context, "", dataSource);
        mGoogleSignInAccount = googleSignInAccount;

        phoneNumber.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                loadGroupsList();
            }
        });
    }

    /**
     * Starts loading data from contact list of the phone
     */
    public void start() {
    }

    /**
     * Reads list of groups in which current user was invited
     */
    private void loadGroupsList() {
        if (!NetworkUtil.networkUp(mContext)) {
            mNavigator.showPopupDialog(mContext.getString(R.string.warning_dialog_title),
                    mContext.getString(R.string.network_not_available));
            return;
        }
        mRepository.getGroupsAvailableToJoin(phoneNumber.get(),
                new FamilyTrackDataSource.GetGroupsAvailableToJoinCallback() {
            @Override
            public void onGetGroupsAvailableToJoinCompleted(FirebaseResult<List<Group>> result) {
                if (result.getData() != null) {
                    availableGroups.clear();
                    for (Group group : result.getData()) {
                        // сюда надо загрузить список групп, которые доступны для присоединения
                        // скорее всего надо сделать отдельный viewModel
                        availableGroups.add(new GroupListItemViewModel(
                                group.getGroupUuid(),
                                group.getName(),
                                group.getGroupUuid().equals(mSelectedGroupUuid),
                                FirstTimeUserViewModel.this));
                    }
                }
            }
        });
    }

    public void goStepTwo() {
        if (phoneNumber.get().equals("")) {
            mNavigator.showPopupDialog(mContext.getString(R.string.phone_number_dialog_title),
                    mContext.getString(R.string.phone_number_dialog_message));
            return;
        }
        phoneNumber.set(phoneNumber.get().replaceAll("[ ()-]", ""));
        isDataLoading.set(true);
        loadGroupsList();
        dialogStepNo.set(STEP_HOW_TO_START_APP);
    }

    // нужно пересмотреть работу данного метода
    // пользователь добавляется всегда, если существует - то апдейт
    // плюс сохранение состояния viewmodel

    public void decide() {
        if (!NetworkUtil.networkUp(mContext)) {
            mNavigator.showPopupDialog(mContext.getString(R.string.warning_dialog_title),
                    mContext.getString(R.string.network_not_available));
            return;
        }

        if (!validateUserData()) {
            return;
        }

        mRepository.getUserByEmail(mGoogleSignInAccount.getEmail(),
                new FamilyTrackDataSource.GetUserByEmailCallback() {
                    @Override
                    public void onGetUserByEmailCompleted(FirebaseResult<User> result) {
                        if (result.getData() == null) {
                            // user signed in for the first time and
                            // doesn't exists in db - register him in db
                            User user = new User("", mGoogleSignInAccount.getFamilyName(), mGoogleSignInAccount.getGivenName(),
                                    mGoogleSignInAccount.getDisplayName(), mGoogleSignInAccount.getPhotoUrl().toString(),
                                    mGoogleSignInAccount.getEmail(), phoneNumber.get(), null, null);
                            mRepository.createUser(user, new FamilyTrackDataSource.CreateUserCallback() {
                                @Override
                                public void onCreateUserCompleted(FirebaseResult<User> result) {
                                    if (result.getData() != null) {
                                        proceedUserChoice(result.getData().getUserUuid());
                                    } else {
                                        Timber.e("Create user failed.", result.getException());
                                    }
                                }
                            });
                        } else {
                            // user already registered in db, update user record from Google account
                            User user = result.getData();
                            final String userUuid = user.getUserUuid();
                            user.setFamilyName(mGoogleSignInAccount.getFamilyName());
                            user.setGivenName(mGoogleSignInAccount.getGivenName());
                            user.setEmail(mGoogleSignInAccount.getEmail());
                            user.setPhotoUrl(mGoogleSignInAccount.getPhotoUrl().buildUpon().toString());
                            mRepository.updateUser(user, new FamilyTrackDataSource.UpdateUserCallback() {
                                @Override
                                public void onUpdateUserCompleted(FirebaseResult<String> result) {
                                    if (result.getData().equals(FirebaseResult.RESULT_OK)) {
                                        proceedUserChoice(userUuid);
                                    }
                                }
                            });
                        }
                    }
                });

    }

    private boolean validateUserData() {
        if (phoneNumber.get().equals("")) {
            mNavigator.showPopupDialog(mContext.getString(R.string.phone_number_dialog_title),
                    mContext.getString(R.string.phone_number_dialog_message));
            return false;
        }

        if (mSelectedGroupUuid.equals("") && joinExistingGroupOption.get()) {
            mNavigator.showPopupDialog(mContext.getString(R.string.join_group_dialog_title),
                    mContext.getString(R.string.join_group_dialog_message));
            return false;
        }

        if (newGroupName.get().equals("") && createNewGroupOption.get()) {
            mNavigator.showPopupDialog(mContext.getString(R.string.create_new_group_dialog_title),
                    mContext.getString(R.string.create_new_group_dialog_message));
            return false;
        }

        return true;
    }

    private void proceedUserChoice(String userUuid) {
        if (createNewGroupOption.get()) {
            createNewGroup(newGroupName.get(), userUuid);
        } else {
            if (joinExistingGroupOption.get()) {
                joinExistingGroup(userUuid);
            } else {
                if (decideLaterOption.get()) {
                    mNavigator.proceedToMainActivity(userUuid);
                }
            }
        }
    }

    private void joinExistingGroup(final String userUuid) {

        mRepository.changeUserMembership(userUuid, "", mSelectedGroupUuid,
                new FamilyTrackDataSource.ChangeUserMembershipCallback() {
                    @Override
                    public void onChangeUserMembershipCompleted(FirebaseResult<String> result) {
                        mNavigator.proceedToMainActivity(userUuid);
                    }
                });
    }

    private void createNewGroup(String groupName, final String userUuid) {
        mRepository.createGroup(new Group(groupName), userUuid, new FamilyTrackDataSource.CreateGroupCallback() {
            @Override
            public void onCreateGroupCompleted(FirebaseResult<Group> result) {
                mNavigator.proceedToMainActivity(userUuid);
            }
        });
    }

    public void setNavigator(LoginNavigator mNavigator) {
        this.mNavigator = mNavigator;
    }

    public void setGoogleSignInAccount(GoogleSignInAccount mGoogleSignInAccount) {
        this.mGoogleSignInAccount = mGoogleSignInAccount;
    }

    public void groupSelected(String mGroupUuid) {
        mSelectedGroupUuid = mGroupUuid;
        for (GroupListItemViewModel vm : availableGroups) {
            vm.selected.set(vm.getGroupUuid().equals(mGroupUuid));
        }
    }
}


