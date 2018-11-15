package com.fisincorporated.soaringforecast.soaring.forecast;

import android.support.annotation.NonNull;

import com.fisincorporated.soaringforecast.common.Constants;
import com.fisincorporated.soaringforecast.common.Constants.FORECAST_SOUNDING;
import com.fisincorporated.soaringforecast.retrofit.SoaringForecastApi;
import com.fisincorporated.soaringforecast.soaring.json.ModelLocationAndTimes;
import com.fisincorporated.soaringforecast.soaring.json.RegionForecastDate;
import com.fisincorporated.soaringforecast.soaring.json.RegionForecastDates;
import com.fisincorporated.soaringforecast.utils.BitmapImageUtils;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;


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

    public Single<RegionForecastDates> getForecastsForDay(final String region) {
        return client.getForecastDates("current.json?" + (new Date()).getTime());
    }

    public Observable<ModelLocationAndTimes> getTypeLocationAndTimes(final String region, final RegionForecastDates regionForecastDates) {
        return Observable.fromIterable(regionForecastDates.getForecastDates())
                .flatMap((Function<RegionForecastDate, Observable<ModelLocationAndTimes>>) (RegionForecastDate regionForecastDate) ->
                        callTypeLocationAndTimes(region, regionForecastDate).toObservable()
                                .doOnNext(regionForecastDate::setModelLocationAndTimes));
    }

    /**
     * Call to find what days forecasts are available for forecasts
     *
     * @return
     * @throws IOException
     * @throws NullPointerException
     */
    public Single<RegionForecastDates> getRegionForecastDates() {
        return client.getForecastDates("current.json?" + (new Date()).getTime());
    }

    /**
     * Call to find out which forecasts (gps, nam, rap) are available for the day
     *
     * @param regionForecastDate
     * @throws IOException
     */
    public Single<ModelLocationAndTimes> callTypeLocationAndTimes(String region, RegionForecastDate regionForecastDate) {
        return client.getTypeLocationAndTimes(region + "/" + regionForecastDate.getYyyymmddDate() + "/status.json");
    }

    /**
     * Get forecast
     *
     * @param region
     * @param yyyymmddDate
     * @param soaringForecastType
     * @param forecastParameter
     * @param times
     * @return
     */

    public Observable<SoaringForecastImage> getSoaringForecastForTypeAndDay(String region, String yyyymmddDate, String soaringForecastType,
                                                                            String forecastParameter, List<String> times) {
        return Observable.fromIterable(times)
                .flatMap((Function<String, Observable<SoaringForecastImage>>) time ->
                        Observable.merge(
                                getSoaringForecastImageObservable(region, yyyymmddDate, soaringForecastType, forecastParameter, time, Constants.BODY, FORECAST_SOUNDING.FORECAST).toObservable()
                                //, getSoaringForecastImageObservable(region, yyyymmddDate, soaringForecastType, forecastParameter, time, Constants.HEAD, ForecastSounding.FORECAST).toObservable()
                                , getSoaringForecastImageObservable(region, yyyymmddDate, soaringForecastType, forecastParameter, time, Constants.SIDE, FORECAST_SOUNDING.FORECAST).toObservable()
                                // , getSoaringForecastImageObservable(region, yyyymmddDate, soaringForecastType, forecastParameter, time, Constants.FOOT, FORECAST_SOUNDING.FORECAST).toObservable()
                        )
                );
    }

    /**
     * Get sounding
     *
     * @param region
     * @param yyyymmddDate
     * @param soaringForecastType
     * @param forecastParameter
     * @param times
     * @return
     */
    public Observable<SoaringForecastImage> getSoaringSoundingForTypeAndDay(String region, String yyyymmddDate, String soaringForecastType,
                                                                            String forecastParameter, List<String> times) {
        return Observable.fromIterable(times)
                .flatMap((Function<String, Observable<SoaringForecastImage>>) time ->
                        getSoaringForecastImageObservable(region, yyyymmddDate, soaringForecastType, forecastParameter
                                , time, Constants.BODY, FORECAST_SOUNDING.SOUNDING).toObservable()
                );
    }

    /**
     * @param region            - "NewEngland"
     * @param yyyymmddDate      - 2018-30-31
     * @param forecastParameter - wstar_bsratio  OR it is sounding index (1 based) with index based on location from sounding.json
     * @param forecastTime      - 1500
     * @param bitmapType        - body
     *                          <p>
     *                          Construct something like http:soargbsc.com/rasp/NewEngland/2018-03-31/gfs/wstar_bsratio.curr.1500lst.d2.body.png?11:15:44”
     *                          for soaring forecast or
     * @param forecastSounding  - gfs/nam/rap
     */
    public Single<SoaringForecastImage> getSoaringForecastImageObservable(String region, String yyyymmddDate, String forecastType,
                                                                          String forecastParameter, String forecastTime, String bitmapType, FORECAST_SOUNDING forecastSounding) {
        final String parmUrl;
        if (forecastTime.startsWith("old ")) {

        }
        switch (forecastSounding) {
            case FORECAST:
                parmUrl = getSoaringForecastUrlParm(region, yyyymmddDate, forecastType, forecastParameter
                        , stripOldIfNeeded(forecastTime), bitmapType);
                break;
            case SOUNDING:
                parmUrl = getSoaringForecastSoundingUrlParm(region, yyyymmddDate, forecastType, forecastParameter
                        , forecastTime, bitmapType);
                break;
            default:
                parmUrl = "xxxx";
        }

        SoaringForecastImage soaringForecastImage = getSoaringForcastImage(region, yyyymmddDate, forecastType
                , forecastParameter, stripOldIfNeeded(forecastTime), bitmapType, parmUrl);

        return Single.create(
                emitter -> {
                    emitter.onSuccess((SoaringForecastImage) bitmapImageUtils.getBitmapImage(soaringForecastImage, forecastUrl + parmUrl));
                });
    }

    private String stripOldIfNeeded(String forecastTime){
        if (forecastTime.startsWith("old ") && forecastTime.length() > 4) {
            return forecastTime.substring(4);
        } else {
            return forecastTime;
        }
    }

    @NonNull
    public SoaringForecastImage getSoaringForcastImage(String region, String yyyymmddDate, String forecastType, String forecastParameter, String forecastTime, String bitmapType, String parmUrl) {
        SoaringForecastImage soaringForecastImage = new SoaringForecastImage(parmUrl);
        soaringForecastImage.setRegion(region)
                .setForecastTime(forecastTime)
                .setYyyymmdd(yyyymmddDate)
                .setForecastType(forecastType)
                .setForecastParameter(forecastParameter)
                .setForecastTime(forecastTime)
                .setBitmapType(bitmapType);
        return soaringForecastImage;
    }

    /**
     * @param region
     * @param yyyymmddDate
     * @param forecastType
     * @param forecastParameter
     * @param forecastTime
     * @param bitmapType
     * @return something like NewEngland/2018-03-31/gfs/wstar_bsratio.curr.1500lst.d2.body.png?11:15:44
     */
    public String getSoaringForecastUrlParm(String region, String yyyymmddDate, String forecastType, String forecastParameter, String forecastTime, String bitmapType) {
        return String.format("%s/%s/%s/%s.curr.%slst.d2.%s.png?%s", region, yyyymmddDate
                , forecastType.toLowerCase(), forecastParameter, forecastTime, bitmapType, new Date().getTime());
    }


    /**
     * @param region
     * @param yyyymmddDate
     * @param forecastType
     * @param soundingIndex
     * @param forecastTime
     * @param bitmapType
     * @return something like NewEngland/2018-08-31/nam/sounding3.curr.1200lst.d2.png
     */

    public String getSoaringForecastSoundingUrlParm(String region, String yyyymmddDate, String forecastType, String soundingIndex, String forecastTime, String bitmapType) {
        return String.format("%s/%s/%s/sounding%s.curr.%slst.d2.png", region, yyyymmddDate
                , forecastType.toLowerCase(), soundingIndex, forecastTime, bitmapType);
    }

}
