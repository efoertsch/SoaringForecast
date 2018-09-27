package com.fisincorporated.aviationweather.soaring.forecast.adapters;


import com.fisincorporated.aviationweather.common.recycleradapter.GenericViewHolder;
import com.fisincorporated.aviationweather.databinding.SoaringForecastView;
import com.fisincorporated.aviationweather.soaring.json.Forecast;

public class SoaringForecastViewHolder extends GenericViewHolder<Forecast,SoaringForecastView> {

    private SoaringForecastView viewDataBinding;

    SoaringForecastViewHolder(SoaringForecastView bindingView) {
        super(bindingView);
        viewDataBinding = bindingView;
    }

    @Override
    public void onBind(Forecast item, int position) {
        viewDataBinding.setForecast(item);
        viewDataBinding.setPosition(position);
    }

    @Override
    public SoaringForecastView getViewDataBinding() {
        return viewDataBinding;
    }
}
