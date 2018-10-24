package com.fisincorporated.soaringforecast.satellite;

import android.annotation.SuppressLint;

import com.fisincorporated.soaringforecast.messages.DataLoadCompleteEvent;
import com.fisincorporated.soaringforecast.messages.DataLoadingEvent;
import com.fisincorporated.soaringforecast.satellite.data.SatelliteImage;
import com.fisincorporated.soaringforecast.satellite.data.SatelliteImageInfo;
import com.fisincorporated.soaringforecast.utils.BitmapImageUtils;
import com.fisincorporated.soaringforecast.utils.TimeUtils;

import org.cache2k.Cache;
import org.greenrobot.eventbus.EventBus;
import org.reactivestreams.Subscription;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class SatelliteImageDownloader {

    // TODO - inject url
    // Remaining URL string similar to .../20180326/16/20180326_1600_sat_irbw_alb.jpg
    private static final String SATELLITE_URL = "https://aviationweather.gov/data/obs/sat/us/";

    private Subscription subscription;
    private SatelliteImageInfo satelliteImageInfo;

    @Inject
    public Cache<String, SatelliteImage> satelliteImageCache;

    @Inject
    public BitmapImageUtils bitmapImageUtils;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    SatelliteImageDownloader() {
    }

    @SuppressLint("CheckResult")
    public void loadSatelliteImages(String area, String type) {
        cancelOutstandingLoads();
        satelliteImageInfo = createSatelliteImageInfo(TimeUtils.getUtcRightNow(), area, type);
        fireLoadStarted();
        DisposableObserver disposableObserver = getImageDownloaderObservable(satelliteImageInfo.getSatelliteImageNames())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Void>() {
                    @Override
                    public void onStart(){
                    }

                    @Override
                    public void onNext(Void aVoid) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        confirmLoad();
                        fireLoadComplete();

                    }
                });
        compositeDisposable.add(disposableObserver);
    }

    private void confirmLoad() {
        Timber.d("Confirming load in cache");
        SatelliteImage satelliteImage;
        for (String satelliteImageName : satelliteImageInfo.getSatelliteImageNames()) {
            satelliteImage = satelliteImageCache.get(satelliteImageName);
            Timber.d("Satellite image: %s -  %s", satelliteImageName, satelliteImage != null ? " cached" : " not cached");
        }
    }

    public static SatelliteImageInfo createSatelliteImageInfo(Calendar imageTime, String area, String type) {
        SatelliteImageInfo satelliteImageInfo = new SatelliteImageInfo();
        String satelliteImageName;
        Calendar satelliteImageTime;

        String imageSuffix = getImageNameSuffix(area, type);
        satelliteImageTime = (Calendar) imageTime.clone();

        TimeUtils.setCalendarToQuarterHour(satelliteImageTime);
        satelliteImageName = TimeUtils.formatCalendarToSatelliteImageUtcDate(satelliteImageTime) + imageSuffix;
        satelliteImageInfo.addSatelliteImageInfo(satelliteImageName, satelliteImageTime, 0);

        // time string will be in ascending order
        for (int i = 0; i < 14; ++i) {
            satelliteImageTime = (Calendar) satelliteImageTime.clone();
            satelliteImageTime.add(Calendar.MINUTE, -15);
            satelliteImageName = TimeUtils.formatCalendarToSatelliteImageUtcDate(satelliteImageTime) + imageSuffix;
            satelliteImageInfo.addSatelliteImageInfo(satelliteImageName, satelliteImageTime, 0);
        }
        return satelliteImageInfo;
    }

    // In format of  ..._sat_irbw_alb.jpg
    public static String getImageNameSuffix(String area, String type) {
        return "_sat" + "_" + type + "_" + area + ".jpg";
    }

    public void cancelOutstandingLoads() {
        compositeDisposable.clear();
    }

    private void fireLoadStarted() {
        EventBus.getDefault().post(new DataLoadingEvent());
    }

    private void fireLoadComplete() {
        EventBus.getDefault().post(new DataLoadCompleteEvent());
    }

    public void shutdown() {
        cancelOutstandingLoads();
        if (satelliteImageCache != null) {
            satelliteImageCache.clearAndClose();
        }
    }

    private Observable<Void> getImageDownloaderObservable(final List<String> satelliteImageNames) {
        return Observable.fromIterable(satelliteImageNames)
                .flatMap((Function<String, Observable<Void>>) satelliteImageName -> {
                    if (satelliteImageCache.get(satelliteImageName) == null) {
                        SatelliteImage satelliteImage = new SatelliteImage(satelliteImageName);
                        getBitmapImage(satelliteImage);
                        satelliteImageCache.put(satelliteImageName, satelliteImage);
                        Timber.d(" %s %s", satelliteImage.getImageName()
                                , satelliteImageCache.containsKey(satelliteImageName) ?
                                        String.format(" was cached.%s", (satelliteImage.isImageLoaded() ? " w/good bitmap" : " no bitmap")) : " not cached.");
                    }
                    return Observable.empty();
                });
    }

    private void getBitmapImage(final SatelliteImage satelliteImage) {
        String url = SATELLITE_URL + satelliteImage.getImageName();
        bitmapImageUtils.getBitmapImage(satelliteImage, url);
    }


    SatelliteImageInfo getSatelliteImageInfo() {
        return satelliteImageInfo;
    }
}
