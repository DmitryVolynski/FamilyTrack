package com.volynski.familytrack.adapters;

/**
 * Created by DmitryVolynski on 08.10.2017.
 */

public class SimpleItemType implements ItemTypesResolver {
    private int mLayoutId;

    public SimpleItemType(int layoutId) {
        mLayoutId = layoutId;
    }

    @Override
    public int getItemViewType(Object item) {
        return 1;
    }

    @Override
    public int getItemViewLayoutId(int viewType) {
        return mLayoutId;
    }
}
