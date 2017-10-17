package com.volynski.familytrack.views;

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
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.databinding.ActivitySettingsBinding;
import com.volynski.familytrack.databinding.ActivityUserDetailsBinding;
import com.volynski.familytrack.utils.IntentUtil;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.viewmodels.SettingsViewModel;
import com.volynski.familytrack.viewmodels.UserDetailsViewModel;
import com.volynski.familytrack.views.navigators.SettingsNavigator;

/**
 * Created by DmitryVolynski on 13.10.2017.
 */

public class SettingsActivity extends AppCompatActivity implements SettingsNavigator {

    private String mCurrentUserUuid = "";
    private ActivitySettingsBinding mBinding;
    private String mUserUuid;
    private SettingsViewModel mViewModel;

    @Override
    public void updateCompleted(String result) {
        Intent intent = new Intent();
        intent.putExtra(StringKeys.SETTINGS_UPDATE_RESULT_KEY, result);
        intent.putExtra(StringKeys.SNACKBAR_TEXT_KEY, "Settings updated");
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        readIntentData();
        setupBindings();
        setupToolbar();
        //setupListeners();
    }

/*
    private void setupListeners() {
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

        mBinding.etUserdetailsFamilyName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mViewModel.validateUserData();
            }
        });
    }
*/

    private void setupToolbar() {
        Toolbar toolbar = mBinding.toolbarSettings;
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings_menu, menu);
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
            case (R.id.action_update_settings):
                mViewModel.updateSettings();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }


    private void setupBindings() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        mViewModel = new SettingsViewModel(this, mCurrentUserUuid,
                new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(this), this),
                this);
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
