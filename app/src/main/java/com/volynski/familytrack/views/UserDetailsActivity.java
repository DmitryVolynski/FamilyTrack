package com.volynski.familytrack.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.volynski.familytrack.R;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.databinding.ActivityUserDetailsBinding;
import com.volynski.familytrack.dialogs.SimpleDialogFragment;
import com.volynski.familytrack.utils.IntentUtil;
import com.volynski.familytrack.utils.SnackbarUtil;
import com.volynski.familytrack.viewmodels.UserDetailsViewModel;
import com.volynski.familytrack.views.navigators.UserDetailsNavigator;

/**
 * Created by DmitryVolynski on 07.09.2017.
 */

public class UserDetailsActivity extends AppCompatActivity implements UserDetailsNavigator {

    private String mCurrentUserUuid = "";
    private ActivityUserDetailsBinding mBinding;
    private String mUserUuid;
    private UserDetailsViewModel mViewModel;

    public void dbopCompleted(String result, String snackbarText) {
        Intent intent = new Intent();
        intent.putExtra(StringKeys.USER_UPDATE_RESULT_KEY, result);
        intent.putExtra(StringKeys.SNACKBAR_TEXT_KEY, snackbarText);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        postponeEnterTransition();

        readIntentData();
        setupBindings();
        setupToolbar();
        setupListeners();
    }

    private void setupListeners() {
        mViewModel.snackbarText.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
            SnackbarUtil.showSnackbar(mBinding.coordinatorlayoutUserdetails, mViewModel.snackbarText.get());
            }
        });

        mViewModel.familyNameError.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                mBinding.tilUserdetailsFamilyname.setError(mViewModel.familyNameError.get());
            }
        });
        mViewModel.givenNameError.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                mBinding.tilUserdetailsGivenname.setError(mViewModel.givenNameError.get());
            }
        });
        mViewModel.displayNameError.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                mBinding.tilUserdetailsDisplayname.setError(mViewModel.displayNameError.get());
            }
        });
        mViewModel.phoneError.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                mBinding.tilUserdetailsPhone.setError(mViewModel.phoneError.get());
            }
        });

        mViewModel.isDataLoading.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                if (!mViewModel.isDataLoading.get()) {
                    startPostponedEnterTransition();
                }
            }
        });

        mBinding.etUserdetailsFamilyName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mViewModel.validateUserData();
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = mBinding.toolbarUserdetails;
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        final CollapsingToolbarLayout collapsingToolbarLayout =
                mBinding.clpstoolbarUserdetails;
        collapsingToolbarLayout.setTitle(" ");
        AppBarLayout appBarLayout = mBinding.appbarUserdetails;
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(getString(R.string.user_details_toolbar_title));
                    isShow = true;
                } else if(isShow) {
                    collapsingToolbarLayout.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_save_user:
                mViewModel.updateUser();
                break;
            case R.id.action_exclude_user:
                startRemoveUser();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void startRemoveUser() {
        final SimpleDialogFragment confirmDialog =
                new SimpleDialogFragment();
        confirmDialog.setParms(getString(R.string.remove_user_dialog_title),
                String.format(getString(R.string.remove_user_dialog_message),
                        mViewModel.user.get().getDisplayName(), mViewModel.activeGroup.get()),
                getString(R.string.label_button_ok),
                getString(R.string.button_cancel_label),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mViewModel.removeUser();
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        confirmDialog.dismiss();
                    }
                });
        confirmDialog.show(getFragmentManager(), getString(R.string.dialog_tag));
    }

    private void setupBindings() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_user_details);
        mViewModel = (UserDetailsViewModel) FragmentsUtil.findOrCreateViewModel(this,
                MainActivity.CONTENT_USER_DETAILS,
                mCurrentUserUuid,
                null);
        mViewModel.setUserUuid(mUserUuid);
        mViewModel.setNavigator(this);
        mBinding.setViewmodel(mViewModel);
    }

    private void readIntentData() {
        mCurrentUserUuid = IntentUtil.extractValueFromIntent(getIntent(), StringKeys.CURRENT_USER_UUID_KEY);
        mUserUuid = IntentUtil.extractValueFromIntent(getIntent(), StringKeys.USER_UUID_KEY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewModel.start();
    }


}
