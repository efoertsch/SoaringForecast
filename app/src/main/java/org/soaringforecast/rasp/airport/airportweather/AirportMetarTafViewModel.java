package org.soaringforecast.rasp.airport.airportweather;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.common.ObservableViewModel;
import org.soaringforecast.rasp.data.AirportMetarTaf;
import org.soaringforecast.rasp.data.common.AviationWeatherResponse;
import org.soaringforecast.rasp.data.metars.Metar;
import org.soaringforecast.rasp.data.metars.MetarResponse;
import org.soaringforecast.rasp.data.taf.TAF;
import org.soaringforecast.rasp.data.taf.TafResponse;
import org.soaringforecast.rasp.common.messages.CallFailure;
import org.soaringforecast.rasp.retrofit.messages.ResponseError;
import org.soaringforecast.rasp.repository.Airport;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.retrofit.AviationWeatherApi;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

//TODO clean up and execute api calls in more RxJava sort of way
public class AirportMetarTafViewModel extends ObservableViewModel implements WeatherMetarTafPreferences {

    private AppPreferences appPreferences;
    private AppRepository appRepository;

    public AviationWeatherApi aviationWeatherApi;
    private Call<MetarResponse> metarCall;
    private Call<TafResponse> tafCall;

    private MutableLiveData<List<AirportMetarTaf>> airportMetarTafs;
    private MutableLiveData<List<TAF>> tafList;
    private MutableLiveData<List<Metar>> metarList;
    private MutableLiveData<List<Airport>> airportList;

    // used for display of Metar/Taf
    private boolean displayRawTafMetar;
    private boolean decodeTafMetar;
    private String temperatureUnits;
    private String altitudeUnits;
    private String windSpeedUnits;
    private String distanceUnits;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public AirportMetarTafViewModel(@NonNull Application application) {
        super(application);
    }

    public AirportMetarTafViewModel setAppPreferences(AppPreferences appPreferences) {
        this.appPreferences = appPreferences;
        return this;
    }

    public AirportMetarTafViewModel setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        return this;
    }

    public AirportMetarTafViewModel setAviationWeaterApi(AviationWeatherApi aviationWeatherApi) {
        this.aviationWeatherApi = aviationWeatherApi;
        return this;
    }

    public LiveData<List<AirportMetarTaf>> getAirportMetarTafs() {
        if (airportMetarTafs == null) {
            airportMetarTafs = new MutableLiveData<>();
            airportMetarTafs.setValue(new ArrayList<>());
            assignDisplayOptions();
            setAirportWeatherOrder();
            callForAirportNames();
        }
        return airportMetarTafs;
    }

    public MutableLiveData<List<Airport>> getAirportList() {
        if (airportList == null) {
            airportList = new MutableLiveData<>();
            airportList.setValue(new ArrayList<>());
        }
        return airportList;
    }

    public LiveData<List<TAF>> getAirportTaf() {
        if (tafList == null) {
            tafList = new MutableLiveData<>();
            tafList.setValue(new ArrayList<>());
        }
        return tafList;
    }

    public LiveData<List<Metar>> getAirportMetars() {
        if (metarList == null) {
            metarList = new MutableLiveData<>();
            metarList.setValue(new ArrayList<>());
        }
        return metarList;
    }

    // Order display of metar/tafs in same order as in list
    private void setAirportWeatherOrder() {
        AirportMetarTaf airportMetarTaf;
        List<AirportMetarTaf> newAirportMetarTafList = new ArrayList<>();
        String airportList = getAirportCodes();
        String[] airports = airportList.trim().split("\\s+");
        for (int i = 0; i < airports.length; ++i) {
            airportMetarTaf = new AirportMetarTaf();
            airportMetarTaf.setIcaoId(airports[i]);
            newAirportMetarTafList.add(airportMetarTaf);
        }
        airportMetarTafs.setValue(newAirportMetarTafList);

    }

    private String getAirportCodes() {
        return appPreferences.getAirportList();
    }

    public void refresh() {
        String airportCodeList = getAirportCodes();
        assignDisplayOptions();
        if (airportCodeList == null || airportCodeList.trim().length() == 0) {
            airportMetarTafs.setValue(new ArrayList<>());
            tafList.setValue(new ArrayList<>());
            metarList.setValue(new ArrayList<>());
            airportList.setValue((new ArrayList<>()));

        } else {
            setAirportWeatherOrder();
            callForMetar(airportCodeList);
            callForTaf(airportCodeList);
            callForAirportNames();
        }
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

    private void callForAirportNames() {
        Disposable disposable = appRepository.getAirportsByIcaoIdAirports(appPreferences.getSelectedAirportCodesList())
                .subscribe(airportList -> {
                            this.airportList.setValue(airportList);
                        },
                        t -> {
                            //TODO display/report error
                            Timber.e(t);
                        });
        compositeDisposable.add(disposable);
    }

    private void callForMetar(String airportList) {
        metarCall = aviationWeatherApi.mostRecentMetarForEachAirport(airportList, AviationWeatherApi.METAR_HOURS_BEFORE_NOW);
        metarCall.enqueue(new Callback<MetarResponse>() {
            @Override
            public void onResponse(Call<MetarResponse> call, Response<MetarResponse> response) {
                if (isGoodResponse(response)) {
                    metarList.setValue(response.body().getData().getMetars());
                } else {
                    displayResponseError(response);
                }
            }

            @Override
            public void onFailure(Call<MetarResponse> call, Throwable t) {
                displayCallFailure(call, t);
            }
        });
    }

    private void callForTaf(String airportList) {
        tafCall = aviationWeatherApi.mostRecentTafForEachAirport(airportList, AviationWeatherApi.TAF_HOURS_BEFORE_NOW);
        tafCall.enqueue(new Callback<TafResponse>() {
            @Override
            public void onResponse(Call<TafResponse> call, Response<TafResponse> response) {
                if (isGoodResponse(response)) {
                    tafList.setValue(response.body().getData().getTAFs());
                } else {
                    displayResponseError(response);
                }
            }

            @Override
            public void onFailure(Call<TafResponse> call, Throwable t) {
                displayCallFailure(call, t);
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
            EventBus.getDefault().post(new ResponseError(response.body().getErrors().getError()));

        } else {
            EventBus.getDefault().post(new ResponseError(getApplication().getString(R.string.aviation_gov_unspecified_error)));
        }
    }

    private void displayCallFailure(Call<? extends AviationWeatherResponse> call, Throwable t) {
        EventBus.getDefault().post(new CallFailure(t.toString()));

    }

    @Override
    public void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }
}
