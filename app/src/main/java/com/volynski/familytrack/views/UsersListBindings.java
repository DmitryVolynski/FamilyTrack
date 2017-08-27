package com.volynski.familytrack.views;

import android.databinding.BindingAdapter;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.volynski.familytrack.R;
import com.volynski.familytrack.adapters.RecyclerViewListAdapter;
import com.volynski.familytrack.data.models.firebase.User;
import com.volynski.familytrack.viewmodels.UserListItemViewModel;

import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

/**
 * Created by DmitryVolynski on 25.08.2017.
 */

public class UsersListBindings {
    @SuppressWarnings("unchecked")
    @BindingAdapter("app:items")
    public static void setItems(RecyclerView recyclerView, List<User> users) {
        RecyclerViewListAdapter<UserListItemViewModel>
                adapter = (RecyclerViewListAdapter<UserListItemViewModel>) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setViewModels(UserListItemViewModel.createViewModels(recyclerView.getContext(), users));
        }
    }

    @BindingAdapter("app:imageUrl")
    public static void bindImage(ImageView view, String imageUrl) {
        if (imageUrl != "") {
            Picasso.with(view.getContext())
                    .load(imageUrl)
                    .transform(new CropCircleTransformation())
                    .into(view);
        } else {
            view.setImageResource(R.mipmap.ic_no_user_photo);
        }
    }
}
