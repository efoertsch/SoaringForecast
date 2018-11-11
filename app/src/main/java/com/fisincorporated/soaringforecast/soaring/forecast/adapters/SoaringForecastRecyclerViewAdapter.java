package com.fisincorporated.soaringforecast.soaring.forecast.adapters;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.common.recycleradapter.GenericRecyclerViewAdapter;
import com.fisincorporated.soaringforecast.databinding.SoaringForecastView;
import com.fisincorporated.soaringforecast.soaring.json.Forecast;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class SoaringForecastRecyclerViewAdapter extends GenericRecyclerViewAdapter<Forecast,SoaringForecastViewHolder> {


    public SoaringForecastRecyclerViewAdapter(List<Forecast> items) {
        super(items);
    }

    @Override
    public SoaringForecastViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SoaringForecastView binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.soaring_forecast_option, parent, false);
        return new SoaringForecastViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(SoaringForecastViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        //TODO get better way to do following
        holder.getViewDataBinding().soaringForecastLabel.setSelected(getItems().get(position).equals(getSelectedItem()));
        holder.getViewDataBinding().setForecastClickListener(this);
    }

    public void onForecastClick(Forecast forecast, Integer position) {
        if (forecast.equals(getSelectedItem())) {
            return;
        }
        if (forecast.getForecastType().equalsIgnoreCase("comment")) {
            return;
        }
        setSelectedItem(forecast);
        notifyDataSetChanged();
        smoothScrollToPosition(position);
        EventBus.getDefault().post(forecast);
    }
}
