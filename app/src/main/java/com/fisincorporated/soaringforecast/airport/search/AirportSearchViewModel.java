package com.fisincorporated.soaringforecast.airport.search;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.fisincorporated.soaringforecast.repository.Airport;
import com.fisincorporated.soaringforecast.repository.AppRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class AirportSearchViewModel extends ViewModel {

        private MutableLiveData<List<Airport>> airports = new MutableLiveData<>();
        private AppRepository appRepository;

        public AirportSearchViewModel setAppRepository(AppRepository appRepository) {
            this.appRepository = appRepository;
            airports = new MutableLiveData<>();
            airports.setValue(new ArrayList<>());
            return this;
        }

        @SuppressLint("CheckResult")
        public LiveData<List<Airport>> searchAirports(String search) {
            appRepository.findAirports("%" + search + "%")
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
