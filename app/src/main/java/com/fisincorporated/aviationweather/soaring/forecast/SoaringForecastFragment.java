package com.fisincorporated.aviationweather.soaring.forecast;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class SoaringForecastFragment extends DaggerFragment {

    public static final String MASTER_FORECAST_FRAGMENT = "MASTER_FORECAST_FRAGMENT";

    @Inject
    SoaringForecastViewModel soaringForecastViewModel;

    public SoaringForecastFragment() {}

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.soaring_forecast_fragment, container, false);
        soaringForecastViewModel.setView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        soaringForecastViewModel.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        soaringForecastViewModel.onPause();

    }
}
