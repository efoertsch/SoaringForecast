package com.fisincorporated.airportweather;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fisincorporated.airportweather.metars.Metar;
import com.fisincorporated.metar.R;
import com.fisincorporated.metar.databinding.AirportMetarTafBinding;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.inject.Inject;



public class AirportWeatherAdapter extends RecyclerView.Adapter<AirportWeatherAdapter.BindingHolder> {

    public ObservableList<Metar> metars = new ObservableArrayList<>();

    // See https://github.com/radzio/android-data-binding-recyclerview/blob/master/recyclerview
    // -binding/src/main/java/net/droidlabs/mvvm/recyclerview/adapter/BindingRecyclerViewAdapter
    // .java
    private final WeakReferenceOnListChangedCallback onListChangedCallback;


    @Inject
    public AirportWeatherAdapter() {
        onListChangedCallback = new WeakReferenceOnListChangedCallback(this);
    }

    public void setMetarList(@Nullable List<Metar> metarList) {

        if (metars == metarList) {
            return;
        }

        // Clean up old list
        if (metars != null) {
            metars.removeOnListChangedCallback(onListChangedCallback);
            notifyItemRangeRemoved(0, metars.size());
            metars.clear();
        }

        if (metarList != null) {
            metars.addOnListChangedCallback(onListChangedCallback);
            metars.addAll(metarList);
            notifyItemRangeInserted(0,metars.size());
        } else {
            metars = null;
        }
    }


    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AirportMetarTafBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent
                .getContext()), BindingHolder.LAYOUT_RESOURCE, parent, false);
        return new BindingHolder(binding);
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {

        holder.binding.setMetar(metars.get(position));
    }

    @Override
    public int getItemCount() {

        return metars.size();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        if (metars != null) {
            metars.removeOnListChangedCallback(onListChangedCallback);
        }
    }


    public static class BindingHolder extends RecyclerView.ViewHolder {
        protected static final int LAYOUT_RESOURCE = R.layout.airport_metar_taf;

        private AirportMetarTafBinding binding;

        public BindingHolder(AirportMetarTafBinding bindingView) {
            super(bindingView.getRoot());
            binding = bindingView;
        }

    }

    private static class WeakReferenceOnListChangedCallback extends ObservableList.OnListChangedCallback {

        private final WeakReference<AirportWeatherAdapter> adapterReference;

        public WeakReferenceOnListChangedCallback(AirportWeatherAdapter AirportWeatherAdapter) {
            this.adapterReference = new WeakReference<>(AirportWeatherAdapter);
        }

        @Override
        public void onChanged(ObservableList sender) {
            RecyclerView.Adapter adapter = adapterReference.get();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount) {
            RecyclerView.Adapter adapter = adapterReference.get();
            if (adapter != null) {
                adapter.notifyItemRangeChanged(positionStart, itemCount);
            }
        }

        @Override
        public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount)
        {
            RecyclerView.Adapter adapter = adapterReference.get();
            if (adapter != null)
            {
                adapter.notifyItemRangeInserted(positionStart, itemCount);
            }
        }

        @Override
        public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount)
        {
            RecyclerView.Adapter adapter = adapterReference.get();
            if (adapter != null)
            {
                adapter.notifyItemMoved(fromPosition, toPosition);
            }
        }

        @Override
        public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount)
        {
            RecyclerView.Adapter adapter = adapterReference.get();
            if (adapter != null)
            {
                adapter.notifyItemRangeRemoved(positionStart, itemCount);
            }
        }
    }
}
