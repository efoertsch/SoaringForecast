package com.fisincorporated.aviationweather.soaring.forecast;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.databinding.RegionForecastDateView;
import com.fisincorporated.aviationweather.soaring.json.RegionForecastDate;

import java.util.List;

public class RecyclerViewAdapterRegionForecastDate extends RecyclerView.Adapter<RecyclerViewAdapterRegionForecastDate.ViewHolder> {
    private List<RegionForecastDate> regionForecastDates;
    public RegionForecastDateClickListener regionForecastDateClickListener;

    public RecyclerViewAdapterRegionForecastDate(RegionForecastDateClickListener RegionForecastDateClickListener, List<RegionForecastDate> regionForecastDates) {
        this.regionForecastDateClickListener = RegionForecastDateClickListener;
        this.regionForecastDates = regionForecastDates;
    }

    @Override
    public RecyclerViewAdapterRegionForecastDate.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewAdapterRegionForecastDate.ViewHolder(DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), ViewHolder.LAYOUT_RESOURCE, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterRegionForecastDate.ViewHolder holder, int position) {
        holder.binding.setRegionForecastDate(regionForecastDates.get(position));
        holder.binding.regionForecastDateLabel.setTag(regionForecastDates.get(position));
        holder.binding.setTypeClickListener(regionForecastDateClickListener);
    }


    @Override
    public int getItemCount() {
        return regionForecastDates.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected static final int LAYOUT_RESOURCE = R.layout.region_forecast_date_layout;

        private final RegionForecastDateView  binding;

        public ViewHolder(RegionForecastDateView  bindingView) {
            super(bindingView.getRoot());
            binding = bindingView;
        }

    }
}