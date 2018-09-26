package com.fisincorporated.aviationweather.common.recycleradapter;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;


/**
 * Base ViewHolder to be used with the generic adapter.
 * {@link GenericRecyclerViewAdapter}
 *
 * @param <T> type of objects, which will be used in the adapter's data set
 *
 * Based on https://github.com/LeoDroidCoder/generic-adapter but modified
 */
public abstract class BaseViewHolder<T, VDB extends ViewDataBinding> extends RecyclerView.ViewHolder {

    private final  VDB viewDataBinding;

    public BaseViewHolder(VDB bindingView) {
        super(bindingView.getRoot());
        viewDataBinding = bindingView;

    }

    /**
     * Bind data to the item.
     * Make sure not to perform any expensive operations here.
     *
     * @param item object, associated with the item.
     * @since 1.0.0
     */
    public abstract void onBind(T item, int position);




}