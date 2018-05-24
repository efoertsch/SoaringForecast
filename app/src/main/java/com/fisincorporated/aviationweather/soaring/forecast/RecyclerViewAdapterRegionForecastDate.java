package com.fisincorporated.aviationweather.soaring.forecast;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.databinding.RegionForecastDateView;
import com.fisincorporated.aviationweather.soaring.json.RegionForecastDate;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class RecyclerViewAdapterRegionForecastDate extends RecyclerView.Adapter<RecyclerViewAdapterRegionForecastDate.ViewHolder> {
    private List<RegionForecastDate> regionForecastDates;
    private int selectedPos = RecyclerView.NO_POSITION;


    public RecyclerViewAdapterRegionForecastDate(List<RegionForecastDate> regionForecastDates) {
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
        holder.binding.regionForecastDateLabel.setSelected(selectedPos == position);
    }

    @Override
    public int getItemCount() {
        return regionForecastDates.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        static final int LAYOUT_RESOURCE = R.layout.region_forecast_date_layout;

        private final RegionForecastDateView  binding;

        ViewHolder(RegionForecastDateView bindingView) {
            super(bindingView.getRoot());
            binding = bindingView;
            bindingView.regionForecastDateLabel.setOnClickListener(v -> {
                notifyItemChanged(selectedPos);
                selectedPos = getLayoutPosition();
                notifyItemChanged(selectedPos);
                EventBus.getDefault().post(v.getTag());
            });
        }


    }
}