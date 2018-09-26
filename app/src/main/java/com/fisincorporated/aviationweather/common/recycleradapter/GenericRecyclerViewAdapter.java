package com.fisincorporated.aviationweather.common.recycleradapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Base generic RecyclerView adapter.
 * Handles basic logic such as adding/removing items,
 * setting listener, binding ViewHolders.
 * Extend the adapter for appropriate use case.
 *
 * @param <T>  type of objects, which will be used in the adapter's dataset
 * @param <VH> ViewHolder {@link BaseViewHolder}
 * @author Leonid Ustenko (Leo.Droidcoder@gmail.com)
 * @since 1.0.0
 * <p>
 * Based on https://github.com/LeoDroidCoder/generic-adapter but with modifications
 */
public abstract class GenericRecyclerViewAdapter<T, VH extends BaseViewHolder> extends RecyclerView.Adapter<VH> {

    private List<T> items = new ArrayList<>();
    private T selectedItem;
    private LayoutInflater layoutInflater;
    private RecyclerView recyclerView;
    private RecyclerView.SmoothScroller smoothScroller;


    public GenericRecyclerViewAdapter(List<T> items) {
       this.items.addAll(items);
    }

    /**
     * Base constructor.
     * Allocate adapter-related objects here if needed.
     *
     * @param context Context needed to retrieve LayoutInflater
     */

    public GenericRecyclerViewAdapter(Context context) {
        this.layoutInflater = LayoutInflater.from(context);
    }

    /**
     * To be implemented in as specific adapter.
     * Here you should return new ViewHolder instance.
     * You may also return different ViewHolders according to a view type.
     * In this case you shoulf also override {@link RecyclerView.Adapter#getItemViewType(int)}
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @Override
    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);


    public void setSelectedItem(T selectedItem) {
        this.selectedItem = selectedItem;
        notifyDataSetChanged();
    }

    public void setSelectedItem(int position) {
        if (position <items.size()){
            selectedItem = items.get(position);
        } else {
            if (items.size() > 0) {
                selectedItem = items.get(0);
            }
        }
        notifyDataSetChanged();
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        smoothScroller = getSmoothScroller(recyclerView);

    }

    private RecyclerView.SmoothScroller getSmoothScroller(RecyclerView recyclerView) {
        return new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            protected int getHorizontalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the itemView to reflect the item at the given
     * position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(VH holder, int position) {
        if (items.size() <= position) {
            return;
        }
        T item = items.get(position);
        holder.onBind(item, position);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    /**
     * Sets items to the adapter and notifies that data set has been changed.
     *
     * @param items items to set to the adapter
     * @author Leonid Ustenko (Leo.Droidcoder@gmail.com)
     * @since 1.0.0
     */
    public void setItems(List<T> items) {
        setItems(items, true);
    }

    /**
     * Sets items to the adapter and notifies that data set has been changed.
     * Typically this method should be use with `notifyChanges = false` in case you are using DiffUtil
     * {@link android.support.v7.util.DiffUtil} in order to delegate it do all the updating job.
     *
     * @param items         items to set to the adapter
     * @param notifyChanges pass in <code>true</code> to call notifiDatasetChanged {@link RecyclerView.Adapter#notifyDataSetChanged()} or <code>false</code> otherwise
     * @throws IllegalArgumentException in case of setting `null` items
     * @author Leonid Ustenko (Leo.Droidcoder@gmail.com)
     * @since 1.0.0
     */
    public void setItems(List<T> items, boolean notifyChanges) throws IllegalArgumentException {
        if (items == null) {
            throw new IllegalArgumentException("Cannot set `null` item to the Recycler adapter");
        }
        this.items.clear();
        this.items.addAll(items);
        if (notifyChanges) {
            notifyDataSetChanged();
        }
    }

    /**
     * Updates items list.
     * Typically to be used for the implementation of DiffUtil {@link android.support.v7.util.DiffUtil}
     *
     * @param newItems new items
     */
    public void updateItems(List<T> newItems) {
        setItems(newItems, false);
    }

    /**
     * Updates items with use of DiffUtil callback {@link DiffUtil.Callback}
     *
     * @param newItems     new items
     * @param diffCallback DiffUtil callback
     */
    public void updateItems(List<T> newItems, DiffUtil.Callback diffCallback) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(diffCallback, false);
        setItems(newItems, false);
        result.dispatchUpdatesTo(this);
    }

    /**
     * Returns all items from the data set held by the adapter.
     *
     * @return All of items in this adapter.
     * @author Leonid Ustenko (Leo.Droidcoder@gmail.com)
     * @since 1.0.0
     */
    public List<T> getItems() {
        return items;
    }

    /**
     * Returns an items from the data set at a certain position.
     *
     * @return All of items in this adapter.
     */
    public T getItem(int position) {
        return items.get(position);
    }

    /**
     * Adds item to the end of the data set.
     * Notifies that item has been inserted.
     *
     * @param item item which has to be added to the adapter.
     * @author Leonid Ustenko (Leo.Droidcoder@gmail.com)
     * @since 1.0.0
     */
    public void add(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot add null item to the Recycler adapter");
        }
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    /**
     * Adds item to the beginning of the data set.
     * Notifies that item has been inserted.
     *
     * @param item item which has to be added to the adapter.
     * @author Leonid Ustenko (Leo.Droidcoder@gmail.com)
     * @since 1.0.0
     */
    public void addToBeginning(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot add null item to the Recycler adapter");
        }
        items.add(0, item);
        notifyItemInserted(0);
    }

    /**
     * Adds list of items to the end of the adapter's data set.
     * Notifies that item has been inserted.
     *
     * @param items items which has to be added to the adapter.
     * @author Leonid Ustenko (Leo.Droidcoder@gmail.com)
     * @since 1.0.0
     */
    public void addAll(List<T> items) {
        if (items == null) {
            throw new IllegalArgumentException("Cannot add `null` items to the Recycler adapter");
        }
        this.items.addAll(items);
        notifyItemRangeInserted(this.items.size() - items.size(), items.size());
    }

    /**
     * Clears all the items in the adapter.
     *
     * @author Leonid Ustenko (Leo.Droidcoder@gmail.com)
     * @since 1.0.0
     */
    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    /**
     * Removes an item from the adapter.
     * Notifies that item has been removed.
     *
     * @param item to be removed
     * @author Leonid Ustenko (Leo.Droidcoder@gmail.com)
     * @since 1.0.0
     */
    public void remove(T item) {
        int position = items.indexOf(item);
        if (position > -1) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Returns whether adapter is empty or not.
     *
     * @return `true` if adapter is empty or `false` otherwise
     * @author Leonid Ustenko (Leo.Droidcoder@gmail.com)
     * @since 1.0.0
     */
    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    /**
     * Indicates whether each item in the data set can be represented with a unique identifier
     * of type {@link Long}.
     *
     * @param hasStableIds Whether items in data set have unique identifiers or not.
     * @see #hasStableIds()
     * @see #getItemId(int)
     */
    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }


    /**
     * Inflates a view.
     *
     * @param layout       layout to me inflater
     * @param parent       container where to inflate
     * @param attachToRoot whether to attach to root or not
     * @return inflated View
     * @author Leonid Ustenko (Leo.Droidcoder@gmail.com)
     * @since 1.0.0
     */
    @NonNull
    protected View inflate(@LayoutRes final int layout, @Nullable final ViewGroup parent, final boolean attachToRoot) {
        return layoutInflater.inflate(layout, parent, attachToRoot);
    }

    /**
     * Inflates a view.
     *
     * @param layout layout to me inflater
     * @param parent container where to inflate
     * @return inflated View
     * @author Leonid Ustenko (Leo.Droidcoder@gmail.com)
     * @since 1.0.0
     */
    @NonNull
    protected View inflate(@LayoutRes final int layout, final @Nullable ViewGroup parent) {
        return inflate(layout, parent, false);
    }

    public void smoothScrollToPosition(int position) {
        smoothScroller.setTargetPosition(position);
        recyclerView.getLayoutManager().startSmoothScroll(smoothScroller);
    }

    public void scrollToPostion(int position){
        recyclerView.scrollToPosition(position);
    }

    public T getSelectedItem() {
        return selectedItem;
    }
}




