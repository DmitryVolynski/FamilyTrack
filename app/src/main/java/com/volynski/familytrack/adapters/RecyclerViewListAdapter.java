package com.volynski.familytrack.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableList;
import android.databinding.ViewDataBinding;
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
    private ObservableList<T> mData;
    private int mRowLayoutId;
    private int mBindingId;
    private Context mContext;
    private RecyclerViewListAdapterOnClickHandler mItemClickHandler;

    /**
     *
     * @param context
     * @param data
     * @param rowLayoutId
     * @param bindingId
     */
    public RecyclerViewListAdapter(Context context, ObservableList<T> data,
                         int rowLayoutId, int bindingId) {
        mData = data;
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
        final T rowData = mData.get(position);
        holder.bind(mBindingId, rowData);
    }

    @Override
    public int getItemCount() {
        if (mData == null) {
            return 0;
        } else {
            return mData.size();
        }
    }

    public void setData(ObservableList<T> mData) {
        this.mData = mData;
        notifyDataSetChanged();
    }

    public void setItemClickHandler(RecyclerViewListAdapterOnClickHandler mItemClickHandler) {
        this.mItemClickHandler = mItemClickHandler;
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
            if (mItemClickHandler == null) {
                Timber.e("mItemClickHandler == null, click event wasn't proceeded");
            }
            mItemClickHandler.onClick(getAdapterPosition(), view);
        }
    }
}
