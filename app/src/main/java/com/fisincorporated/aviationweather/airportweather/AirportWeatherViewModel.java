package com.fisincorporated.aviationweather.airportweather;


import android.databinding.BaseObservable;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.fisincorporated.aviationweather.BR;
import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.app.AppPreferences;
import com.fisincorporated.aviationweather.databinding.ActivityAirportWeatherInfoBinding;
import com.fisincorporated.aviationweather.metars.Metar;
import com.fisincorporated.aviationweather.metars.MetarResponse;
import com.fisincorporated.aviationweather.retrofit.AppRetrofit;
import com.fisincorporated.aviationweather.retrofit.AviationWeatherAPI;
import com.fisincorporated.aviationweather.utils.ConversionUtils;
import com.fisincorporated.aviationweather.utils.ViewUtilities;

import net.droidlabs.mvvm.recyclerview.adapter.binder.ItemBinder;
import net.droidlabs.mvvm.recyclerview.adapter.binder.ItemBinderBase;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AirportWeatherViewModel extends BaseObservable {

    private Call<MetarResponse> metarCall;

    private String airportList;

    public ObservableArrayList<Metar> metars = new ObservableArrayList<>();

    private ActivityAirportWeatherInfoBinding viewDataBinding;

    private View bindingView;

    private final ObservableBoolean showProgressBar = new ObservableBoolean();

    @Inject
    public AppPreferences appPreferences;

    @Inject
    public AirportWeatherViewModel() {
    }

    /**
     * This gets called via xml parms in recylerview in activity_airport_weather_info
     * and binds a metar to a row (airport_metar_taf) in the recyclerview
     */
    public ItemBinder<Metar> itemViewBinder() {
        return new ItemBinderBase<>(BR.metar, R.layout.airport_metar_taf);
    }


    public AirportWeatherViewModel setView(View view) {
        bindingView = view.findViewById(R.id.activity_weather_view);
        viewDataBinding = DataBindingUtil.bind(bindingView);
        setupRecyclerView(viewDataBinding.activityMetarRecyclerView);
        // This binding is to  handle indeterminate progress bar
        viewDataBinding.setMetars(metars);
        // This binding is to handle metar detail (set app:itemViewBinder in xml)
        viewDataBinding.setViewmodel(this);
        return this;
    }

    public AirportWeatherViewModel setAirportList(String airportList) {
        this.airportList = airportList;
        return this;
    }

    public void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
    }

    public void onResume() {
        callForMetar();
    }

    public void onPause() {
        if (metarCall != null) {
            metarCall.cancel();
        }
    }

    public List<Metar> getMetarList() {
        return metars;
    }

    public void callForMetar() {

        airportList = getAirportCodes();

        if (airportList != null & airportList.trim().length() != 0) {
            showProgressBar.set(true);

            AviationWeatherAPI.CurrentMetar client = AppRetrofit.get().create(AviationWeatherAPI
                    .CurrentMetar.class);

            metarCall = client.mostRecentMetarForEachAirport(ConversionUtils.getAirportListForMetars(airportList), 2);

            // Execute the call asynchronously. Get a positive or negative callback.
            metarCall.enqueue(new Callback<MetarResponse>() {
                @Override
                public void onResponse(Call<MetarResponse> call, Response<MetarResponse> response) {
                    Log.d("AirportWeatherActivity", "Got response");
                    if (response != null && response.body() != null && response.body().getErrors
                            () == null) {

                        metars.clear();
                        metars.addAll(response.body().getData().getMetars());
                    } else {
                        if (response != null && response.body() != null) {
                            ViewUtilities.displayErrorDialog(bindingView, bindingView.getContext()
                                    .getString(R.string.oops), response.body().getErrors());
                        } else {
                            ViewUtilities.displayErrorDialog(bindingView, bindingView.getContext()
                                    .getString(R.string.oops), bindingView.getContext().getString
                                    (R.string.aviation_gov_unspecified_error));
                        }
                    }
                    showProgressBar.set(false);
                }

                @Override
                public void onFailure(Call<MetarResponse> call, Throwable t) {
                    ViewUtilities.displayErrorDialog(bindingView, bindingView.getContext().getString
                            (R.string.oops), t.toString());
                    showProgressBar.set(false);
                }
            });
        } else {
            // hide progress spinner as we aren't doing anything.
            showProgressBar.set(false);
        }

    }

    public boolean isShowProgressBar(){
        return showProgressBar.get();
    }

    private String getAirportCodes() {
        return appPreferences.getAirportList(bindingView.getContext());
    }

}
