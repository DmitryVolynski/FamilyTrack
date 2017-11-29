package com.volynski.familytrack.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.volynski.familytrack.R;
import com.volynski.familytrack.viewmodels.PopupMenuListener;

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
    private boolean mPopupMenuEnabled = false;
    private boolean mIsSingleLayout = true;
    private ItemTypesResolver mItemTypesResolver;

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
        mItemTypesResolver = new SimpleItemType(rowLayoutId);
    }

    public RecyclerViewListAdapter(Context context, List<T> viewModels,
                                   ItemTypesResolver itemTypesResolver, int bindingId) {
        mViewModels = viewModels;
        mBindingId = bindingId;
        mContext = context;
        mItemTypesResolver = itemTypesResolver;
        mIsSingleLayout = false;
    }

    @Override
    public int getItemViewType(int position) {
        return mItemTypesResolver.getItemViewType(mViewModels.get(position));
    }

    @Override
    public RecyclerViewListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        ViewDataBinding binding = DataBindingUtil.inflate(layoutInflater,
                mItemTypesResolver.getItemViewLayoutId(viewType), parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new RecyclerViewListAdapterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(RecyclerViewListAdapter.RecyclerViewListAdapterViewHolder holder, int position) {
        final T viewModel = mViewModels.get(position);
        holder.bind(mBindingId, viewModel);

        if (mPopupMenuEnabled) {
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

    public List<T> getViewModels() {
        return this.mViewModels;
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
        mPopupMenuEnabled = true;
        mMenuId = menuId;
        mViewToShowPopupId = viewId;
    }

    public class RecyclerViewListAdapterViewHolder
            extends RecyclerView.ViewHolder
            implements
                View.OnClickListener,
                PopupMenu.OnMenuItemClickListener
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
        public boolean onMenuItemClick(MenuItem item) {
            PopupMenuListener listener =
                    (PopupMenuListener) RecyclerViewListAdapter.this.getViewModels().get(getAdapterPosition());

            if (listener != null) {
                listener.menuCommand(item, binding.getRoot());
            } else {
                Timber.e(mContext.getString(R.string.ex_cant_cast_to_popupmenulistener));
            }
            return false;
        }

        @Override
        public void onClick(View view) {
            if (mPopupMenuEnabled && view.getId() == mViewToShowPopupId) {
                PopupMenu popupMenu = new PopupMenu(RecyclerViewListAdapter.this.mContext, view);
                popupMenu.inflate(mMenuId);
                popupMenu.setOnMenuItemClickListener(this);
                popupMenu.show();
            } else {
                // if item view model implements RecyclerViewListAdapterOnClickHandler then
                // call this method of viewmodel class
                RecyclerViewListAdapterOnClickHandler handler =
                        (RecyclerViewListAdapterOnClickHandler) RecyclerViewListAdapter.this.getViewModels().get(getAdapterPosition());
                if (handler != null) {
                    handler.onClick(getAdapterPosition(), view);
                }
                if (mItemClickHandler != null) {
                    mItemClickHandler.onClick(getAdapterPosition(), view);
                }
            }
        }
    }
}
