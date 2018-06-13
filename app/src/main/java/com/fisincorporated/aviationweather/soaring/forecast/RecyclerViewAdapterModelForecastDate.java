package com.fisincorporated.aviationweather.soaring.forecast;

import android.databinding.DataBindingUtil;
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
    private ModelForecastDateClickListener modelForecastDateClickListener;
    private int selectedPos = RecyclerView.NO_POSITION;


    public RecyclerViewAdapterModelForecastDate(List<ModelForecastDate> modelForecastDates) {
        this.modelForecastDateClickListener = modelForecastDateClickListener;
        this.modelForecastDates = modelForecastDates;
    }

    public void updateModelForecastDateList(List<ModelForecastDate> modelForecastDates) {
        this.modelForecastDates = modelForecastDates;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerViewAdapterModelForecastDate.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ModelForecastDateView binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), ViewHolder.LAYOUT_RESOURCE, parent, false);
        ViewHolder viewHolder = new RecyclerViewAdapterModelForecastDate.ViewHolder(binding);
        viewHolder.itemView.setOnClickListener(v -> {
            ModelForecastDate modelForecastDate = modelForecastDates.get(viewHolder.getAdapterPosition());
            notifyItemChanged(selectedPos);
            selectedPos = viewHolder.getLayoutPosition();
            notifyItemChanged(selectedPos);
            EventBus.getDefault().post(modelForecastDate);
        });
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterModelForecastDate.ViewHolder holder, int position) {
        holder.binding.setModelForecastDate(modelForecastDates.get(position));
        holder.binding.modelForecastDateLabel.setTag(modelForecastDates.get(position));
        holder.binding.modelForecastDateLabel.setSelected(selectedPos == position);
    }

    @Override
    public int getItemCount() {
        return modelForecastDates.size();
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