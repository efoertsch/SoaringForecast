package com.fisincorporated.aviationweather.soaring.forecast;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.databinding.SoaringForecastTypeView;

import java.util.List;

public class RecyclerViewAdapterSoaringForecastType extends RecyclerView.Adapter<RecyclerViewAdapterSoaringForecastType.ViewHolder> {
    private List<SoaringForecastModel> soaringForecastModels;
    public SoaringForecastModelClickListener soaringForecastModelClickListener;
    private int selectedPos = RecyclerView.NO_POSITION;

    public RecyclerViewAdapterSoaringForecastType(SoaringForecastModelClickListener soaringForecastModelClickListener, List<SoaringForecastModel> soaringForecastModels) {
        this.soaringForecastModelClickListener = soaringForecastModelClickListener;
        this.soaringForecastModels = soaringForecastModels;
    }

    @Override
    public RecyclerViewAdapterSoaringForecastType.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SoaringForecastTypeView binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), ViewHolder.LAYOUT_RESOURCE, parent, false);
        return new RecyclerViewAdapterSoaringForecastType.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterSoaringForecastType.ViewHolder holder, int position) {
        holder.binding.setSoaringForecastModel(soaringForecastModels.get(position));
        holder.binding.soaringForecastTypeLabel.setTag(soaringForecastModels.get(position));
        holder.binding.setTypeClickListener(soaringForecastModelClickListener);
        holder.binding.soaringForecastTypeLabel.setSelected(selectedPos == position);
    }


    @Override
    public int getItemCount() {
        return soaringForecastModels.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        static final int LAYOUT_RESOURCE = R.layout.soaring_forecast_type_layout;

        private final SoaringForecastTypeView  binding;

        public ViewHolder(SoaringForecastTypeView  bindingView) {
            super(bindingView.getRoot());
            binding = bindingView;
        }

    }
}