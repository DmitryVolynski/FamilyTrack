package com.volynski.familytrack.views;

import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.databinding.InverseBindingListener;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.volynski.familytrack.R;
import com.volynski.familytrack.adapters.RecyclerViewListAdapter;
import com.volynski.familytrack.data.models.firebase.Group;
import com.volynski.familytrack.data.models.firebase.Location;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.viewmodels.GroupListItemViewModel;
import com.volynski.familytrack.viewmodels.UserListItemViewModel;

import java.util.List;
import java.util.Observable;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

/**
 * Created by DmitryVolynski on 25.08.2017.
 */

public class UserListBindings {
    @SuppressWarnings("unchecked")
    @BindingAdapter("app:items")
    public static void setItems(RecyclerView recyclerView, List<User> users) {
        RecyclerViewListAdapter<UserListItemViewModel>
                adapter = (RecyclerViewListAdapter<UserListItemViewModel>) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setViewModels(UserListItemViewModel.createViewModels(recyclerView.getContext(), users));
        }
    }

    @BindingAdapter("app:viewModels")
    public static void setViewModels(RecyclerView recyclerView,
                                     List<UserListItemViewModel> viewModels) {
        RecyclerViewListAdapter<UserListItemViewModel> adapter =
                (RecyclerViewListAdapter<UserListItemViewModel>) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setViewModels(viewModels);
        }
    }

    @BindingAdapter("app:groups")
    public static void setGroups(RecyclerView recyclerView, List<GroupListItemViewModel> viewModels) {
        RecyclerViewListAdapter<GroupListItemViewModel>
                adapter = (RecyclerViewListAdapter<GroupListItemViewModel>) recyclerView.getAdapter();
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
            view.setImageResource(R.mipmap.ic_no_user_photo);
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
