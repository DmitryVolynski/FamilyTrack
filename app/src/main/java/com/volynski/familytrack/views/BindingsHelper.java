package com.volynski.familytrack.views;

import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.databinding.InverseBindingListener;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;

import com.squareup.picasso.Picasso;
import com.volynski.familytrack.R;
import com.volynski.familytrack.adapters.RecyclerViewListAdapter;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.viewmodels.GeofenceEventListItemViewModel;
import com.volynski.familytrack.viewmodels.MembershipListItemViewModel;
import com.volynski.familytrack.viewmodels.UserListItemViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

/**
 * Created by DmitryVolynski on 25.08.2017.
 */

public class BindingsHelper {
    @SuppressWarnings("unchecked")

    @BindingAdapter("app:viewModels")
    public static void setViewModels(RecyclerView recyclerView,
                                     List<UserListItemViewModel> viewModels) {
        RecyclerViewListAdapter<UserListItemViewModel> adapter =
                (RecyclerViewListAdapter<UserListItemViewModel>) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setViewModels(viewModels);
        }
    }

    @BindingAdapter("app:geofenceEvents")
    public static void setGeofenceEvents(RecyclerView recyclerView,
                                     List<GeofenceEventListItemViewModel> viewModels) {
        RecyclerViewListAdapter<GeofenceEventListItemViewModel> adapter =
                (RecyclerViewListAdapter<GeofenceEventListItemViewModel>) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setViewModels(viewModels);
        }
    }

    @BindingAdapter("app:usersOfGroup")
    public static void setUsersOfGroup(RecyclerView recyclerView,
                                       Map<String, User> users) {
        RecyclerViewListAdapter<User> adapter =
                (RecyclerViewListAdapter<User>) recyclerView.getAdapter();
        if (adapter != null) {
            List<User> list = new ArrayList<User>(users.values());
            adapter.setViewModels(list);
        }
    }

    @BindingAdapter("app:groupListItemViewModels")
    public static void setGroupListItemViewModels(RecyclerView recyclerView,
                                     List<MembershipListItemViewModel> viewModels) {
        RecyclerViewListAdapter<MembershipListItemViewModel> adapter =
                (RecyclerViewListAdapter<MembershipListItemViewModel>) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setViewModels(viewModels);
        }
    }

    @BindingAdapter("app:groups")
    public static void setGroups(RecyclerView recyclerView, List<MembershipListItemViewModel> viewModels) {
        RecyclerViewListAdapter<MembershipListItemViewModel>
                adapter = (RecyclerViewListAdapter<MembershipListItemViewModel>) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setViewModels(viewModels);
        }
    }

    @BindingAdapter("app:roundImageUrl")
    public static void bindRoundImage(ImageView view, String imageUrl) {
        if (imageUrl != null && !imageUrl.equals("")) {
            Picasso.with(view.getContext())
                    .load(imageUrl)
                    .transform(new CropCircleTransformation())
                    .into(view);
        } else {
            //view.setImageResource(R.mipmap.ic_no_user_photo);
        }
    }

    @BindingAdapter("app:imageUrl")
    public static void bindImage(ImageView view, String imageUrl) {
        if (imageUrl != null && !imageUrl.equals("")) {
            Picasso.with(view.getContext())
                    .load(imageUrl)
                    .into(view);
        } else {
            view.setImageResource(R.mipmap.ic_no_user_photo);
        }
    }

    @BindingAdapter(value = {"bind:selectedValue", "bind:selectedValueAttrChanged"}, requireAll = false)
    public static void bindSpinnerData(Spinner spinner,
                                       String newSelectedValue,
                                       final InverseBindingListener newTextAttrChanged) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                newTextAttrChanged.onChange();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        if (newSelectedValue != null) {
            int pos = -1;
            for (int i=0; i < spinner.getAdapter().getCount(); i++) {
                if (spinner.getAdapter().getItem(i).equals(newSelectedValue)) {
                    pos = i;
                    break;
                }
            }
            spinner.setSelection(pos, true);
        }
    }
    @InverseBindingAdapter(attribute = "bind:selectedValue", event = "bind:selectedValueAttrChanged")
    public static String captureSelectedValue(Spinner spinner) {
        return (String) spinner.getSelectedItem();
    }
}
