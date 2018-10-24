package com.fisincorporated.soaringforecast.soaring.forecast.adapters;


import com.fisincorporated.soaringforecast.common.recycleradapter.GenericViewHolder;
import com.fisincorporated.soaringforecast.databinding.SoaringForecastView;
import com.fisincorporated.soaringforecast.soaring.json.Forecast;

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
