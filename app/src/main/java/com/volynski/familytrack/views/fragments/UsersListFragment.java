package com.volynski.familytrack.views.fragments;

import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.volynski.familytrack.BR;
import com.volynski.familytrack.R;
import com.volynski.familytrack.databinding.FragmentUsersListBinding;
import com.volynski.familytrack.viewmodels.UsersListViewModel;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by DmitryVolynski on 22.08.2017.
 */

public class UsersListFragment
        extends Fragment {
    private UsersListViewModel mViewModel;
    private static final String TAG = UsersListFragment.class.getSimpleName();

    FragmentUsersListBinding mBinding;

    public static UsersListFragment newInstance() {
        UsersListFragment result = new UsersListFragment();
        result.setViewModel(new UsersListViewModel());
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
                R.layout.fragment_users_list,
                container,
                false);
        mBinding.setViewmodel(mViewModel);
        mViewModel.showDialog.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                UsersListFragment.this.startNewGroupDialog();
            }
        });
        return mBinding.getRoot();
    }

    private void startNewGroupDialog() {
        // get a reference to the already created main layout

        LinearLayout mainLayout = mBinding.linearlayoutFragmentuserslistRootlayout;

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.dialog_create_group, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        popupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        /*
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
        */
    }

    public void setViewModel(UsersListViewModel mViewModel) {
        this.mViewModel = mViewModel;
    }
}
