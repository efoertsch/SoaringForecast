package org.soaringforecast.rasp.airport.search;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.repository.Airport;
import org.soaringforecast.rasp.repository.AppRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class AirportSearchViewModel extends ViewModel {


    private AppRepository appRepository;
    private AppPreferences appPreferences;

    private MutableLiveData<List<Airport>> searchAirports;
    private Disposable searchDisposable;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public AirportSearchViewModel setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        return this;
    }

    public AirportSearchViewModel setAppPreferences(AppPreferences appPreferences) {
        this.appPreferences = appPreferences;
        return this;
    }

    public LiveData<List<Airport>> getSearchAirports(String search) {
        if (searchAirports == null){
            searchAirports = new MutableLiveData<>();
            searchAirports.setValue(new ArrayList<>());
        }
        return searchAirports(search);
    }

    @SuppressLint("CheckResult")
    public LiveData<List<Airport>> searchAirports(String search) {
        if (search == null || search.isEmpty()) {
            searchAirports.setValue(null);
        } else {
            if (searchDisposable != null) {
                compositeDisposable.delete(searchDisposable);
            }
            searchDisposable = appRepository.findAirports("%" + search + "%")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(airportList -> {
                                searchAirports.setValue(airportList);
                            },
                            t -> {
                                Timber.e(t);
                            });
            compositeDisposable.add(searchDisposable);
        }
        return searchAirports;
    }

    public void addAirportIcaoCodeToSelectedAirports(String icaoId) {
        appPreferences.addAirportCodeToSelectedIcaoCodes(icaoId);
    }

    @Override
    public void onCleared(){
        compositeDisposable.dispose();
        super.onCleared();
    }

}
