package com.volynski.familytrack.views;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.volynski.familytrack.R;
import com.volynski.familytrack.StringKeys;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.databinding.ActivityUserDetailsBinding;
import com.volynski.familytrack.utils.IntentUtil;
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

        readIntentData();
        setupBindings();
        setupToolbar();
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
                    collapsingToolbarLayout.setTitle("User details");
                    isShow = true;
                } else if(isShow) {
                    collapsingToolbarLayout.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    private void setupBindings() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_user_details);
        mViewModel = new UserDetailsViewModel(this, mCurrentUserUuid, mUserUuid,
                new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(this), this));
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
