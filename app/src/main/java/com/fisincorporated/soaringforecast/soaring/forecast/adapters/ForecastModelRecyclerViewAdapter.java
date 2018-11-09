package com.fisincorporated.soaringforecast.soaring.forecast.adapters;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.common.recycleradapter.GenericRecyclerViewAdapter;
import com.fisincorporated.soaringforecast.databinding.SoaringForecastTypeView;
import com.fisincorporated.soaringforecast.soaring.forecast.SoaringForecastModel;

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
        if(getSelectedItem() != null) {
            holder.getViewDataBinding().soaringForecastModelLabel.setSelected(getSelectedItem().equals(getItems().get(position)));
            if (getSelectedItem().equals(getItems().get(position))){
                smoothScrollToPosition(position);
            }
        }
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
