package com.fisincorporated.aviationweather.soaring.forecast;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.databinding.SoaringForecastTypeView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class RecyclerViewAdapterSoaringForecastModel extends RecyclerView.Adapter<RecyclerViewAdapterSoaringForecastModel.ViewHolder> {
    private List<SoaringForecastModel> soaringForecastModels;
    private int selectedPos = RecyclerView.NO_POSITION;

    public RecyclerViewAdapterSoaringForecastModel(List<SoaringForecastModel> soaringForecastModels) {
        this.soaringForecastModels = soaringForecastModels;
    }

    public void setSelectedPosition(int selectedPos){
        this.selectedPos = selectedPos;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerViewAdapterSoaringForecastModel.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SoaringForecastTypeView binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), ViewHolder.LAYOUT_RESOURCE, parent, false);
        ViewHolder viewHolder = new RecyclerViewAdapterSoaringForecastModel.ViewHolder(binding);

        viewHolder.itemView.setOnClickListener(v -> {
            SoaringForecastModel soaringForecastModel = soaringForecastModels.get(viewHolder.getAdapterPosition());
            notifyItemChanged(selectedPos);
            selectedPos = viewHolder.getLayoutPosition();
            notifyItemChanged(selectedPos);
            EventBus.getDefault().post(soaringForecastModel);
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterSoaringForecastModel.ViewHolder holder, int position) {
        holder.binding.setSoaringForecastModel(soaringForecastModels.get(position));
        holder.binding.soaringForecastTypeLabel.setTag(soaringForecastModels.get(position));
        holder.binding.soaringForecastTypeLabel.setSelected(selectedPos == position);
    }


    @Override
    public int getItemCount() {
        return soaringForecastModels.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        static final int LAYOUT_RESOURCE = R.layout.soaring_forecast_type_layout;

        private final SoaringForecastTypeView binding;

        public ViewHolder(SoaringForecastTypeView bindingView) {
            super(bindingView.getRoot());
            binding = bindingView;
        }

    }
}