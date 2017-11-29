package com.volynski.familytrack.views;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.volynski.familytrack.R;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.databinding.ActivitySettingsBinding;
import com.volynski.familytrack.utils.IntentUtil;
import com.volynski.familytrack.utils.SnackbarUtil;
import com.volynski.familytrack.viewmodels.SettingsViewModel;
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
        intent.putExtra(StringKeys.SNACKBAR_TEXT_KEY, getString(R.string.msg_settings_updated));
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        readIntentData();
        setupBindings();
        setupToolbar();
    }

    private void setupToolbar() {
        Toolbar toolbar = mBinding.toolbarSettings;
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(R.string.settings_toolbar_title);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
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
        mViewModel = (SettingsViewModel) FragmentsUtil.findOrCreateViewModel(this,
                MainActivity.CONTENT_SETTINGS,
                mCurrentUserUuid,
                null);
        mViewModel.setNavigator(this);
        mViewModel.snackbarText.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                SnackbarUtil.showSnackbar(mBinding.coordinatorlayoutSettings, mViewModel.snackbarText.get());
            }
        });
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
