package com.fisincorporated.aviationweather.soaring.forecast;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.databinding.SoaringConditionView;
import com.fisincorporated.aviationweather.soaring.json.Forecast;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class RecyclerViewAdapterSoaringForecast extends RecyclerView.Adapter<RecyclerViewAdapterSoaringForecast.ViewHolder> {
    private List<Forecast> forecasts;
    private Forecast selectedForecast;
    private RecyclerView recyclerView;
    private RecyclerView.SmoothScroller smoothScroller;

    RecyclerViewAdapterSoaringForecast(List<Forecast> forecasts) {
        this.forecasts = forecasts;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        smoothScroller = getSmoothScroller(recyclerView);

    }

    public void setSelectedForecast(Forecast forecast) {
        selectedForecast = forecast;
    }

    @Override
    public RecyclerViewAdapterSoaringForecast.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SoaringConditionView binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), RecyclerViewAdapterSoaringForecast.ViewHolder.LAYOUT_RESOURCE, parent, false);
        return new RecyclerViewAdapterSoaringForecast.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterSoaringForecast.ViewHolder holder, int position) {
        holder.binding.setForecast(forecasts.get(position));
        holder.binding.setPosition(position);
       // holder.binding.setForecastClickListener(this);
        //holder.bindingsoaringForecastLabel.setSelected((selectedForecast == forecasts.get(position)));
    }

    @Override
    public int getItemCount() {
        return forecasts.size();
    }

    private RecyclerView.SmoothScroller getSmoothScroller(RecyclerView recyclerView) {
        return new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            protected int getHorizontalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };
    }

    public void onForecastClick(Forecast forecast, Integer position) {
        if (forecast == selectedForecast) {
            return;
        }
        if (forecast.getForecastType().equalsIgnoreCase("comment")) {
            return;
        }
        selectedForecast = forecast;
        RecyclerViewAdapterSoaringForecast.this.notifyDataSetChanged();
        smoothScroller.setTargetPosition(position);
        recyclerView.getLayoutManager().startSmoothScroll(smoothScroller);
        EventBus.getDefault().post(forecast);
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