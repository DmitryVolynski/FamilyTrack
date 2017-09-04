package com.volynski.familytrack.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.views.navigators.LoginNavigator;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 02.09.2017.
 */

public class FirstTimeUserViewModel extends BaseObservable {
    private final static int STEP_ENTER_YOUR_PHONE_NUMBER = 0;
    private final static int STEP_HOW_TO_START_APP = 1;
    private final static int OPTION_CREATE_NEW_GROUP = 0;
    private final static int OPTION_JOIN_GROUP = 1;

    private final static String TAG = UserListViewModel.class.getSimpleName();
    private final Context mContext;
    private GoogleSignInAccount mGoogleSignInAccount;
    private boolean mIsDataLoading = false;
    private LoginNavigator mNavigator;
    private FamilyTrackDataSource mRepository;

    // model fields
    public final ObservableField<String> phoneNumber = new ObservableField<>("+79857602865");
    public final ObservableInt  dialogStepNo = new ObservableInt(STEP_ENTER_YOUR_PHONE_NUMBER);
    public final ObservableBoolean createNewGroupOption = new ObservableBoolean(true);
    public final ObservableBoolean joinExistingGroupOption = new ObservableBoolean(false);
    public final ObservableField<String> newGroupName = new ObservableField<>("My group");


    public FirstTimeUserViewModel(Context context,
                                  GoogleSignInAccount googleSignInAccount,
                                  FamilyTrackDataSource dataSource) {
        mContext = context.getApplicationContext();
        mGoogleSignInAccount = googleSignInAccount;
        mRepository = dataSource;
    }

    /**
     * Starts loading data from contact list of the phone
     */
    public void start() {
        mIsDataLoading = true;
        loadGroupsList();
    }

    /**
     * Reads list of groups in which current user was invited
     */
    private void loadGroupsList() {

    }

    public void goStepTwo() {
        dialogStepNo.set(STEP_HOW_TO_START_APP);
    }

    public void decide() {
        /*
        mRepository.getGroupsAvailableToJoin("dd", new FamilyTrackDataSource.GetGroupsAvailableToJoinCallback() {
            @Override
            public void onGetGroupsAvailableToJoinCompleted(FirebaseResult<List<Group>> result) {
                checkResult(result);
            }
        });
        */
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
                                        SharedPrefsUtil.setCurrentUserUuid(mContext, result.getData().getUserUuid());
                                        proceedUserChoice(result.getData().getUserUuid());
                                    } else {
                                        Timber.e("Create user failed.", result.getException());
                                    }
                                }
                            });
                        } else {
                            // user already registered in db, just save him in SharedPreferences
                            SharedPrefsUtil.setCurrentUserUuid(mContext, result.getData().getUserUuid());
                            proceedUserChoice(result.getData().getUserUuid());
                        }
                    }
                });

    }

    private void proceedUserChoice(String userUuid) {
        if (createNewGroupOption.get()) { createNewGroup(newGroupName.get(), userUuid); }
        if (joinExistingGroupOption.get()) { joinExistingGroup(); }
    }

    private void joinExistingGroup() {
    }

    private void createNewGroup(String groupName, String userUuid) {
        Timber.v("Create group: " + groupName);
        mRepository.createGroup(new Group(groupName), userUuid, null);
        mNavigator.proceedToMainActivity();
    }

    private void checkResult(Object result) {
        int i = 0;
    }

    public void doItLater() {
        mNavigator.proceedToMainActivity();
    }

    public void setNavigator(LoginNavigator mNavigator) {
        this.mNavigator = mNavigator;
    }

    public GoogleSignInAccount getGoogleSignInAccount() {
        return mGoogleSignInAccount;
    }

    public void setGoogleSignInAccount(GoogleSignInAccount mGoogleSignInAccount) {
        this.mGoogleSignInAccount = mGoogleSignInAccount;
    }
}


