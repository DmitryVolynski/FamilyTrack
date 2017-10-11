package com.volynski.familytrack.views.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.volynski.familytrack.BR;
import com.volynski.familytrack.R;
import com.volynski.familytrack.adapters.RecyclerViewListAdapter;
import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.databinding.FragmentUserListBinding;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.viewmodels.UserListViewModel;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by DmitryVolynski on 22.08.2017.
 */

public class UserListFragment
        extends Fragment {
    private UserListViewModel mViewModel;
    //private String mCurrentUserUuid;
    private GridLayoutManager mLayoutManager;

    FragmentUserListBinding mBinding;
    private RecyclerViewListAdapter mAdapter;

    public static UserListFragment newInstance(Context context,
                                               String currentUserUuid,
                                               UserListNavigator navigator) {
        UserListFragment result = new UserListFragment();

        // TODO проверить это место. может быть создание модели именно здесь неверно.
        UserListViewModel viewModel = new UserListViewModel(context, currentUserUuid,
                new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(context), context));
        viewModel.setNavigator(navigator);
        result.setViewModel(viewModel);
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.start();
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
                R.layout.fragment_user_list,
                container,
                false);
        mLayoutManager = new GridLayoutManager(this.getContext(), 1);
        mBinding.recyclerviewFragmentuserslistUserslist.setLayoutManager(mLayoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mBinding.recyclerviewFragmentuserslistUserslist.getContext(),
                        mLayoutManager.getOrientation());

        mBinding.recyclerviewFragmentuserslistUserslist.addItemDecoration(dividerItemDecoration);
        mAdapter = new RecyclerViewListAdapter(this.getContext(), mViewModel.viewModels,
                R.layout.user_list_item, BR.viewmodel);
        mAdapter.enablePopupMenu(R.menu.user_popup_menu, R.id.imageview_userslistitem_popupsymbol);

        mBinding.recyclerviewFragmentuserslistUserslist.setAdapter(mAdapter);
        mBinding.setViewmodel(mViewModel);

        return mBinding.getRoot();
    }



    public void setViewModel(UserListViewModel mViewModel) {
        this.mViewModel = mViewModel;
    }

    public void inviteUser() {

    }
}
