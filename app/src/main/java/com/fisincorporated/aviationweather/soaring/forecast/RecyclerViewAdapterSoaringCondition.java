package com.fisincorporated.aviationweather.soaring.forecast;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.databinding.SoaringConditionView;
import com.fisincorporated.aviationweather.soaring.json.Forecast;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class RecyclerViewAdapterSoaringCondition extends RecyclerView.Adapter<RecyclerViewAdapterSoaringCondition.ViewHolder> {
    private List<Forecast> forecastOptions;
    private int selectedPos = RecyclerView.NO_POSITION;


    public RecyclerViewAdapterSoaringCondition(List<Forecast> forecastOptions) {
        this.forecastOptions = forecastOptions;
    }

    public void setSelectedPosition(int selectedPos, boolean notify) {
        this.selectedPos = selectedPos;
        if (notify) {
            notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerViewAdapterSoaringCondition.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SoaringConditionView binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), RecyclerViewAdapterSoaringCondition.ViewHolder.LAYOUT_RESOURCE, parent, false);
        RecyclerViewAdapterSoaringCondition.ViewHolder viewHolder = new RecyclerViewAdapterSoaringCondition.ViewHolder(binding);
        viewHolder.itemView.setOnClickListener(v -> {
            Forecast forecast = forecastOptions.get(viewHolder.getAdapterPosition());
            if (forecast.getForecastType().equalsIgnoreCase("comment")) {
                return;
            }
            notifyItemChanged(selectedPos);
            selectedPos = viewHolder.getLayoutPosition();
            notifyItemChanged(selectedPos);
            EventBus.getDefault().post(forecast);
        });
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterSoaringCondition.ViewHolder holder, int position) {
        holder.binding.setForecast(forecastOptions.get(position));
        holder.binding.soaringConditionOption.setTag(forecastOptions.get(position));
        holder.binding.soaringConditionOption.setSelected(selectedPos == position);
    }

    @Override
    public int getItemCount() {
        return forecastOptions.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        static final int LAYOUT_RESOURCE = R.layout.soaring_forecast_option;

        private final SoaringConditionView binding;

        ViewHolder(SoaringConditionView bindingView) {
            super(bindingView.getRoot());
            binding = bindingView;
        }

    }
}