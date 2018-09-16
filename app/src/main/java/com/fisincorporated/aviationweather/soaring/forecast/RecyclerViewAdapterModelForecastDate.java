package com.fisincorporated.aviationweather.soaring.forecast;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.databinding.ModelForecastDateView;
import com.fisincorporated.aviationweather.soaring.json.ModelForecastDate;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class RecyclerViewAdapterModelForecastDate extends RecyclerView.Adapter<RecyclerViewAdapterModelForecastDate.ViewHolder> {
    private List<ModelForecastDate> modelForecastDates;
    private ModelForecastDate selectedModelForecastDate;
    private RecyclerView recyclerView;
    private RecyclerView.SmoothScroller smoothScroller;

    RecyclerViewAdapterModelForecastDate(List<ModelForecastDate> modelForecastDates) {
        this.modelForecastDates = modelForecastDates;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        smoothScroller = getSmoothScroller(recyclerView);
    }

    public void updateModelForecastDateList(List<ModelForecastDate> modelForecastDates) {
        this.modelForecastDates = modelForecastDates;
        notifyDataSetChanged();
    }

    public void setSelectedModelForecastDate(ModelForecastDate modelForecastDate) {
        selectedModelForecastDate = modelForecastDate;
        notifyDataSetChanged();
    }

    public void setSelectedModelForecastDate(int position) {
        if (position < modelForecastDates.size()){
            selectedModelForecastDate = modelForecastDates.get(position);
        } else {
            if (modelForecastDates.size() > 0) {
                selectedModelForecastDate = modelForecastDates.get(0);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerViewAdapterModelForecastDate.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ModelForecastDateView binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), ViewHolder.LAYOUT_RESOURCE, parent, false);
        return new RecyclerViewAdapterModelForecastDate.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterModelForecastDate.ViewHolder holder, int position) {
        holder.binding.setModelForecastDate(modelForecastDates.get(position));
        holder.binding.setPosition(position);
        holder.binding.setDateClickListener(this);
        holder.binding.modelForecastDateLabel.setSelected(selectedModelForecastDate == modelForecastDates.get(position));
    }

    @Override
    public int getItemCount() {
        return modelForecastDates.size();
    }

    public void onDateClick(ModelForecastDate modelForecastDate, Integer position) {
        if (modelForecastDate != selectedModelForecastDate) {
            selectedModelForecastDate = modelForecastDate;
            RecyclerViewAdapterModelForecastDate.this.notifyDataSetChanged();
            smoothScroller.setTargetPosition(position);
            recyclerView.getLayoutManager().startSmoothScroll(smoothScroller);
            EventBus.getDefault().post(modelForecastDate);
        }
    }

    private RecyclerView.SmoothScroller getSmoothScroller(RecyclerView recyclerView) {
        return new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            protected int getHorizontalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        static final int LAYOUT_RESOURCE = R.layout.region_forecast_date_layout;
        private final ModelForecastDateView binding;

        ViewHolder(ModelForecastDateView bindingView) {
            super(bindingView.getRoot());
            binding = bindingView;
        }

    }
}