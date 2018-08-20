package com.fisincorporated.aviationweather.airport.list;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.fisincorporated.aviationweather.repository.Airport;
import com.fisincorporated.aviationweather.repository.AppRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class AirportListViewModel extends ViewModel {

    private MutableLiveData<List<Airport>> airports = new MutableLiveData<>();
    private AppRepository appRepository;

    public AirportListViewModel setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        airports = new MutableLiveData<>();
        airports.setValue(new ArrayList<>());
        return this;
    }

    @SuppressLint("CheckResult")
    public LiveData<List<Airport>> listAirports() {
            appRepository.listAllAirports()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(airportList -> {
                                airports.setValue(airportList);
                            },
                            t -> {
                                Timber.e(t);
                            });
        return airports;
    }

    @SuppressLint("CheckResult")
    public LiveData<List<Airport>> listSelectedAirports(List<String> icaoIds) {
        appRepository.selectIcaoIdAirports(icaoIds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(airportList -> {
                            airports.setValue(airportList);
                        },
                        t -> {
                            Timber.e(t);
                        });
        return airports;
    }

}
