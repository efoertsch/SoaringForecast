package com.fisincorporated.aviationweather.soaring.forecast.adapters;

import com.fisincorporated.aviationweather.common.recycleradapter.GenericViewHolder;
import com.fisincorporated.aviationweather.databinding.SoaringForecastTypeView;
import com.fisincorporated.aviationweather.soaring.forecast.SoaringForecastModel;

public class ForecastModelViewHolder extends GenericViewHolder<SoaringForecastModel,SoaringForecastTypeView> {

    private SoaringForecastTypeView viewDataBinding;

    ForecastModelViewHolder(SoaringForecastTypeView bindingView){
        super(bindingView);
        viewDataBinding = bindingView;
    }

    @Override
    public void onBind(SoaringForecastModel item, int position) {
        viewDataBinding.setSoaringForecastModel(item);
        viewDataBinding.setPosition(position);

    }

    @Override
    public SoaringForecastTypeView getViewDataBinding() {
        return viewDataBinding;
    }
}
