package com.volynski.familytrack.views.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.volynski.familytrack.R;
import com.volynski.familytrack.databinding.FragmentUsersListBinding;
import com.volynski.familytrack.databinding.FragmentUsersOnMapBinding;
import com.volynski.familytrack.viewmodels.UsersOnMapViewModel;

/**
 * Created by DmitryVolynski on 22.08.2017.
 */

public class UsersOnMapFragment extends Fragment {
    private static final String TAG = UsersOnMapFragment.class.getSimpleName();

    private UsersOnMapViewModel mViewModel;

    FragmentUsersOnMapBinding mBinding;

    public static UsersOnMapFragment newInstance(Context context) {
        UsersOnMapFragment result = new UsersOnMapFragment();
        result.setViewModel(new UsersOnMapViewModel(context));
        return result;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_users_on_map,
                container,
                false);
        return mBinding.getRoot();
    }

    public void setViewModel(UsersOnMapViewModel mViewModel) {
        this.mViewModel = mViewModel;
    }
}
