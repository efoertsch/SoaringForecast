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
import com.fisincorporated.aviationweather.data.metars.Metar;
import com.fisincorporated.aviationweather.data.metars.MetarResponse;
import com.fisincorporated.aviationweather.data.taf.TAF;
import com.fisincorporated.aviationweather.data.taf.TafResponse;
import com.fisincorporated.aviationweather.databinding.ActivityAirportWeatherInfoBinding;
import com.fisincorporated.aviationweather.retrofit.AppRetrofit;
import com.fisincorporated.aviationweather.retrofit.AviationWeatherApis;
import com.fisincorporated.aviationweather.utils.ViewUtilities;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AirportWeatherViewModel implements WeatherDisplayPreferences {

    private Call<MetarResponse> metarCall;

    private Call<TafResponse> tafCall;

    private String airportList;

    public ArrayList<Metar> metars = new ArrayList<>();

    public ArrayList<TAF> tafs = new ArrayList<>();

    private ActivityAirportWeatherInfoBinding viewDataBinding;

    private View bindingView;

    public final ObservableBoolean showProgressBar = new ObservableBoolean();

    public final ObservableBoolean rawMetarDisplay = new ObservableBoolean();

    public final ObservableField<String> temperatureUnits = new ObservableField();

    public final ObservableField<String> altitudeUnits = new ObservableField();

    public final ObservableField<String> windSpeedUnits = new ObservableField();

    public final ObservableField<String> distanceUnits = new ObservableField<>();

    @Inject
    public AppPreferences appPreferences;

    @Inject
    public AirportWeatherAdapter airportWeatherAdapter;

    @Inject
    public AirportWeatherViewModel() {}

    public AirportWeatherViewModel setView(View view) {
        bindingView = view.findViewById(R.id.activity_weather_view);
        viewDataBinding = DataBindingUtil.bind(bindingView);
        setupRecyclerView(viewDataBinding.activityMetarRecyclerView);
        // This binding is to handle metar detail (set app:itemViewBinder in xml)
        viewDataBinding.setViewmodel(this);
        // Data to recyclerViewAdapter
        airportWeatherAdapter.setMetarList(metars).setWeatherDisplayPreferences(this);
        viewDataBinding.activityMetarRecyclerView.setAdapter(airportWeatherAdapter);

        return this;
    }

    public WeatherDisplayPreferences setAirportList(String airportList) {
        this.airportList = airportList;
        return this;
    }

    public void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
    }

    public void onResume() {
        assignDisplayOptions();
        callForMetar();
        callForTaf();
    }

    public void onPause() {
        if (metarCall != null) {
            metarCall.cancel();
        }

        if (tafCall != null) {
            tafCall.cancel();
        }
    }

    public List<Metar> getMetarList() {
        return metars;
    }

    public void callForMetar() {
        airportList = getAirportCodes();
        if (airportList != null & airportList.trim().length() != 0) {
            showProgressBar.set(true);

            AviationWeatherApis client = AppRetrofit.get().create(AviationWeatherApis.class);

            metarCall = client.mostRecentMetarForEachAirport(airportList, 2);

            // Execute the call asynchronously. Get a positive or negative callback.
            metarCall.enqueue(new Callback<MetarResponse>() {
                @Override
                public void onResponse(Call<MetarResponse> call, Response<MetarResponse> response) {
                    Log.d("AirportWeatherActivity", "METAR Got response");
                    if (response != null && response.body() != null && response.body().getErrors
                            () == null) {
                        airportWeatherAdapter.updateMetarList(response.body().getData().getMetars());
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

    public void callForTaf() {
        airportList = getAirportCodes();
        if (airportList != null & airportList.trim().length() != 0) {
            showProgressBar.set(true);

            AviationWeatherApis client = AppRetrofit.get().create(AviationWeatherApis.class);

            tafCall = client.mostRecentTafForEachAirport(airportList, 7);

            // Execute the call asynchronously. Get a positive or negative callback.
            tafCall.enqueue(new Callback<TafResponse>() {
                @Override
                public void onResponse(Call<TafResponse> call, Response<TafResponse> response) {
                    Log.d("AirportWeatherActivity", "TAF Got response");
                    if (response != null && response.body() != null && response.body().getErrors
                            () == null) {
                        airportWeatherAdapter.updateTafList(response.body().getData().getTAFs());
                    } else {
                        if (response != null && response.body() != null) {
                            ViewUtilities.displayErrorDialog(bindingView, bindingView.getContext()
                                    .getString(R.string.oops), response.body().getErrors().getError());
                        } else {
                            ViewUtilities.displayErrorDialog(bindingView, bindingView.getContext()
                                    .getString(R.string.oops), bindingView.getContext().getString
                                    (R.string.aviation_gov_unspecified_error));
                        }
                    }
                    showProgressBar.set(false);
                }

                @Override
                public void onFailure(Call<TafResponse> call, Throwable t) {
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

    private String getAirportCodes() {
        return appPreferences.getAirportList();
    }

    private void assignDisplayOptions() {
        rawMetarDisplay.set(appPreferences.getDisplayRawMetar());
        temperatureUnits.set(appPreferences.getTemperatureDisplay());
        altitudeUnits.set(appPreferences.getAltitudeDisplay());
        windSpeedUnits.set(appPreferences.getWindSpeedDisplay());
        distanceUnits.set(appPreferences.getDistanceUnits());
    }

    public ObservableBoolean getRawMetarDisplay() {
        return rawMetarDisplay;
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
