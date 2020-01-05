package org.soaringforecast.rasp.settings.forecastorder;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.soaring.json.Forecast;
import org.soaringforecast.rasp.soaring.json.Forecasts;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ForecastOrderViewModel extends ViewModel implements ForecastOrderAdapter.NewForecastOrderListener {

    private MutableLiveData<List<Forecast>> orderedForecasts;
    private AppRepository appRepository;
    private AppPreferences appPreferences;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public ForecastOrderViewModel setRepositoryAndPreferences(AppRepository appRepository, AppPreferences appPreferences) {
        this.appRepository = appRepository;
        this.appPreferences = appPreferences;
        return this;
    }

    public MutableLiveData<List<Forecast>> getOrderedForecasts() {
        if (orderedForecasts == null) {
            orderedForecasts = new MutableLiveData<>();
            listOrderedForecasts();
        }
        return orderedForecasts;
    }

    /**
     * First try to get forecast list from appPreferences, and if nothing, get default list from appRepository
     */
    public void listOrderedForecasts() {
        Disposable disposable = appPreferences.getOrderedForecastList()
                .flatMap((Function<Forecasts, Observable<Forecasts>>) orderedForecasts -> {
                    if (orderedForecasts != null && orderedForecasts.getForecasts() != null && orderedForecasts.getForecasts().size() > 0) {
                        return Observable.just(orderedForecasts);
                    } else {
                        return appRepository.getForecasts().toObservable();
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(orderedForecasts -> {
                            this.orderedForecasts.setValue(orderedForecasts.getForecasts());
                        }
                );
        compositeDisposable.add(disposable);
    }

    @Override
    public void newForecastOrder(List<Forecast> orderedForecasts) {
        Forecasts forecasts = new Forecasts();
        forecasts.setForecasts(orderedForecasts);
        appPreferences.setOrderedForecastList(forecasts);
    }

    public void deleteCustomForecastOrder() {
        appPreferences.deleteCustomForecastOrder();
        Disposable disposable = appRepository.getForecasts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(orderedForecasts -> {
                            this.orderedForecasts.setValue(orderedForecasts.getForecasts());
                        }
                );
        compositeDisposable.add(disposable);
    }


    @Override
    public void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }


}
