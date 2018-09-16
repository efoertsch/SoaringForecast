package com.fisincorporated.aviationweather.soaring.forecast;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.databinding.SoaringForecastTypeView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class RecyclerViewAdapterForecastModel extends RecyclerView.Adapter<RecyclerViewAdapterForecastModel.ViewHolder> {
    private List<SoaringForecastModel> soaringForecastModels;
    private SoaringForecastModel selectedSoaringForecastModel;
    private RecyclerView recyclerView;
    private RecyclerView.SmoothScroller smoothScroller;

    RecyclerViewAdapterForecastModel(List<SoaringForecastModel> soaringForecastModels) {
        this.soaringForecastModels = soaringForecastModels;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        smoothScroller = getSmoothScroller(recyclerView);

    }

    public void setSelectedForecastModel(SoaringForecastModel soaringForecastModel) {
        selectedSoaringForecastModel = soaringForecastModel;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerViewAdapterForecastModel.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SoaringForecastTypeView binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), ViewHolder.LAYOUT_RESOURCE, parent, false);
        return new RecyclerViewAdapterForecastModel.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterForecastModel.ViewHolder holder, int position) {
        holder.binding.setSoaringForecastModel(soaringForecastModels.get(position));
        holder.binding.setPosition(position);
        holder.binding.setForecastModelClickListener(this);
        holder.binding.soaringForecastModelLabel.setSelected(selectedSoaringForecastModel == soaringForecastModels.get(position));

    }

    @Override
    public int getItemCount() {
        return soaringForecastModels.size();
    }

    public void onModelClick(SoaringForecastModel soaringForecastModel, Integer position) {
        if (soaringForecastModel != selectedSoaringForecastModel) {
            selectedSoaringForecastModel = soaringForecastModel;
            recyclerView.getLayoutManager().scrollToPosition(position);
            RecyclerViewAdapterForecastModel.this.notifyDataSetChanged();
            smoothScroller.setTargetPosition(position);
            recyclerView.getLayoutManager().startSmoothScroll(smoothScroller);
            EventBus.getDefault().post(soaringForecastModel);
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
        static final int LAYOUT_RESOURCE = R.layout.soaring_forecast_model_layout;
        private final SoaringForecastTypeView binding;

        public ViewHolder(SoaringForecastTypeView bindingView) {
            super(bindingView.getRoot());
            binding = bindingView;
        }
    }
}