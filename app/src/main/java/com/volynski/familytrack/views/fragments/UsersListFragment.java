package com.volynski.familytrack.views.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.volynski.familytrack.BR;
import com.volynski.familytrack.R;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.databinding.FragmentUsersListBinding;
import com.volynski.familytrack.utils.AuthUtil;
import com.volynski.familytrack.viewmodels.UsersListViewModel;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by DmitryVolynski on 22.08.2017.
 */

public class UsersListFragment
        extends Fragment {
    private UsersListViewModel mViewModel;
    private static final String TAG = UsersListFragment.class.getSimpleName();
    private User mCurrentUser;

    FragmentUsersListBinding mBinding;

    public static UsersListFragment newInstance(Context context) {
        UsersListFragment result = new UsersListFragment();

        // TODO проверить это место. может быть создание модели именно здесь неверно.
        result.setViewModel(new UsersListViewModel(context, new FamilyTrackRepository(null)));
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.start(mCurrentUser);
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
        mCurrentUser = AuthUtil.getCurrentUserFromPrefs(getContext());

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
        final View popupView = inflater.inflate(R.layout.dialog_create_group, null);

        // create the popup window
        int width =  (int) Math.round(0.8 * mainLayout.getWidth());  //LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = (int) Math.round(0.8 * mainLayout.getHeight());  //LinearLayout.LayoutParams.WRAP_CONTENT;

        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        popupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);
        popupView.findViewById(R.id.button_dialogcreategroup_cancel)
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        popupView.findViewById(R.id.button_dialogcreategroup_create)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText editText = (EditText) popupView.findViewById(R.id.edittext_dialogcreategroup_groupname);
                        mViewModel.createNewGroup(editText.getText().toString());
                        popupWindow.dismiss();
                    }
                });
    }

    public void setViewModel(UsersListViewModel mViewModel) {
        this.mViewModel = mViewModel;
    }
}
