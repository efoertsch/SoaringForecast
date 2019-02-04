package org.soaringforecast.rasp.satellite;

import android.annotation.SuppressLint;

import org.soaringforecast.rasp.satellite.data.SatelliteImage;
import org.soaringforecast.rasp.satellite.data.SatelliteImageInfo;
import org.soaringforecast.rasp.utils.BitmapImageUtils;
import org.soaringforecast.rasp.utils.TimeUtils;

import org.cache2k.Cache;
import org.reactivestreams.Subscription;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
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


    @Inject
    SatelliteImageDownloader() {
    }

    @SuppressLint("CheckResult")
    public Observable<Void> loadSatelliteImages(String area, String type) {
        satelliteImageInfo = createSatelliteImageInfo(TimeUtils.getUtcRightNow(), area, type);
        return getImageDownloaderObservable(satelliteImageInfo.getSatelliteImageNames());

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
        return new StringBuilder().append("_sat_").append(type).append("_").append(area).append(".jpg").toString();
    }


    public Observable<Void> getImageDownloaderObservable(final List<String> satelliteImageNames) {
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
        bitmapImageUtils.getBitmapImage(satelliteImage, SATELLITE_URL, satelliteImage.getImageName());
    }


    public SatelliteImageInfo getSatelliteImageInfo() {
        return satelliteImageInfo;
    }
}
