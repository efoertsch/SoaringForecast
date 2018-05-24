package com.fisincorporated.aviationweather.soaring.forecast;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.databinding.SoaringForecastTypeView;

import java.util.List;

public class RecyclerViewAdapterSoaringForecastType extends RecyclerView.Adapter<RecyclerViewAdapterSoaringForecastType.ViewHolder> {
    private List<SoaringForecastType> soaringForecastTypes;
    public SoaringForecastTypeClickListener soaringForecastTypeClickListener;
    private int selectedPos = RecyclerView.NO_POSITION;

    public RecyclerViewAdapterSoaringForecastType(SoaringForecastTypeClickListener soaringForecastTypeClickListener, List<SoaringForecastType> soaringForecastTypes) {
        this.soaringForecastTypeClickListener = soaringForecastTypeClickListener;
        this.soaringForecastTypes = soaringForecastTypes;
    }

    @Override
    public RecyclerViewAdapterSoaringForecastType.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SoaringForecastTypeView binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), ViewHolder.LAYOUT_RESOURCE, parent, false);
        return new RecyclerViewAdapterSoaringForecastType.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterSoaringForecastType.ViewHolder holder, int position) {
        holder.binding.setSoaringForecastType(soaringForecastTypes.get(position));
        holder.binding.soaringForecastTypeLabel.setTag(soaringForecastTypes.get(position));
        holder.binding.setTypeClickListener(soaringForecastTypeClickListener);
        holder.binding.soaringForecastTypeLabel.setSelected(selectedPos == position);
    }


    @Override
    public int getItemCount() {
        return soaringForecastTypes.size();
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