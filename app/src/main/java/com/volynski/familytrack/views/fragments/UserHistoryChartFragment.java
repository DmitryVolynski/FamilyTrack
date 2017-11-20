package com.volynski.familytrack.views.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.maps.model.LatLng;
import com.volynski.familytrack.BR;
import com.volynski.familytrack.R;
import com.volynski.familytrack.adapters.RecyclerViewListAdapter;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.databinding.FragmentUserHistoryChartBinding;
import com.volynski.familytrack.databinding.FragmentUserOnMapBinding;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.viewmodels.UserHistoryChartViewModel;
import com.volynski.familytrack.views.MainActivity;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DmitryVolynski on 25.09.2017.
 */

public class UserHistoryChartFragment extends Fragment implements View.OnClickListener{
    private UserHistoryChartViewModel mViewModel;
    private LinearLayoutManager mLayoutManager;

    FragmentUserHistoryChartBinding mBinding;
    private RecyclerViewListAdapter mAdapter;

    public static UserHistoryChartFragment newInstance(Context context,
                                                       String currentUserUuid,
                                                       UserListNavigator navigator) {
        UserHistoryChartFragment result = new UserHistoryChartFragment();

        UserHistoryChartViewModel viewModel = new UserHistoryChartViewModel(context, currentUserUuid,
                new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(context), context));

        viewModel.setNavigator(navigator);
        result.setViewModel(viewModel);
        return result;
    }

    @Override
    public void onClick(View v) {
        ToggleButton tb = (ToggleButton)v;
        if (tb != null && mViewModel != null) {
            mViewModel.onToggleButtonClick(tb.getTextOn().toString());
        }
    }

    public void setViewModel(UserHistoryChartViewModel viewModel) {
        this.mViewModel = viewModel;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(R.string.toolbar_title_history_chart);
        mViewModel.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_user_history_chart,
                container,
                false);

        setupToggleButtons();
        setupCustomListeners();

        boolean isLandscape = getResources().getBoolean(R.bool.is_landscape);
        mLayoutManager = new LinearLayoutManager(getContext(),
                (isLandscape ? LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL), false);

        mBinding.recyclerviewFrguserhistorychart.setLayoutManager(mLayoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(getContext(), mLayoutManager.getOrientation());

        mBinding.recyclerviewFrguserhistorychart.addItemDecoration(dividerItemDecoration);
        mAdapter = new RecyclerViewListAdapter(this.getContext(), mViewModel.viewModels,
                R.layout.user_horizontal_list_item, BR.viewmodel);

        mBinding.recyclerviewFrguserhistorychart.setAdapter(mAdapter);
        mBinding.setViewmodel(mViewModel);


        return mBinding.getRoot();
    }

    private void setupCustomListeners() {
        mViewModel.redrawChart.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                UserHistoryChartFragment.this.redrawChart();
            }
        });
    }

    private void redrawChart() {
        int maxSeparateValues = 5;

        // let's take only 5 most visited places
        int c = 0;
        int otherPlacesCount = 0;

        List<PieEntry> entries = new ArrayList<>();
        for (int key : mViewModel.userStatistic.keySet()) {
            if (c++ < maxSeparateValues) {
                entries.add(new PieEntry(key, mViewModel.userStatistic.get(key)));
            } else {
                otherPlacesCount += key;
            }
        }

        if (c > maxSeparateValues) {
            entries.add(new PieEntry(otherPlacesCount, "Other"));
        }

        PieDataSet set = new PieDataSet(entries, "Most visited places");
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData data = new PieData(set);

        mBinding.chartFrguserhistorychart.setData(data);
        mBinding.chartFrguserhistorychart.getLegend().setEnabled(false);
        //legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        //legend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        mBinding.chartFrguserhistorychart.animateY(2000);
    }


    private void setupToggleButtons() {
        mBinding.tbutOff.setOnClickListener(this);
        mBinding.tbut1h.setOnClickListener(this);
        mBinding.tbut8h.setOnClickListener(this);
        mBinding.tbut1d.setOnClickListener(this);
        mBinding.tbut1w.setOnClickListener(this);
    }

    public void userClicked(User user) {
        mViewModel.selectUser(user);
    }
}

