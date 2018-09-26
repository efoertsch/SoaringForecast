package com.fisincorporated.aviationweather.soaring.forecast.adapters;

import com.fisincorporated.aviationweather.common.recycleradapter.GenericViewHolder;
import com.fisincorporated.aviationweather.databinding.ModelForecastDateView;
import com.fisincorporated.aviationweather.soaring.json.ModelForecastDate;

public class ForecastDateViewHolder extends GenericViewHolder<ModelForecastDate, ModelForecastDateView> {

    private ModelForecastDateView viewDataBinding;

    ForecastDateViewHolder(ModelForecastDateView bindingView) {
        super(bindingView);
        viewDataBinding = bindingView;
    }

    public void onBind(ModelForecastDate item, int position) {
        viewDataBinding.setModelForecastDate(item);
        viewDataBinding.setPosition(position);
    }

    @Override
    public ModelForecastDateView getViewDataBinding() {
        return viewDataBinding;
    }

}
