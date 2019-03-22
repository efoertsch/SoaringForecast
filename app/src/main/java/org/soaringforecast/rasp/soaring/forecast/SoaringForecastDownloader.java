package org.soaringforecast.rasp.soaring.forecast;

import android.support.annotation.NonNull;

import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.app.CacheTimeListener;
import org.soaringforecast.rasp.common.Constants;
import org.soaringforecast.rasp.common.Constants.FORECAST_SOUNDING;
import org.soaringforecast.rasp.retrofit.SoaringForecastApi;
import org.soaringforecast.rasp.soaring.json.ForecastModels;
import org.soaringforecast.rasp.soaring.json.Regions;
import org.soaringforecast.rasp.utils.BitmapImageUtils;
import org.soaringforecast.rasp.utils.StringUtils;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Field;
import timber.log.Timber;


public class SoaringForecastDownloader implements CacheTimeListener {

    private static String raspUrl;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private BitmapImageUtils bitmapImageUtils;
    private SoaringForecastApi client;
    private StringUtils stringUtils;
    private AppPreferences appPreferences;
    private long cacheTimeCheckpoint = 0;
    private long cacheClearTime = 0;

    @Inject
    public SoaringForecastDownloader(SoaringForecastApi client, BitmapImageUtils bitmapImageUtils, String raspUrl
            , StringUtils stringUtils, AppPreferences appPreferences) {
        this.client = client;
        this.bitmapImageUtils = bitmapImageUtils;
        this.raspUrl = raspUrl;
        this.stringUtils = stringUtils;
        this.appPreferences = appPreferences;
        // Since downloader should exist for lifetime of app, not unregistering anywhere
        this.appPreferences.registerCacheTimeChangeListener(this);
        cacheTimeLimit(appPreferences.getClearCacheTime());

    }

    /**
     * Call to find what regions and days forecasts are available
     *
     * @return
     * @throws IOException
     * @throws NullPointerException
     */
    public Single<Regions> getRegionForecastDates() {
        return client.getForecastDates("current.json");
    }

    /**
     * For the given region (e.g. NewEngland) and day find the available forecast models
     *
     * @param region
     * @param regionForecastDate
     * @throws IOException
     */
    public Single<ForecastModels> getForecastModels(String region, String regionForecastDate) {
        //Timber.d("Url:  %1$s/%2$s/status.json", region, regionForecastDate);
        return client.getForecastModels(region + "/" + regionForecastDate + "/status.json");
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
        return checkCacheTimeCompletable().andThen(Observable.fromIterable(times)
                .flatMap((Function<String, Observable<SoaringForecastImage>>) time ->
                        Observable.merge(
                                getSoaringForecastImageObservable(region, yyyymmddDate, soaringForecastType, forecastParameter, time, Constants.BODY, FORECAST_SOUNDING.FORECAST).toObservable()
                                , getSoaringForecastImageObservable(region, yyyymmddDate, soaringForecastType, forecastParameter, time, Constants.SIDE, FORECAST_SOUNDING.FORECAST).toObservable()

                        )
                ));
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
        return checkCacheTimeCompletable().andThen(Observable.fromIterable(times)
                .flatMap((Function<String, Observable<SoaringForecastImage>>) time ->
                        getSoaringForecastImageObservable(region, yyyymmddDate, soaringForecastType, forecastParameter
                                , time, Constants.BODY, FORECAST_SOUNDING.SOUNDING).toObservable()
                ));
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
        return Single.create(emitter -> {
            try {
                final String parmUrl;
                switch (forecastSounding) {
                    case FORECAST:
                        parmUrl = getSoaringForecastUrlParm(region, yyyymmddDate, forecastType, forecastParameter
                                , stringUtils.stripOldIfNeeded(forecastTime), bitmapType);
                        Timber.d("calling parmUrl %1$s", parmUrl);
                        break;
                    case SOUNDING:
                        parmUrl = getSoaringForecastSoundingUrlParm(region, yyyymmddDate, forecastType, forecastParameter
                                , stringUtils.stripOldIfNeeded(forecastTime), bitmapType);
                        break;
                    default:
                        parmUrl = "xxxx";
                }

                SoaringForecastImage soaringForecastImage = getSoaringForcastImage(region, yyyymmddDate, forecastType
                        , forecastParameter, stringUtils.stripOldIfNeeded(forecastTime), bitmapType, parmUrl);
                emitter.onSuccess((SoaringForecastImage) bitmapImageUtils.getBitmapImage(soaringForecastImage, raspUrl, parmUrl));

            } catch (Exception e) {
                emitter.onError(e);
                Timber.e(e);
            }
        });
    }


    @NonNull
    public SoaringForecastImage getSoaringForcastImage(String region, String
            yyyymmddDate, String forecastType, String forecastParameter, String forecastTime, String
                                                               bitmapType, String parmUrl) {
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
     * @return something like NewEngland/2018-03-31/gfs/wstar_bsratio.1500local.d2.body.png
     */
    public String getSoaringForecastUrlParm(String region, String yyyymmddDate, String
            forecastType, String forecastParameter, String forecastTime, String bitmapType) {
        return String.format("%s/%s/%s/%s.%slocal.d2.%s.png", region, yyyymmddDate
                , forecastType.toLowerCase(), forecastParameter, forecastTime, bitmapType);
    }

    /**
     * @param region
     * @param yyyymmddDate
     * @param forecastType
     * @param soundingIndex
     * @param forecastTime
     * @param bitmapType
     * @return something like NewEngland/2018-08-31/nam/sounding3.1200local.d2.png
     */

    public String getSoaringForecastSoundingUrlParm(String region, String yyyymmddDate, String
            forecastType, String soundingIndex, String forecastTime, String bitmapType) {
        return String.format("%s/%s/%s/sounding%s.%slocal.d2.png", region, yyyymmddDate
                , forecastType.toLowerCase(), soundingIndex, forecastTime, bitmapType);
    }


    /**
     * See if need to clear cache
     *
     * @return
     */
    public Completable checkCacheTimeCompletable() {
        return Completable.fromAction(() -> {
            try {
                long currentMillis = new Date().getTime();
                /** if cacheTimeCheckpoint (first time here)
                 *      set current millisec time
                 *      don't need to clear cache
                 *   else if time since last call >= cacheClearTime
                 *       clear cache
                 *       set current millisec time
                 */
                if (cacheTimeCheckpoint == 0) {
                    cacheTimeCheckpoint = currentMillis;
                } else if ((currentMillis - cacheTimeCheckpoint) >= cacheClearTime) {
                    Timber.d("Clearing bitmap cache");
                    bitmapImageUtils.clearAllImages();
                    cacheTimeCheckpoint = currentMillis;
                }
            } catch (Throwable throwable) {
                //TODO report error
                throw Exceptions.propagate(throwable);
            }
        });

    }

    @Override
    public void cacheTimeLimit(int minutes) {
        cacheClearTime = minutes * 60 * 1000;
    }


    public Single<Response<ResponseBody>> getPointForecastAtLatLong(String region, String date, String model
            , String time, String lat, String lon, String forecastType) {
       return client.getLatLongPointForecast(region, date, model, time, lat, lon, forecastType)
    }
}
