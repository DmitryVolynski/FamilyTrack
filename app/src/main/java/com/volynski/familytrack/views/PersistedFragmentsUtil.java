package com.volynski.familytrack.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.viewmodels.InviteUsersViewModel;
import com.volynski.familytrack.viewmodels.UserDetailsViewModel;
import com.volynski.familytrack.viewmodels.UserListViewModel;
import com.volynski.familytrack.viewmodels.UserOnMapViewModel;
import com.volynski.familytrack.views.fragments.ViewModelHolder;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import static com.volynski.familytrack.views.MainActivity.CONTENT_INVITE_USERS;
import static com.volynski.familytrack.views.MainActivity.CONTENT_MAP;
import static com.volynski.familytrack.views.MainActivity.CONTENT_USER_DETAILS;
import static com.volynski.familytrack.views.MainActivity.CONTENT_USER_LIST;

/**
 * Created by DmitryVolynski on 07.11.2017.
 */

public class PersistedFragmentsUtil {

    @NonNull
    public static Object findOrCreateViewModel(AppCompatActivity activity,
                                               int contentId,
                                               String currentUserUuid,
                                               UserListNavigator navigator,
                                               boolean forceRecreate) {
        // In a configuration change we might have a ViewModel present. It's retained using the
        // Fragment Manager.
        Object viewModel;
        Context context = activity.getApplicationContext();
        switch (contentId) {
            case CONTENT_USER_LIST:
                ViewModelHolder<UserListViewModel> userListVM =
                        (ViewModelHolder<UserListViewModel>) activity.getSupportFragmentManager()
                                .findFragmentByTag(UserListViewModel.class.getSimpleName());

                if (!forceRecreate && userListVM != null && userListVM.getViewmodel() != null) {
                    // If the model was retained, return it.
                    viewModel = userListVM.getViewmodel();
                    ((UserListViewModel) viewModel).setCreatedFromViewHolder(true);
                    return viewModel;
                } else {
                    // There is no ViewModel yet, create it.
                    viewModel = new UserListViewModel(context,
                            currentUserUuid,
                            new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(context), context),
                            navigator);
                    ((UserListViewModel) viewModel).setCreatedFromViewHolder(false);
                    // and bind it to this Activity's lifecycle using the Fragment Manager.
                }
                break;
            case CONTENT_MAP:
                ViewModelHolder<UserOnMapViewModel> userOnMapVM =
                        (ViewModelHolder<UserOnMapViewModel>) activity.getSupportFragmentManager()
                                .findFragmentByTag(UserOnMapViewModel.class.getSimpleName());

                if (!forceRecreate && userOnMapVM != null && userOnMapVM.getViewmodel() != null) {
                    // If the model was retained, return it.
                    viewModel = userOnMapVM.getViewmodel();
                    ((UserOnMapViewModel) viewModel).setCreatedFromViewHolder(true);
                    return viewModel;
                } else {
                    // There is no ViewModel yet, create it.
                    viewModel = new UserOnMapViewModel(context,
                            currentUserUuid,
                            new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(context), context),
                            navigator);
                    ((UserOnMapViewModel) viewModel).setCreatedFromViewHolder(false);
                    // and bind it to this Activity's lifecycle using the Fragment Manager.
                }
                break;
            case CONTENT_INVITE_USERS:
                ViewModelHolder<InviteUsersViewModel> inviteUsersVM =
                        (ViewModelHolder<InviteUsersViewModel>) activity.getSupportFragmentManager()
                                .findFragmentByTag(InviteUsersViewModel.class.getSimpleName());

                if (!forceRecreate && inviteUsersVM != null && inviteUsersVM.getViewmodel() != null) {
                    // If the model was retained, return it.
                    viewModel = inviteUsersVM.getViewmodel();
                    ((InviteUsersViewModel) viewModel).setCreatedFromViewHolder(true);
                    return viewModel;
                } else {
                    // There is no ViewModel yet, create it.
                    viewModel = new InviteUsersViewModel(context,
                            currentUserUuid,
                            new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(context), context),
                            navigator);
                    ((InviteUsersViewModel) viewModel).setCreatedFromViewHolder(false);
                    // and bind it to this Activity's lifecycle using the Fragment Manager.
                }
                break;
            case CONTENT_USER_DETAILS:
                ViewModelHolder<UserDetailsViewModel> userDetailsVM =
                        (ViewModelHolder<UserDetailsViewModel>) activity.getSupportFragmentManager()
                                .findFragmentByTag(UserDetailsViewModel.class.getSimpleName());

                if (!forceRecreate && userDetailsVM != null && userDetailsVM.getViewmodel() != null) {
                    // If the model was retained, return it.
                    viewModel = userDetailsVM.getViewmodel();
                    ((UserDetailsViewModel) viewModel).setCreatedFromViewHolder(true);
                    return viewModel;
                } else {
                    // There is no ViewModel yet, create it.
                    viewModel = new UserDetailsViewModel(context,
                            currentUserUuid,
                            new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(context), context));
                    ((UserDetailsViewModel) viewModel).setCreatedFromViewHolder(false);
                    // and bind it to this Activity's lifecycle using the Fragment Manager.
                }
                break;

            default:
                viewModel = null;
        }
        activity.getSupportFragmentManager()
                .beginTransaction()
                .add(ViewModelHolder.createContainer(viewModel), viewModel.getClass().getSimpleName())
                .commit();
        return viewModel;
    }
}
