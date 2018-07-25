package com.fisincorporated.aviationweather.soaring.forecast;

import android.annotation.SuppressLint;

import com.fisincorporated.aviationweather.common.Constants;
import com.fisincorporated.aviationweather.messages.ReadyToSelectSoaringForecastEvent;
import com.fisincorporated.aviationweather.retrofit.SoaringForecastApi;
import com.fisincorporated.aviationweather.soaring.json.ModelLocationAndTimes;
import com.fisincorporated.aviationweather.soaring.json.RegionForecastDate;
import com.fisincorporated.aviationweather.soaring.json.RegionForecastDates;
import com.fisincorporated.aviationweather.utils.BitmapImageUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


public class SoaringForecastDownloader {

    private static final String forecastUrl = "http:soargbsc.com/rasp/";

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private BitmapImageUtils bitmapImageUtils;

    private SoaringForecastApi client;

    @Inject
    public SoaringForecastDownloader(SoaringForecastApi client, BitmapImageUtils bitmapImageUtils) {
        this.client = client;
        this.bitmapImageUtils = bitmapImageUtils;
    }

    public void shutdown() {
        compositeDisposable.dispose();
    }

    public void clearOutstandingLoads() {
        compositeDisposable.clear();
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
        DisposableObserver disposableObserver = Observable.fromIterable(regionForecastDates.getForecastDates())
                .flatMap((Function<RegionForecastDate, Observable<ModelLocationAndTimes>>) (RegionForecastDate regionForecastDate) ->
                        callTypeLocationAndTimes(region, regionForecastDate).toObservable()
                                .doOnNext(regionForecastDate::setModelLocationAndTimes))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ModelLocationAndTimes>() {
                    @Override
                    public void onNext(ModelLocationAndTimes typeLocationAndTimes) {
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
        compositeDisposable.add(disposableObserver);

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
    public Single<ModelLocationAndTimes> callTypeLocationAndTimes(String region, RegionForecastDate regionForecastDate) {
        return client.getTypeLocationAndTimes(region + "/" + regionForecastDate.getYyyymmddDate() + "/status.json")
                .subscribeOn(Schedulers.io());
    }

    public Observable<SoaringForecastImage> getSoaringForecastForTypeAndDay(String region, String yyyymmddDate, String soaringForecastType,
                                                                            String forecastParameter, List<String> times) {
        return Observable.fromIterable(times)
                .flatMap((Function<String, Observable<SoaringForecastImage>>) time ->
                        Observable.merge(
                        getSoaringForecastImageObservable(region, yyyymmddDate, soaringForecastType, forecastParameter, time, Constants.BODY).toObservable()
                        , getSoaringForecastImageObservable(region, yyyymmddDate, soaringForecastType, forecastParameter, time, Constants.HEAD).toObservable()
                        , getSoaringForecastImageObservable(region, yyyymmddDate, soaringForecastType, forecastParameter, time, Constants.SIDE).toObservable()
                        , getSoaringForecastImageObservable(region, yyyymmddDate, soaringForecastType, forecastParameter, time, Constants.FOOT).toObservable()
                        )
                );
    }

    /**
     * @param region            - "NewEngland"
     * @param yyyymmddDate      - 2018-30-31
     * @param forecastType      - gfs/nam/rap
     * @param forecastParameter - wstar_bsratio
     * @param forecastTime      - 1500
     * @param bitmapType        - body
     *                          <p>
     *                          Construct something like http:soargbsc.com/rasp/NewEngland/2018-03-31/gfs/wstar_bsratio.curr.1500lst.d2.body.png?11:15:44”
     */
    public Single<SoaringForecastImage> getSoaringForecastImageObservable(String region, String yyyymmddDate, String forecastType,
                                                                          String forecastParameter, String forecastTime, String bitmapType) {
        String parmUrl = String.format("%s/%s/%s/%s.curr.%slst.d2.%s.png?%s", region, yyyymmddDate
                , forecastType.toLowerCase(), forecastParameter, forecastTime, bitmapType, new Date().getTime());
        SoaringForecastImage soaringForecastImage = new SoaringForecastImage(parmUrl);
        soaringForecastImage.setRegion(region)
                .setYyyymmdd(yyyymmddDate)
                .setForecastType(forecastType)
                .setForecastParameter(forecastParameter)
                .setForecastTime(forecastTime)
                .setBitmapType(bitmapType);

        return Single.create(
                emitter -> {
                    emitter.onSuccess((SoaringForecastImage) bitmapImageUtils.getBitmapImage(soaringForecastImage, forecastUrl + parmUrl));
                });
    }

}
