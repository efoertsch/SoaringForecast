package com.fisincorporated.aviationweather.soaring.forecast.adapters;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.common.recycleradapter.GenericRecyclerViewAdapter;
import com.fisincorporated.aviationweather.databinding.ModelForecastDateView;
import com.fisincorporated.aviationweather.soaring.json.ModelForecastDate;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import timber.log.Timber;

public class ForecastDateRecyclerViewAdapter extends GenericRecyclerViewAdapter<ModelForecastDate, ForecastDateViewHolder> {

    private ForecastDateViewHolder forecastDateViewHolder;

    public ForecastDateRecyclerViewAdapter(List<ModelForecastDate> items) {
        super(items);
    }

    @Override
    public ForecastDateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ModelForecastDateView binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.region_forecast_date_layout, parent, false);
        forecastDateViewHolder = new ForecastDateViewHolder(binding);
        return forecastDateViewHolder;
    }

    private int checkForSelectedDateInNewList(ModelForecastDate modelForecastDate, List<ModelForecastDate> newForecastDates) {
        if (newForecastDates == null || newForecastDates.size() == 0) {
            return -1;
        }
        for (int i = 0; i < newForecastDates.size(); ++i) {
            if (newForecastDates.get(i).getYyyymmddDate().equals(modelForecastDate.getYyyymmddDate())) {
                return i;
            }
        }
        return -1;
    }

    public void updateModelForecastDateList(List<ModelForecastDate> modelForecastDates) {
        int position = -1;
        getItems().clear();
        if (modelForecastDates != null) {
            getItems().addAll(modelForecastDates);
            //if selected date  has been previously set, see if that date is
            // in the new list and if so set selected to same date as previously set
            // if not in list set to first date in list.
            if (getSelectedItem() != null) {
                position = checkForSelectedDateInNewList(getSelectedItem(), modelForecastDates);
            }
            if (position > -1) {
                setSelectedItem(modelForecastDates.get(position));
            } else {
                if (modelForecastDates.size() > 0) {
                    setSelectedItem(modelForecastDates.get(0));
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(ForecastDateViewHolder holder, int position) {
        //super.onBindViewHolder(holder, position);
        holder.viewDataBinding.setModelForecastDate(getItem(position));
        holder.viewDataBinding.setPosition(position);
        //TODO remove following commnet if switching back to this recyclerview adapter
        forecastDateViewHolder.viewDataBinding.setDateClickListener(this);
        forecastDateViewHolder.viewDataBinding.modelForecastDateLabel.setSelected(getSelectedItem() == getItems().get(position));
        Timber.d("Selected date: %1s = date in list: %2s", getSelectedItem().toString(), getItems().get(position).toString() );
        Timber.d("Selected state = %1s", forecastDateViewHolder.viewDataBinding.modelForecastDateLabel.isSelected());
    }

    public void onDateClick(ModelForecastDate modelForecastDate, Integer position) {
        if (modelForecastDate != getSelectedItem()) {
            setSelectedItem(modelForecastDate);
            smoothScrollToPosition(position);
            EventBus.getDefault().post(modelForecastDate);

        }
    }

}
