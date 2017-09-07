package com.volynski.familytrack.views;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.volynski.familytrack.R;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.databinding.ActivityUserDetailsBinding;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.viewmodels.UserDetailsViewModel;
import com.volynski.familytrack.views.fragments.InviteUsersDialogFragment;
import com.volynski.familytrack.views.fragments.UserListFragment;
import com.volynski.familytrack.views.fragments.UserOnMapFragment;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 07.09.2017.
 */

public class UserDetailsActivity extends AppCompatActivity {

    private String mCurrentUserUuid = "";
    private ActivityUserDetailsBinding mBinding;
    private String mUserUuid;
    private UserDetailsViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mUserUuid = extractValueFromIntent(getIntent(), StringKeys.USER_UUID_KEY);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_user_details);
        mViewModel = new UserDetailsViewModel(this, mUserUuid,
                new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(this), this));
        mBinding.setViewmodel(mViewModel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_userdetails);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewModel.start();
    }

    private String extractValueFromIntent(Intent intent, String key) {
        String result = "";
        if (intent != null) {
            if (intent.hasExtra(key)) {
                result = intent.getStringExtra(key);
            } else {
                Timber.e("Current user uuid expected but not found in intent");
            }
        }
        return result;
    }
}
