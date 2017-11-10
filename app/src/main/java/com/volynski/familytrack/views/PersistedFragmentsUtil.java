package com.volynski.familytrack.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.volynski.familytrack.data.FamilyTrackRepository;
import com.volynski.familytrack.data.models.firebase.Settings;
import com.volynski.familytrack.utils.SharedPrefsUtil;
import com.volynski.familytrack.viewmodels.GeofenceEventsViewModel;
import com.volynski.familytrack.viewmodels.InviteUsersViewModel;
import com.volynski.familytrack.viewmodels.SettingsViewModel;
import com.volynski.familytrack.viewmodels.UserDetailsViewModel;
import com.volynski.familytrack.viewmodels.UserListViewModel;
import com.volynski.familytrack.viewmodels.UserMembershipViewModel;
import com.volynski.familytrack.viewmodels.UserOnMapViewModel;
import com.volynski.familytrack.views.fragments.ViewModelHolder;
import com.volynski.familytrack.views.navigators.UserListNavigator;

import static com.volynski.familytrack.views.MainActivity.CONTENT_GEOFENCE_EVENTS;
import static com.volynski.familytrack.views.MainActivity.CONTENT_INVITE_USERS;
import static com.volynski.familytrack.views.MainActivity.CONTENT_MAP;
import static com.volynski.familytrack.views.MainActivity.CONTENT_MEMBERSHIP;
import static com.volynski.familytrack.views.MainActivity.CONTENT_SETTINGS;
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
                    viewModel = userListVM.getViewmodel();
                    ((UserListViewModel) viewModel).setCreatedFromViewHolder(true);
                    return viewModel;
                } else {
                    viewModel = new UserListViewModel(context,
                            currentUserUuid,
                            new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(context), context),
                            navigator);
                    ((UserListViewModel) viewModel).setCreatedFromViewHolder(false);
                }
                break;
            case CONTENT_MAP:
                ViewModelHolder<UserOnMapViewModel> userOnMapVM =
                        (ViewModelHolder<UserOnMapViewModel>) activity.getSupportFragmentManager()
                                .findFragmentByTag(UserOnMapViewModel.class.getSimpleName());

                if (!forceRecreate && userOnMapVM != null && userOnMapVM.getViewmodel() != null) {
                    viewModel = userOnMapVM.getViewmodel();
                    ((UserOnMapViewModel) viewModel).setCreatedFromViewHolder(true);
                    return viewModel;
                } else {
                    viewModel = new UserOnMapViewModel(context,
                            currentUserUuid,
                            new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(context), context),
                            navigator);
                    ((UserOnMapViewModel) viewModel).setCreatedFromViewHolder(false);
                }
                break;
            case CONTENT_INVITE_USERS:
                ViewModelHolder<InviteUsersViewModel> inviteUsersVM =
                        (ViewModelHolder<InviteUsersViewModel>) activity.getSupportFragmentManager()
                                .findFragmentByTag(InviteUsersViewModel.class.getSimpleName());

                if (!forceRecreate && inviteUsersVM != null && inviteUsersVM.getViewmodel() != null) {
                    viewModel = inviteUsersVM.getViewmodel();
                    ((InviteUsersViewModel) viewModel).setCreatedFromViewHolder(true);
                    return viewModel;
                } else {
                    viewModel = new InviteUsersViewModel(context,
                            currentUserUuid,
                            new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(context), context),
                            navigator);
                    ((InviteUsersViewModel) viewModel).setCreatedFromViewHolder(false);
                }
                break;
            case CONTENT_USER_DETAILS:
                ViewModelHolder<UserDetailsViewModel> userDetailsVM =
                        (ViewModelHolder<UserDetailsViewModel>) activity.getSupportFragmentManager()
                                .findFragmentByTag(UserDetailsViewModel.class.getSimpleName());

                if (!forceRecreate && userDetailsVM != null && userDetailsVM.getViewmodel() != null) {
                    viewModel = userDetailsVM.getViewmodel();
                    ((UserDetailsViewModel) viewModel).setCreatedFromViewHolder(true);
                    return viewModel;
                } else {
                    viewModel = new UserDetailsViewModel(context,
                            currentUserUuid,
                            new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(context), context));
                    ((UserDetailsViewModel) viewModel).setCreatedFromViewHolder(false);
                }
                break;
            case CONTENT_SETTINGS:
                ViewModelHolder<SettingsViewModel> settingsVM =
                        (ViewModelHolder<SettingsViewModel>) activity.getSupportFragmentManager()
                                .findFragmentByTag(SettingsViewModel.class.getSimpleName());

                if (!forceRecreate && settingsVM != null && settingsVM.getViewmodel() != null) {
                    viewModel = settingsVM.getViewmodel();
                    ((SettingsViewModel) viewModel).setCreatedFromViewHolder(true);
                    return viewModel;
                } else {
                    viewModel = new SettingsViewModel(context,
                            currentUserUuid,
                            new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(context), context));
                    ((SettingsViewModel) viewModel).setCreatedFromViewHolder(false);
                }
                break;
            case CONTENT_GEOFENCE_EVENTS:
                ViewModelHolder<GeofenceEventsViewModel> geofenceEventsVM =
                        (ViewModelHolder<GeofenceEventsViewModel>) activity.getSupportFragmentManager()
                                .findFragmentByTag(GeofenceEventsViewModel.class.getSimpleName());

                if (!forceRecreate && geofenceEventsVM != null &&
                        geofenceEventsVM.getViewmodel() != null) {
                    viewModel = geofenceEventsVM.getViewmodel();
                    ((GeofenceEventsViewModel) viewModel).setCreatedFromViewHolder(true);
                    return viewModel;
                } else {
                    viewModel = new GeofenceEventsViewModel(context,
                            currentUserUuid,
                            new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(context), context));
                    ((GeofenceEventsViewModel) viewModel).setNavigator(navigator);
                    ((GeofenceEventsViewModel) viewModel).setCreatedFromViewHolder(false);
                }
                break;
            case CONTENT_MEMBERSHIP:
                ViewModelHolder<UserMembershipViewModel> membershipVM =
                        (ViewModelHolder<UserMembershipViewModel>) activity.getSupportFragmentManager()
                                .findFragmentByTag(UserMembershipViewModel.class.getSimpleName());

                if (!forceRecreate && membershipVM != null &&
                        membershipVM.getViewmodel() != null) {
                    viewModel = membershipVM.getViewmodel();
                    ((UserMembershipViewModel) viewModel).setCreatedFromViewHolder(true);
                    return viewModel;
                } else {
                    viewModel = new UserMembershipViewModel(context,
                            currentUserUuid,
                            new FamilyTrackRepository(SharedPrefsUtil.getGoogleAccountIdToken(context), context));
                    ((UserMembershipViewModel) viewModel).setNavigator(navigator);
                    ((UserMembershipViewModel) viewModel).setCreatedFromViewHolder(false);
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
