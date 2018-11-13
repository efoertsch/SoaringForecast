package com.fisincorporated.soaringforecast.airport.list;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.repository.Airport;
import com.fisincorporated.soaringforecast.repository.AppRepository;

import java.util.Collections;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class AirportListViewModel extends ViewModel {

    private MutableLiveData<List<Airport>> airports;
    private AppRepository appRepository;
    private AppPreferences appPreferences;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public AirportListViewModel setRepositoryAndPreferences(AppRepository appRepository, AppPreferences appPreferences) {
        this.appRepository = appRepository;
        this.appPreferences = appPreferences;
        return this;
    }

    public MutableLiveData<List<Airport>> getSelectedAirports(){
        if (airports == null) {
            airports = new MutableLiveData<>();
            refreshSelectedAirportsList();
        }
        return airports;
    }

    public void refreshSelectedAirportsList() {
        listSelectedAirports(appPreferences.getSelectedAirportCodesList());
    }

    @SuppressLint("CheckResult")
    public void listSelectedAirports( List<String> icaoIds) {
        Disposable disposable = appRepository.getAirportsByIcaoIdAirports(icaoIds)
                .subscribe(airportList -> {
                            airports.setValue(sortAirports(icaoIds, airportList));
                        },
                        t -> {
                            Timber.e(t);
                        });
        compositeDisposable.add(disposable);
    }



    private List<Airport> sortAirports(List<String> icaoIds, List<Airport> airports) {
        // airports may not be in preferred order so order them now
        for (int i = 0; i < icaoIds.size(); ++i) {
            for (int j = 0; j < airports.size(); ++j) {
                if (icaoIds.get(i).equalsIgnoreCase(airports.get(j).getIdent())
                        && i != j && i < j) {
                    Collections.swap(airports, i, j);
                }
            }
        }
        return airports;
    }

    // Get list of all airports in database
    @SuppressLint("CheckResult")
    public LiveData<List<Airport>> listAirports() {
        Disposable disposable = appRepository.listAllAirports()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(airportList -> {
                            airports.setValue(airportList);
                        },
                        t -> {
                            Timber.e(t);
                        });
        compositeDisposable.add(disposable);
        return airports;
    }

    @Override
    public void onCleared(){
        compositeDisposable.dispose();
        super.onCleared();
    }


}
