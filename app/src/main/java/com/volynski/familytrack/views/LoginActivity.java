package com.volynski.familytrack.views;

import android.Manifest;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

//import com.firebase.jobdispatcher.FirebaseJobDispatcher;
//import com.firebase.jobdispatcher.GooglePlayDriver;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.volynski.familytrack.BuildConfig;
import com.volynski.familytrack.R;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.data.FamilyTrackDataSource;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.FirebaseResult;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.dialogs.SimpleDialogFragment;
import com.volynski.familytrack.services.FirebaseListenersService;
import com.volynski.familytrack.utils.MyDebugTree;
import com.volynski.familytrack.utils.NetworkUtil;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.viewmodels.FirstTimeUserViewModel;
import com.volynski.familytrack.views.fragments.FirstTimeUserDialogFragment;
import com.volynski.familytrack.views.fragments.ViewModelHolder;
import com.volynski.familytrack.views.navigators.LoginNavigator;

import timber.log.Timber;

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener,
        LoginNavigator,
        FamilyTrackDataSource.GetUserByEmailCallback {

    private static final int SIGNED_IN = 0;
    private static final int STATE_SIGNING_IN = 1;
    private static final int STATE_IN_PROGRESS = 2;
    private static final int RC_SIGN_IN = 0;
    private static final int RC_PHONE_NUMBER = 12;

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int PERMISSIONS_ACCESS_FINE_LOCATION = 2;
    private static final int PERMISSIONS_ACCESS_COARSE_LOCATION = 3;

    private SignInButton mSignInButton;

    private FirstTimeUserDialogFragment mFirstTimeDialog;
    private FirstTimeUserViewModel mFirstTimeUserViewModel;

    private GoogleApiClient mGoogleApiClient;
    FirebaseAuth mFirebaseAuth;
    private GoogleSignInAccount mGoogleSignInAccount;
    private boolean phoneHintStarted;
    private boolean mOrientationChanged;
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.plant(new MyDebugTree());

        setContentView(R.layout.activity_login);
        SharedPrefsUtil.wipeUserData(this);

        mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(this);
        mSignInButton.setSize(SignInButton.SIZE_STANDARD);

        if (!NetworkUtil.networkUp(this)) {
            Toast.makeText(this, getString(R.string.network_not_available), Toast.LENGTH_LONG).show();
            finish();
        }

        GoogleSignInOptions gso = new
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.WEB_CLIENT_ID_KEY)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this )
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Auth.CREDENTIALS_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        mOrientationChanged = (savedInstanceState != null);
    }

    private void checkIfAlreadySignedIn() {
        OptionalPendingResult<GoogleSignInResult> opr =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
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
                    } else {
                        mSignInButton.setVisibility(View.VISIBLE);
                    }
                }
            });

        }
    }

    // Construct a request for phone numbers and show the picker
    private void requestHint() {
        if (phoneHintStarted) return;
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();

        PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(
                mGoogleApiClient, hintRequest);
        try {
            startIntentSenderForResult(intent.getIntentSender(),
                    RC_PHONE_NUMBER, null, 0, 0, 0, null);
            phoneHintStarted = true;
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLocationPermission();
        checkIfAlreadySignedIn();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, R.string.google_api_client_connection_failed,
                Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            mSignInButton.setEnabled(false);
            mGoogleSignInAccount = result.getSignInAccount();
            loginToFirebase();
        } else {
            // Signed out, show unauthenticated UI.
            Toast.makeText(this, R.string.sign_in_failed_message, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loginToFirebase() {
        String idToken = mGoogleSignInAccount.getIdToken();
        if (idToken == null) {
            Toast.makeText(this, R.string.ex_check_web_client_id, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        SharedPrefsUtil.setGoogleAccountIdToken(this, idToken);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mFirebaseAuth = FirebaseAuth.getInstance();
        if (mFirebaseAuth.getCurrentUser() == null) {
            mFirebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //Timber.v(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                            startMainActivity();
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Timber.e("signInWithCredential", task.getException());
                                Toast.makeText(LoginActivity.this, "Login to Firebase failed", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        }
                    });
        } else {
            startMainActivity();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RC_SIGN_IN:
                // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleSignInResult(result);
                break;
            case RC_PHONE_NUMBER:
                String phoneNumber = "";
                if (resultCode == RESULT_OK) {
                    Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                    phoneNumber = credential.getId();
                }
                showFirstTimeUserDialog(phoneNumber);
                break;
            }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean(StringKeys.FIRST_TIME_USER_DIALOG_KEY, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void startMainActivity() {
        FamilyTrackDataSource dataSource =
                new FamilyTrackRepository(mGoogleSignInAccount.getIdToken(), this);
        dataSource.getUserByEmail(mGoogleSignInAccount.getEmail(), this);
    }

/*
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
*/

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.

         */

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void proceedToMainActivity(String userUuid) {

        Intent serviceIntent = new Intent(this, FirebaseListenersService.class);
        serviceIntent.putExtra(StringKeys.CURRENT_USER_UUID_KEY, userUuid);
        startService(serviceIntent);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(StringKeys.CURRENT_USER_UUID_KEY, userUuid);
        intent.putExtra(StringKeys.MAIN_ACTIVITY_MODE_KEY, MainActivity.CONTENT_MAP);
        startActivity(intent);

        finish();
    }

    @Override
    public void onGetUserByEmailCompleted(FirebaseResult<User> result) {
        if (result.getData() == null || result.getData().getPhotoUrl().equals(StringKeys.CREATED_FROM_CONTACTS_KEY)) {
            if (mOrientationChanged) {
                showFirstTimeUserDialog("");
            } else {
                requestHint();
            }
        } else {
            proceedToMainActivity(result.getData().getUserUuid());
        }
    }

    private void showFirstTimeUserDialog(String phoneNumber) {
        mFirstTimeDialog = FirstTimeUserDialogFragment.newInstance();

        mFirstTimeUserViewModel = findOrCreateViewModel();

        if (!mOrientationChanged) {
            mFirstTimeUserViewModel.phoneNumber.set(phoneNumber);
        }

        mFirstTimeUserViewModel.setGoogleSignInAccount(mGoogleSignInAccount);

        mFirstTimeUserViewModel.setNavigator(this);
        mFirstTimeDialog.setViewModel(mFirstTimeUserViewModel);

        mFirstTimeDialog.show(getSupportFragmentManager(), "aa");
    }

    public FirstTimeUserViewModel findOrCreateViewModel() {
        FirstTimeUserViewModel viewModel;

        ViewModelHolder<FirstTimeUserViewModel> vm =
                (ViewModelHolder<FirstTimeUserViewModel>) getSupportFragmentManager()
                        .findFragmentByTag(FirstTimeUserViewModel.class.getSimpleName());

        if (vm != null && vm.getViewmodel() != null) {
            viewModel = vm.getViewmodel();
            viewModel.setCreatedFromViewHolder(true);
            viewModel.setNavigator(this);
        } else {
            viewModel = new FirstTimeUserViewModel(this,
                    mGoogleSignInAccount,
                    new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(this), this));
            viewModel.setCreatedFromViewHolder(false);
            viewModel.setNavigator(this);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(ViewModelHolder.createContainer(viewModel),
                            viewModel.getClass().getSimpleName())
                    .commit();
            getSupportFragmentManager()
                    .executePendingTransactions();
        }
        return viewModel;
    }

    @Override
    public void showPopupDialog(String title, String message) {
        final SimpleDialogFragment confirmDialog = new SimpleDialogFragment();
        confirmDialog.setParms(title, message, getString(R.string.label_button_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        confirmDialog.dismiss();
                    }
                });
        confirmDialog.show(getFragmentManager(), getString(R.string.dialog_tag));
    }
}
