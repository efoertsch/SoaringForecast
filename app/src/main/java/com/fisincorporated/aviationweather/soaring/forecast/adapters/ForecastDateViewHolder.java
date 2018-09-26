package com.fisincorporated.aviationweather.soaring.forecast.adapters;

import com.fisincorporated.aviationweather.common.recycleradapter.BaseViewHolder;
import com.fisincorporated.aviationweather.databinding.ModelForecastDateView;
import com.fisincorporated.aviationweather.soaring.json.ModelForecastDate;

public class ForecastDateViewHolder extends BaseViewHolder<ModelForecastDate, ModelForecastDateView> {

    ModelForecastDateView viewDataBinding;

    ForecastDateViewHolder(ModelForecastDateView bindingView) {
        super(bindingView);
        viewDataBinding = bindingView;
    }

    public void onBind(ModelForecastDate item, int position) {
        viewDataBinding.setModelForecastDate(item);
        viewDataBinding.setPosition(position);
    }

}
