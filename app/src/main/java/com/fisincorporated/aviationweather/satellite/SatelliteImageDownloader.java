package com.fisincorporated.aviationweather.satellite;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.fisincorporated.aviationweather.utils.TimeUtils;

import org.cache2k.Cache;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class SatelliteImageDownloader {

    public static final String SATELLITE_URL = "https://aviationweather.gov/adds/data/satellite/";

    private Subscription subscription;
    private SatelliteImageInfo satelliteImageInfo;

    @Inject
    public Cache<String, SatelliteImage> satelliteImageCache;

    @Inject
    public SatelliteImageDownloader() {
    }

    public void loadSatelliteImages(String area, String type) {
        cancelOutstandingLoads();
        clearSatelliteImageCache();
        satelliteImageInfo = createSatelliteImageInfo(TimeUtils.getUtcRightNow(), area, type);
        subscription = getImageDownloaderObservable(satelliteImageInfo.getSatelliteImageNames()).subscribeOn(Schedulers.io()).subscribe();
    }

    public static SatelliteImageInfo createSatelliteImageInfo(Calendar imageTime, String area, String type) {
        SatelliteImageInfo satelliteImageInfo = new SatelliteImageInfo();
        String satelliteImageName;
        Calendar satelliteImageTime;

        String imageSuffix = getImageNameSuffix(area, type);
        satelliteImageTime =  (Calendar) imageTime.clone();

        TimeUtils.setCalendarToQuarterHour(satelliteImageTime);
        satelliteImageName = TimeUtils.formatCalendarToSatelliteImageUtcDate(satelliteImageTime ) + imageSuffix;
        satelliteImageInfo.addSatelliteImageInfo(satelliteImageName, satelliteImageTime,0);

        // time string will be in ascending order
        for (int i = 0; i < 14; ++i) {
            satelliteImageTime = (Calendar) satelliteImageTime.clone();
            satelliteImageTime.add(Calendar.MINUTE, -15);
            satelliteImageName =  TimeUtils.formatCalendarToSatelliteImageUtcDate(satelliteImageTime) + imageSuffix;
            satelliteImageInfo.addSatelliteImageInfo(satelliteImageName, satelliteImageTime,0);
        }
        return satelliteImageInfo;
    }

    public static String getImageNameSuffix(String area, String type) {
        return "_" + area.toUpperCase() + "_" + type + ".jpg";
    }

    public void cancelOutstandingLoads() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    private void clearSatelliteImageCache() {
        satelliteImageCache.clear();
    }

    public void shutdown() {
        cancelOutstandingLoads();
        if (satelliteImageCache != null) {
            satelliteImageCache.clearAndClose();
        }
    }

    public Observable<Void> getImageDownloaderObservable(final List<String> satelliteImageNames) {
        return Observable.from(satelliteImageNames)
                .flatMap(new Func1<String, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(String satelliteImageName) {
                        if (satelliteImageCache.get(satelliteImageName) == null) {
                            SatelliteImage satelliteImage = new SatelliteImage(satelliteImageName);
                            download(satelliteImage);
                            satelliteImageCache.put(satelliteImageName, satelliteImage);
                        }
                        return Observable.empty();
                    }
                });
    }

    public void download(final SatelliteImage satelliteImage) {
        Response response = null;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SATELLITE_URL + satelliteImage.getImageName())
                .build();
        try {
            response = client.newCall(request).execute();
            InputStream inputStream = response.body().byteStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            satelliteImage.setBitmap(bitmap);

        } catch (IOException e) {
            satelliteImage.setErrorOnLoad(true);
            System.out.println(e.toString());
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }


    public SatelliteImageInfo getSatelliteImageInfo() {
        return satelliteImageInfo;
    }
}
