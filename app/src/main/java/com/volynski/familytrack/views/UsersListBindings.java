package com.volynski.familytrack.views;

import android.databinding.BindingAdapter;
import android.databinding.ObservableList;
import android.support.v7.widget.RecyclerView;
import android.widget.ListView;

import com.volynski.familytrack.adapters.RecyclerViewListAdapter;
import com.volynski.familytrack.data.models.firebase.User;

import java.util.List;

/**
 * Created by DmitryVolynski on 25.08.2017.
 */

public class UsersListBindings {
    @SuppressWarnings("unchecked")
    @BindingAdapter("app:items")
    public static void setItems(RecyclerView recyclerView, ObservableList<User> users) {
        RecyclerViewListAdapter<User> adapter = (RecyclerViewListAdapter<User>) recyclerView.getAdapter();
        if (adapter != null)
        {
            adapter.setData(users);
        }
    }}
