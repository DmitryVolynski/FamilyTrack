package com.volynski.familytrack.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 25.08.2017.
 */


public class RecyclerViewListAdapter<T>
        extends RecyclerView.Adapter<RecyclerViewListAdapter.RecyclerViewListAdapterViewHolder> {
    private List<T> mViewModels;
    private int mRowLayoutId;
    private int mBindingId;
    private Context mContext;
    private RecyclerViewListAdapterOnClickHandler mItemClickHandler;
    private int mMenuId = -1;
    private int mViewToShowPopupId = -1;
    /**
     *
     * @param context
     * @param viewModels
     * @param rowLayoutId
     * @param bindingId
     */
    public RecyclerViewListAdapter(Context context, List<T> viewModels,
                         int rowLayoutId, int bindingId) {
        mViewModels = viewModels;
        mRowLayoutId = rowLayoutId;
        mBindingId = bindingId;
        mContext = context;
    }

    @Override
    public RecyclerViewListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding = DataBindingUtil.inflate(layoutInflater,
                mRowLayoutId, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new RecyclerViewListAdapterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(RecyclerViewListAdapter.RecyclerViewListAdapterViewHolder holder, int position) {
        final T viewModel = mViewModels.get(position);
        holder.bind(mBindingId, viewModel);

        if (mViewToShowPopupId != -1) {
            View v = holder.itemView.findViewById(mViewToShowPopupId);
            v.setOnClickListener(holder);
        }
    }

    @Override
    public int getItemCount() {
        if (mViewModels == null) {
            return 0;
        } else {
            return mViewModels.size();
        }
    }

    public void setViewModels(List<T> viewModels) {
        this.mViewModels = viewModels;
        notifyDataSetChanged();
    }

    public void setItemClickHandler(RecyclerViewListAdapterOnClickHandler mItemClickHandler) {
        this.mItemClickHandler = mItemClickHandler;
    }

    /**
     * Enables popup menu with id menuId when user clicks on view with viewId
     * @param menuId menu Id to show
     * @param viewId view Id to handle clicks from
     */
    public void enablePopupMenu(int menuId, int viewId) {
        mMenuId = menuId;
        mViewToShowPopupId = viewId;
    }

    public class RecyclerViewListAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private final ViewDataBinding binding;

        public RecyclerViewListAdapterViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(this);
        }

        public void bind(int bindingId, Object obj) {
            binding.setVariable(bindingId, obj);
            binding.executePendingBindings();
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == mViewToShowPopupId) {
                PopupMenu popupMenu = new PopupMenu(RecyclerViewListAdapter.this.mContext, view);
                popupMenu.inflate(mMenuId);
                popupMenu.show();
            }

            if (mItemClickHandler == null) {
                Timber.e("mItemClickHandler == null, click event wasn't proceeded");
                return;
            } else {
                mItemClickHandler.onClick(getAdapterPosition(), view);
            }
        }
    }
}
