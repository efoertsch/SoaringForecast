package com.fisincorporated.aviationweather.airport.list;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.databinding.AirportDetailBinding;
import com.fisincorporated.aviationweather.repository.Airport;

import java.util.List;

public class AirportListAdapter extends RecyclerView.Adapter<AirportListAdapter.BindingHolder> {

    private List<Airport> airports;

    public  AirportListAdapter() { }

    public void setAirportList(List<Airport> newAirportList) {
        this.airports = newAirportList;
        notifyDataSetChanged();
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AirportDetailBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent
                .getContext()), AirportListAdapter.BindingHolder.LAYOUT_RESOURCE, parent, false);
        return new AirportListAdapter.BindingHolder(binding);
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        holder.binding.setAirport(airports.get(position));
    }

    @Override
    public int getItemCount() {
        return airports == null ? 0 : airports.size();
    }

    public static class BindingHolder extends RecyclerView.ViewHolder {
        protected static final int LAYOUT_RESOURCE = R.layout.airport_detail;

        private AirportDetailBinding binding;

        public BindingHolder(AirportDetailBinding bindingView) {
            super(bindingView.getRoot());
            binding = bindingView;
        }

    }
}
