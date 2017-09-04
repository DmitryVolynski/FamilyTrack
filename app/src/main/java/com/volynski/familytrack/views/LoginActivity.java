package com.volynski.familytrack.views;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.volynski.familytrack.R;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.views.fragments.FirstTimeUserDialogFragment;
import com.volynski.familytrack.views.navigators.LoginNavigator;

import java.util.List;

import timber.log.Timber;

public class LoginActivity extends AppCompatActivity implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        View.OnClickListener,
        LoginNavigator,
        FamilyTrackDataSource.GetUserByEmailCallback,
        FamilyTrackDataSource.GetContactsToInvite {

    private static final int SIGNED_IN = 0;
    private static final int STATE_SIGNING_IN = 1;
    private static final int STATE_IN_PROGRESS = 2;
    private static final int RC_SIGN_IN = 0;
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    private SignInButton mSignInButton;
    private Button mSignOutButton;
    private Button mRevokeButton;
    private Button mCheckUserExistsButton;
    private Button mMainActivityButton;
    private FirstTimeUserDialogFragment mFirstTimeDialog;

    private TextView mStatus;
    private GoogleApiClient mGoogleApiClient;
    FirebaseAuth mFirebaseAuth;
    private GoogleSignInAccount mGoogleSignInAccount;
    private String mUserEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.plant(new Timber.DebugTree());

        setContentView(R.layout.activity_login);

        SharedPrefsUtil.wipeUserData(this);

        // Get references to all of the UI views
        mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        mSignOutButton = (Button) findViewById(R.id.sign_out_button);
        mRevokeButton = (Button) findViewById(R.id.revoke_access_button);
        mCheckUserExistsButton = (Button) findViewById(R.id.check_user_exists_button);
        mMainActivityButton = (Button) findViewById(R.id.button_login_goto_main_activity);

        mStatus = (TextView) findViewById(R.id.statuslabel);

        // Add click listeners for the buttons
        mSignInButton.setOnClickListener(this);
        mSignInButton.setSize(SignInButton.SIZE_STANDARD);
        mSignInButton.setOnClickListener(this);

        mSignOutButton.setOnClickListener(this);
        mRevokeButton.setOnClickListener(this);
        mCheckUserExistsButton.setOnClickListener(this);
        mMainActivityButton.setOnClickListener(this);

        mStatus = (TextView) findViewById(R.id.statuslabel);

        GoogleSignInOptions gso = new
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("994450542296-cnp3qee96s737dbggug542af6dssih2m.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        checkIfAlreadySignedIn();
    }

    private void checkIfAlreadySignedIn() {
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            //showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    //hideProgressDialog();
                    if (googleSignInResult.isSuccess()) {
                        handleSignInResult(googleSignInResult);
                    }
                }
            });

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            mGoogleSignInAccount = result.getSignInAccount();
            mStatus.setText(mGoogleSignInAccount.getDisplayName());
            mSignInButton.setEnabled(false);
            mUserEmail = mGoogleSignInAccount.getEmail();
            SharedPrefsUtil.setGoogleAccountIdToken(this, mGoogleSignInAccount.getIdToken());
        } else {
            // Signed out, show unauthenticated UI.
            //updateUI(false);
            mStatus.setText("Sign in failed");
            int i = 0;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
            case R.id.check_user_exists_button:
                //String a = UUID.randomUUID().toString();
                checkUserExists();
                break;
            case R.id.button_login_goto_main_activity:
                startMainActivity();
                break;
        }
    }

    private void gotoMainActivity() {
        final FamilyTrackDataSource dataSource =
                new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(this), this);

        dataSource.getUserByEmail(mGoogleSignInAccount.getEmail(),
                new FamilyTrackDataSource.GetUserByEmailCallback() {
            @Override
            public void onGetUserByEmailCompleted(FirebaseResult<User> result) {
                if (result.getData() == null) {
                    // user signed in for the first time and
                    // doesn't exists in db - register him in db
                    User user = new User("", mGoogleSignInAccount.getFamilyName(), mGoogleSignInAccount.getGivenName(),
                            mGoogleSignInAccount.getDisplayName(), mGoogleSignInAccount.getPhotoUrl().toString(),
                            mGoogleSignInAccount.getEmail(), "4838437", null, null);
                    dataSource.createUser(user, new FamilyTrackDataSource.CreateUserCallback() {
                        @Override
                        public void onCreateUserCompleted(FirebaseResult<User> result) {
                            if (result.getData() != null) {
                                SharedPrefsUtil.setCurrentUser(getApplicationContext(), result.getData());
                                startMainActivity();
                            } else {
                                Timber.e("Create user failed.", result.getException());
                            }
                        }
                    });
                } else {
                    // user already registered in db, just save him in SharedPreferences
                    SharedPrefsUtil.setCurrentUser(getApplicationContext(), result.getData());
                    startMainActivity();
                }
            }
        });

    }

    private void startMainActivity() {
        mFirstTimeDialog = FirstTimeUserDialogFragment.newInstance(this, mGoogleSignInAccount, this);
        mFirstTimeDialog.show(getSupportFragmentManager(), "aa");
    }

    private void checkUserExists() {
        /**
        FamilyTrackDataSource dataSource =
                new FamilyTrackRepository(mGoogleSignInAccount.getIdToken(), this);
        dataSource.getUserByEmail(mGoogleSignInAccount.getEmail(), this);
         */
        getLocationPermission();
        FamilyTrackDataSource dataSource =
                new FamilyTrackRepository(mGoogleSignInAccount.getIdToken(), this);
        dataSource.getContactsToInvite(this);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            mStatus.setText("Signed out");
                            mSignInButton.setEnabled(true);
                        }
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private GoogleApiClient buildGoogleApiClient() {
        return null;
    }

    @Override
    public void onGetContactsToInviteCompleted(FirebaseResult<List<User>> result) {
        int i = 0;
    }

    @Override
    public void onGetUserByEmailCompleted(FirebaseResult<User> result) {
        if (result.getData() == null) {
            createNewUser();
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            //mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    }

    @Override
    public void proceedToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void createNewUser() {

        /*
        FamilyTrackDataSource dataSource =
                new FamilyTrackRepository(mGoogleSignInAccount);

        User user = new User("", mGoogleSignInAccount.getFamilyName(), mGoogleSignInAccount.getGivenName(),
                mGoogleSignInAccount.getPhotoUrl().toString(), mGoogleSignInAccount.getEmail(),
                "4838437", User.ROLE_UNDEFINED, User.USER_CREATED, null);
        dataSource.createUser(user);
        */

    }
}
