package com.fisincorporated.aviationweather.airportweather;


import android.databinding.BaseObservable;
import android.databinding.DataBindingUtil;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.app.AppPreferences;
import com.fisincorporated.aviationweather.app.ViewModelLifeCycle;
import com.fisincorporated.aviationweather.data.AirportWeather;
import com.fisincorporated.aviationweather.data.common.AviationWeatherResponse;
import com.fisincorporated.aviationweather.data.metars.MetarResponse;
import com.fisincorporated.aviationweather.data.taf.TafResponse;
import com.fisincorporated.aviationweather.databinding.AirportWeatherFragmentBinding;
import com.fisincorporated.aviationweather.messages.DataLoadCompleteEvent;
import com.fisincorporated.aviationweather.messages.DataLoadingEvent;
import com.fisincorporated.aviationweather.retrofit.AviationWeatherApi;
import com.fisincorporated.aviationweather.utils.ViewUtilities;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AirportWeatherViewModel extends BaseObservable implements ViewModelLifeCycle, WeatherDisplayPreferences {

    private static final int MAX_CALLS = 2;

    private int numberCallsComplete = 0;

    private Call<MetarResponse> metarCall;

    private Call<TafResponse> tafCall;

    private AirportWeatherFragmentBinding viewDataBinding;

    private View bindingView;

    private FloatingActionButton fab;

    private ArrayList<AirportWeather> airportWeatherList = new ArrayList<>();

    private boolean displayRawTafMetar;

    private boolean decodeTafMetar;

    private String temperatureUnits;

    private String altitudeUnits;

    private String windSpeedUnits;

    private String distanceUnits;

    @Inject
    public AppPreferences appPreferences;

    @Inject
    public AirportWeatherAdapter airportWeatherAdapter;

    @Inject
    public AviationWeatherApi aviationWeatherApi;

    @Inject
    public AirportWeatherViewModel() {
    }

    public AirportWeatherViewModel setView(View view) {

        bindingView = view.findViewById(R.id.fragment_airport_weather_layout);
        viewDataBinding = DataBindingUtil.bind(bindingView);
        setupRecyclerView(viewDataBinding.fragmentAirportWeatherRecyclerView);
        viewDataBinding.setViewmodel(this);
        fab = viewDataBinding.fragmentAirportWeatherFab;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
            }
        });
        setAirportWeatherOrder();
        airportWeatherAdapter.setAirportWeatherList(airportWeatherList).setWeatherDisplayPreferences(this);
        viewDataBinding.fragmentAirportWeatherRecyclerView.setAdapter(airportWeatherAdapter);
        return this;
    }

    private void setAirportWeatherOrder() {
        AirportWeather airportWeather;
        airportWeatherList.clear();
        String airportList = getAirportCodes();
        String[] airports = airportList.trim().split("\\s+");
        for (int i = 0; i < airports.length; ++i) {
            airportWeather = new AirportWeather();
            airportWeather.setIcaoId(airports[i]);
            airportWeatherList.add(airportWeather);
        }
    }

    public void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
    }

    @Override
    public void onResume() {
        assignDisplayOptions();
        setAirportWeatherOrder();
        airportWeatherAdapter.setAirportWeatherList(airportWeatherList);
        refresh();
    }

    @Override
    public void onPause() {
        if (metarCall != null) {
            metarCall.cancel();
            fireLoadComplete();
        }

        if (tafCall != null) {
            tafCall.cancel();
            fireLoadComplete();
        }

    }

    @Override
    public void onDestroy() {
    }

    private void fireLoadStarted() {
        EventBus.getDefault().post(new DataLoadingEvent());
    }

    private void fireLoadComplete() {
        EventBus.getDefault().post(new DataLoadCompleteEvent());
    }

    public void refresh() {
        fireLoadStarted();
        String airportList = getAirportCodes();
        if (airportList != null & airportList.trim().length() != 0) {
            callForMetar(airportList);
            callForTaf(airportList);
        }
    }

    private void callForMetar(String airportList) {
        metarCall = aviationWeatherApi.mostRecentMetarForEachAirport(airportList, AviationWeatherApi.METAR_HOURS_BEFORE_NOW);
        metarCall.enqueue(new Callback<MetarResponse>() {
            @Override
            public void onResponse(Call<MetarResponse> call, Response<MetarResponse> response) {
                if (isGoodResponse(response)) {
                    airportWeatherAdapter.updateMetarList(response.body().getData().getMetars());
                } else {
                    displayResponseError(response);
                }
                fireLoadComplete();
            }

            @Override
            public void onFailure(Call<MetarResponse> call, Throwable t) {
                displayCallFailure(call, t);
                fireLoadComplete();
            }
        });
    }

    private void callForTaf(String airportList) {
        tafCall = aviationWeatherApi.mostRecentTafForEachAirport(airportList, AviationWeatherApi.TAF_HOURS_BEFORE_NOW);
        tafCall.enqueue(new Callback<TafResponse>() {
            @Override
            public void onResponse(Call<TafResponse> call, Response<TafResponse> response) {
                if (isGoodResponse(response)) {
                    airportWeatherAdapter.updateTafList(response.body().getData().getTAFs());
                } else {
                    displayResponseError(response);
                }
                fireLoadComplete();
            }

            @Override
            public void onFailure(Call<TafResponse> call, Throwable t) {
                displayCallFailure(call, t);
                fireLoadComplete();
            }
        });
    }

    private boolean isGoodResponse(Response<? extends AviationWeatherResponse> response) {
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
        if (t.getCause() != null && !t.getCause().equals("Canceled")) {
            ViewUtilities.displayErrorDialog(bindingView, bindingView.getContext().getString
                    (R.string.oops), t.toString());
        }
    }

    private String getAirportCodes() {
        return appPreferences.getAirportList();
    }

    private void assignDisplayOptions() {
        displayRawTafMetar = appPreferences.isDisplayRawTafMetar();
        decodeTafMetar = appPreferences.isDecodeTafMetar();
        temperatureUnits = appPreferences.getTemperatureDisplay();
        altitudeUnits = appPreferences.getAltitudeDisplay();
        windSpeedUnits = appPreferences.getWindSpeedDisplay();
        distanceUnits = appPreferences.getDistanceUnits();
    }

    public boolean isDisplayRawTafMetar() {
        return displayRawTafMetar;
    }

    public boolean isDecodeTafMetar() {
        return decodeTafMetar;
    }

    public String getAltitudeUnits() {
        return altitudeUnits;
    }

    public String getWindSpeedUnits() {
        return windSpeedUnits;
    }

    public String getDistanceUnits() {
        return distanceUnits;
    }

}
