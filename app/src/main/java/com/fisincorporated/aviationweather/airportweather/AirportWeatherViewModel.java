package com.fisincorporated.aviationweather.airportweather;


import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.app.AppPreferences;
import com.fisincorporated.aviationweather.data.AirportWeather;
import com.fisincorporated.aviationweather.data.common.AviationWeatherResponse;
import com.fisincorporated.aviationweather.data.metars.MetarResponse;
import com.fisincorporated.aviationweather.data.taf.TafResponse;
import com.fisincorporated.aviationweather.databinding.AirportWeatherInfoBinding;
import com.fisincorporated.aviationweather.retrofit.AirportMetarService;
import com.fisincorporated.aviationweather.retrofit.AirportTafService;
import com.fisincorporated.aviationweather.utils.ViewUtilities;

import java.util.ArrayList;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AirportWeatherViewModel implements WeatherDisplayPreferences {

    private Call<MetarResponse> metarCall;

    private Call<TafResponse> tafCall;

    public ArrayList<AirportWeather> airportWeatherList = new ArrayList<>();

    private AirportWeatherInfoBinding viewDataBinding;

    private View bindingView;

    public final ObservableBoolean showProgressBar = new ObservableBoolean();

    public final ObservableBoolean displayRawTafMetar = new ObservableBoolean();

    public final ObservableBoolean decodeTafMetar = new ObservableBoolean();

    public final ObservableField<String> temperatureUnits = new ObservableField();

    public final ObservableField<String> altitudeUnits = new ObservableField();

    public final ObservableField<String> windSpeedUnits = new ObservableField();

    public final ObservableField<String> distanceUnits = new ObservableField<>();

    @Inject
    public AppPreferences appPreferences;

    @Inject
    public AirportWeatherAdapter airportWeatherAdapter;

    @Inject
    public AirportMetarService airportMetarService;

    @Inject
    public AirportTafService airportTafService;

    @Inject
    public AirportWeatherViewModel() {
    }

    public AirportWeatherViewModel setView(View view) {
        bindingView = view.findViewById(R.id.activity_weather_view);
        viewDataBinding = DataBindingUtil.bind(bindingView);
        setupRecyclerView(viewDataBinding.activityMetarRecyclerView);
        viewDataBinding.setViewmodel(this);
        airportWeatherAdapter.setAirportWeatherList(airportWeatherList).setWeatherDisplayPreferences(this);
        viewDataBinding.activityMetarRecyclerView.setAdapter(airportWeatherAdapter);
        return this;
    }

    public void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
    }

    public void onResume() {
        assignDisplayOptions();
        refresh();
    }

    public void onPause() {
        if (metarCall != null) {
            metarCall.cancel();
        }

        if (tafCall != null) {
            tafCall.cancel();
        }
    }

    public void refresh() {
        String airportList = getAirportCodes();
        if (airportList != null & airportList.trim().length() != 0) {
            callForMetar(airportList);
            callForTaf(airportList);
        }
    }

    private void callForMetar(String airportList) {

        metarCall = airportMetarService.mostRecentMetarForEachAirport(airportList, 2);

        // Execute the call asynchronously. Get a positive or negative callback.
        metarCall.enqueue(new Callback<MetarResponse>() {
            @Override
            public void onResponse(Call<MetarResponse> call, Response<MetarResponse> response) {
                Log.d("AirportWeatherActivity", "METAR Got response");
                if (isGoodResponse(response)) {
                    airportWeatherAdapter.updateMetarList(response.body().getData().getMetars());
                } else {
                    displayResponseError(response);
                }
                showProgressBar.set(false);
            }

            @Override
            public void onFailure(Call<MetarResponse> call, Throwable t) {
                displayCallFailure(call, t);
                showProgressBar.set(false);
            }
        });
    }

    private void callForTaf(String airportList) {

        tafCall = airportTafService.mostRecentTafForEachAirport(airportList, 7);

        // Execute the call asynchronously. Get a positive or negative callback.
        tafCall.enqueue(new Callback<TafResponse>() {
            @Override
            public void onResponse(Call<TafResponse> call, Response<TafResponse> response) {
                Log.d("AirportWeatherActivity", "TAF Got response");
                if (isGoodResponse(response)) {
                    airportWeatherAdapter.updateTafList(response.body().getData().getTAFs());
                } else {
                    displayResponseError(response);
                }
                showProgressBar.set(false);
            }

            @Override
            public void onFailure(Call<TafResponse> call, Throwable t) {
                displayCallFailure(call, t);
                showProgressBar.set(false);
            }
        });
    }

    private boolean isGoodResponse(Response<? extends AviationWeatherResponse> response){
        return response != null
                && response.body() != null
                && response.body().getErrors() != null && response.body().getErrors().getError() == null;
    }

    private void displayResponseError(Response<? extends AviationWeatherResponse> response) {
        if (response != null && response.body() != null) {
            ViewUtilities.displayErrorDialog(bindingView, bindingView.getContext()
                    .getString(R.string.oops), response.body().getErrors().getError());
        } else {
            ViewUtilities.displayErrorDialog(bindingView, bindingView.getContext()
                    .getString(R.string.oops), bindingView.getContext().getString
                    (R.string.aviation_gov_unspecified_error));
        }
    }

    private void displayCallFailure(Call<? extends AviationWeatherResponse> call, Throwable t) {
        ViewUtilities.displayErrorDialog(bindingView, bindingView.getContext().getString
                (R.string.oops), t.toString());
    }

    private String getAirportCodes() {
        return appPreferences.getAirportList();
    }

    private void assignDisplayOptions() {
        displayRawTafMetar.set(appPreferences.isDisplayRawTafMetar());
        decodeTafMetar.set(appPreferences.isDecodeTafMetar());
        temperatureUnits.set(appPreferences.getTemperatureDisplay());
        altitudeUnits.set(appPreferences.getAltitudeDisplay());
        windSpeedUnits.set(appPreferences.getWindSpeedDisplay());
        distanceUnits.set(appPreferences.getDistanceUnits());
    }

    public ObservableBoolean isDisplayRawTafMetar() {
        return displayRawTafMetar;
    }

    public ObservableBoolean isDecodeTafMetar() {
        return decodeTafMetar;
    }

    public ObservableField<String> getAltitudeUnits() {
        return altitudeUnits;
    }

    public ObservableField<String> getWindSpeedUnits() {
        return windSpeedUnits;
    }

    public ObservableField<String> getDistanceUnits() {
        return distanceUnits;
    }

}
