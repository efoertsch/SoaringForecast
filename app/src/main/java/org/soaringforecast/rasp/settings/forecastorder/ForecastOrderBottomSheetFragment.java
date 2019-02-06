package org.soaringforecast.rasp.settings.forecastorder;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.databinding.ForecastOrderBottomSheetView;
import org.soaringforecast.rasp.soaring.json.Forecast;

public class ForecastOrderBottomSheetFragment extends BottomSheetDialogFragment {

    private Forecast forecast;

    public static ForecastOrderBottomSheetFragment newInstance(Forecast forecast) {
        ForecastOrderBottomSheetFragment forecastOrderBottomSheetFragment
                = new ForecastOrderBottomSheetFragment();
        forecastOrderBottomSheetFragment.forecast = forecast;
        return forecastOrderBottomSheetFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        ForecastOrderBottomSheetView forecastOrderBottomSheetView =
                DataBindingUtil.inflate(inflater, R.layout.forecast_order_bottom_sheet_fragment, container, false);
        forecastOrderBottomSheetView.setLifecycleOwner(this);
        forecastOrderBottomSheetView.setForecast(forecast);
        forecastOrderBottomSheetView.forecastOrderForecastDescription.setMovementMethod(new ScrollingMovementMethod());
        return forecastOrderBottomSheetView.getRoot();

    }
}
