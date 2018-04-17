package com.fisincorporated.aviationweather.soaring.forecast;

import android.annotation.SuppressLint;

import com.fisincorporated.aviationweather.app.AppPreferences;
import com.fisincorporated.aviationweather.messages.ReadyToSelectSoaringForecastEvent;
import com.fisincorporated.aviationweather.retrofit.SoaringForecastApi;
import com.fisincorporated.aviationweather.soaring.json.RegionForecastDate;
import com.fisincorporated.aviationweather.soaring.json.RegionForecastDates;
import com.fisincorporated.aviationweather.soaring.json.TypeLocationAndTimes;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.Date;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


public class SoaringForecastDownloader {

    private SoaringForecastDate soaringForecastDate;

   // private RegionForecastDates regionForecastDates;

    private SoaringForecastApi client;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    AppPreferences appPreferences;

    @Inject
    public SoaringForecastDownloader(SoaringForecastApi client) {
        this.client = client;

    }

    public void shutdown() {
    }

    public void cancelOutstandingLoads() {
    }

    public void loadSoaringForcecastImages(String name, String yyyymmddDate, String forecastParameter) {
    }


    // Run on background thread
    @SuppressLint("CheckResult")
    public void loadForecastsForDay(final String region) {
        compositeDisposable.add(callRegionForecastDates()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(regionForecastDates -> {
                            sendViaBus(regionForecastDates);
                        },
                        throwable -> {
                            Timber.d("Error: %s ", throwable.getMessage());
                            //TODO - put error on bus
                            throwable.printStackTrace();
                        })
        );
    }

    private void sendViaBus(RegionForecastDates regionForecastDates) {
        EventBus.getDefault().post(regionForecastDates);
    }

    public void loadTypeLocationAndTimes(final String region, final RegionForecastDates regionForecastDates) {
        Observable.fromIterable(regionForecastDates.getForecastDates())
                .flatMap((Function<RegionForecastDate, Observable<TypeLocationAndTimes>>) (RegionForecastDate regionForecastDate) ->
                        callTypeLocationAndTimes(region, regionForecastDate).toObservable()
                                .doOnNext(regionForecastDate::setTypeLocationAndTimes))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<TypeLocationAndTimes>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(TypeLocationAndTimes typeLocationAndTimes) {
                        // TODO determine how to combine together into Single
                        //nothing
                    }

                    @Override
                    public void onError(Throwable e) {
                        //TODO - put error on bus
                    }

                    @Override
                    public void onComplete() {
                        EventBus.getDefault().post(new ReadyToSelectSoaringForecastEvent());

                    }
                });

    }


    /**
     * Call to find what days forecasts are available for
     *
     * @return
     * @throws IOException
     * @throws NullPointerException
     */
    public Single<RegionForecastDates> callRegionForecastDates() {
        return client.getForecastDates("current.json?" + (new Date()).getTime());
    }

    /**
     * Call to find out which forecasts (gps, nam, rap) are available for the day
     *
     * @param regionForecastDate
     * @throws IOException
     */
    public Single<TypeLocationAndTimes> callTypeLocationAndTimes(String region, RegionForecastDate regionForecastDate) {
        return client.getTypeLocationAndTimes(region + "/" + regionForecastDate.getYyyymmddDate() + "/status.json")
                .subscribeOn(Schedulers.io());
    }

}
