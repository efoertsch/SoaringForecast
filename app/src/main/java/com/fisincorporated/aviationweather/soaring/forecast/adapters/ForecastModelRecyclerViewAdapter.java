package com.fisincorporated.aviationweather.soaring.forecast.adapters;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.common.recycleradapter.GenericRecyclerViewAdapter;
import com.fisincorporated.aviationweather.databinding.SoaringForecastTypeView;
import com.fisincorporated.aviationweather.soaring.forecast.SoaringForecastModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class ForecastModelRecyclerViewAdapter extends GenericRecyclerViewAdapter<SoaringForecastModel,ForecastModelViewHolder> {

    private List<SoaringForecastModel> soaringForecastModels;

    public ForecastModelRecyclerViewAdapter( List<SoaringForecastModel> items){
        super(items);
    }

    @Override
    public ForecastModelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SoaringForecastTypeView binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.soaring_forecast_model_layout, parent, false);
        return new ForecastModelViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ForecastModelViewHolder holder, int position) {
        super.onBindViewHolder(holder,position);
        holder.getViewDataBinding().setForecastModelClickListener(this);
        holder.getViewDataBinding().soaringForecastModelLabel.setSelected(getSelectedItem().equals(getItems().get(position)));
    }

    public void setSelectedForecastModel(SoaringForecastModel soaringForecastModel) {
       setSelectedItem(soaringForecastModel);
       notifyDataSetChanged();
    }

    public void onModelClick(SoaringForecastModel soaringForecastModel, Integer position) {
        if (!soaringForecastModel.equals(getSelectedItem())) {
            setSelectedItem(soaringForecastModel);
            smoothScrollToPosition(position);
            EventBus.getDefault().post(soaringForecastModel);
        }
    }
}
