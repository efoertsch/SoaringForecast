package com.fisincorporated.soaringforecast.soaring.forecast.adapters;

import com.fisincorporated.soaringforecast.common.recycleradapter.GenericViewHolder;
import com.fisincorporated.soaringforecast.databinding.SoaringForecastTypeView;
import com.fisincorporated.soaringforecast.soaring.forecast.SoaringForecastModel;

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
