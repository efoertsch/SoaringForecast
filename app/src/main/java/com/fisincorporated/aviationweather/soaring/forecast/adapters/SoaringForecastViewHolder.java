package com.fisincorporated.aviationweather.soaring.forecast.adapters;

import com.fisincorporated.aviationweather.common.recycleradapter.GenericViewHolder;
import com.fisincorporated.aviationweather.databinding.SoaringConditionView;
import com.fisincorporated.aviationweather.soaring.json.Forecast;

public class SoaringForecastViewHolder extends GenericViewHolder<Forecast,SoaringConditionView> {

    private SoaringConditionView viewDataBinding;

    SoaringForecastViewHolder(SoaringConditionView bindingView) {
        super(bindingView);
        viewDataBinding = bindingView;
    }

    @Override
    public void onBind(Forecast item, int position) {
        viewDataBinding.setForecast(item);
        viewDataBinding.setPosition(position);
    }

    @Override
    public SoaringConditionView getViewDataBinding() {
        return viewDataBinding;
    }
}
