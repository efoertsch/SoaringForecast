package org.soaringforecast.rasp.satellite;

import org.cache2k.Cache;
import org.soaringforecast.rasp.satellite.data.SatelliteImage;
import org.soaringforecast.rasp.satellite.data.SatelliteImageInfo;
import org.soaringforecast.rasp.utils.BitmapImageUtils;
import org.soaringforecast.rasp.utils.TimeUtils;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import timber.log.Timber;

public class SatelliteImageDownloader {

    // TODO - inject url
    // Remaining URL string similar to .../20180326/16/20180326_1600_sat_irbw_alb.jpg
    private static final String SATELLITE_URL = "https://aviationweather.gov/data/obs/sat/us/";

    private SatelliteImageInfo satelliteImageInfo;

    @Inject
    public Cache<String, SatelliteImage> satelliteImageCache;

    @Inject
    public BitmapImageUtils bitmapImageUtils;


    @Inject
    public SatelliteImageDownloader() {
    }

    private void confirmLoad() {
        Timber.d("Confirming load in cache");
        SatelliteImage satelliteImage;
        for (String satelliteImageName : satelliteImageInfo.getSatelliteImageNames()) {
            satelliteImage = satelliteImageCache.get(satelliteImageName);
            Timber.d("Satellite image: %s -  %s", satelliteImageName, satelliteImage != null ? " cached" : " not cached");
        }
    }


    /**
     * Create a list of times to start process of finding available Noaa satellite images
     * At one point images produced every quarter hour, but now seem to be produced every 15 minutes starting at some random part of the hour
     * @param imageTime
     * @param area
     * @param type
     * @param plusMinusMinutes
     * @param calenderPeriods
     * @return
     */
    public static SatelliteImageInfo createSatelliteImageInfo(Calendar imageTime, String area, String type, int plusMinusMinutes, int calenderPeriods) {
        SatelliteImageInfo satelliteImageInfo = new SatelliteImageInfo();
        String satelliteImageName;
        Calendar satelliteImageTime;

        String imageSuffix = getImageNameSuffix(area, type);
        satelliteImageTime = (Calendar) imageTime.clone();

        satelliteImageName = TimeUtils.formatCalendarToSatelliteImageUtcDate(satelliteImageTime) + imageSuffix;
        satelliteImageInfo.addSatelliteImageInfo(satelliteImageName, satelliteImageTime, 0);

        // time string will be in ascending order
        for (int i = 0; i < calenderPeriods; ++i) {
            satelliteImageTime = TimeUtils.addXMinutesToCalendar((Calendar) satelliteImageTime.clone(),plusMinusMinutes);
            satelliteImageName = TimeUtils.formatCalendarToSatelliteImageUtcDate(satelliteImageTime) + imageSuffix;
            satelliteImageInfo.addSatelliteImageInfo(satelliteImageName, satelliteImageTime, 0);
        }
        return satelliteImageInfo;
    }


    // In format of  ..._sat_irbw_alb.jpg
    public static String getImageNameSuffix(String area, String type) {
        return "_sat_" + type + "_" + area + ".jpg";
    }

    public Single<SatelliteImageInfo> getSatelliteImageInfoSingle(final String area, final String type) {
        return Single.create(emitter -> {
            try {
                int mostCurrentBitmapIndex = 0 ;
                SatelliteImage satelliteImage;
                Calendar rightNow = TimeUtils.getUtcRightNow();
                SatelliteImageInfo satelliteImageInfo = SatelliteImageDownloader.createSatelliteImageInfo(rightNow
                        , area, type, -1, 30);
                // This loop finds the most current satellite image. Need the time of image to then go back in 15 min increments to get older images
                for (int i = satelliteImageInfo.getSatelliteImageNames().size() -1 ; i >= 0; --i) {
                    satelliteImage = new SatelliteImage(satelliteImageInfo.getSatelliteImageName(i));
                    getWeatherSatelliteImage(satelliteImage);
                    if (satelliteImage.getBitmap() != null) {
                        mostCurrentBitmapIndex = i;
                        Timber.d(" bitmap for %s was found", satelliteImage.getImageName());
                        break;
                    }
                }
                // Now that we know time of most recent image, create new array of times, stepping back in time in 15 min increments for older images.
                // (Most recent time, from above, is at end of list)
                satelliteImageInfo = SatelliteImageDownloader.createSatelliteImageInfo(satelliteImageInfo.getSatelliteImageCalendar(mostCurrentBitmapIndex)
                        , area, type, -15, 14);
                emitter.onSuccess(satelliteImageInfo);
            } catch (Exception e) {
                emitter.onError(e);
                Timber.e(e);
            }
        });

    }


    public Observable<Void> getImageDownloaderObservable(final List<String> satelliteImageNames) {
        return Observable.fromIterable(satelliteImageNames)
                .flatMap((Function<String, Observable<Void>>) satelliteImageName -> {
                    SatelliteImage satelliteImage = new SatelliteImage(satelliteImageName);
                    if (satelliteImageCache.get(satelliteImageName) == null) {
                        getWeatherSatelliteImage(satelliteImage);
                        if (satelliteImage.isImageLoaded()) {
                            satelliteImageCache.put(satelliteImageName, satelliteImage);
                            Timber.d(" %s %s", satelliteImage.getImageName()
                                    , satelliteImageCache.containsKey(satelliteImageName) ?
                                            String.format(" was cached.%s", (satelliteImage.isImageLoaded() ? " w/good bitmap" : " no bitmap")) : " not cached.");
                        }
                    }
                    return Observable.empty();
                });
    }

    public void getWeatherSatelliteImage(final SatelliteImage satelliteImage) {
        bitmapImageUtils.getBitmapImage(satelliteImage, SATELLITE_URL, satelliteImage.getImageName());
    }

    public String getSatelliteUrl(){
        return SATELLITE_URL;
    }


    public SatelliteImageInfo getSatelliteImageInfo() {
        return satelliteImageInfo;
    }
}
