package com.fisincorporated.soaringforecast.soaring.forecast.adapters;

import com.fisincorporated.soaringforecast.common.recycleradapter.GenericViewHolder;
import com.fisincorporated.soaringforecast.databinding.ModelForecastDateView;
import com.fisincorporated.soaringforecast.soaring.json.ModelForecastDate;

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
