package com.volynski.familytrack.adapters;

/**
 * Created by DmitryVolynski on 08.10.2017.
 */

public interface ItemTypesResolver {
    public int getItemViewType(Object item);
    public int getItemViewLayoutId(int viewType);
}
