package com.fisincorporated.aviationweather.satellite;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.fisincorporated.aviationweather.app.DataLoading;
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
import rx.Observer;
import rx.Subscription;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class SatelliteImageDownloader {

    // TODO - inject url
    // Remaining URL string similar to .../20180326/16/20180326_1600_sat_irbw_alb.jpg
    public static final String SATELLITE_URL = "https://aviationweather.gov/data/obs/sat/us/";

    private Subscription subscription;
    private SatelliteImageInfo satelliteImageInfo;
    private DataLoading dataLoading = null;

    @Inject
    public Cache<String, SatelliteImage> satelliteImageCache;

     private OkHttpClient client;

    @Inject
    public SatelliteImageDownloader() {
    }

    public void loadSatelliteImages(DataLoading dataLoading, String area, String type) {
        client = new OkHttpClient();
        this.dataLoading = dataLoading;
        cancelOutstandingLoads();
        //clearSatelliteImageCache();
        satelliteImageInfo = createSatelliteImageInfo(TimeUtils.getUtcRightNow(), area, type);
        fireLoadStarted();
        subscription = getImageDownloaderObservable(satelliteImageInfo.getSatelliteImageNames()).subscribeOn(Schedulers.io()).subscribe(new Observer<Void>() {
            @Override
            public void onCompleted() {
                fireLoadComplete();
            }

            @Override
            public void onError(Throwable e) {
                fireLoadComplete();
            }

            @Override
            public void onNext(Void aVoid) {
            }
        });
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
        if (subscription != null) {
            subscription.unsubscribe();
        }
        fireLoadComplete();
    }

    public void fireLoadStarted() {
        if (dataLoading != null) {
            dataLoading.loadRunning(true);
        }
    }

    public void fireLoadComplete() {
        if (dataLoading != null) {
            dataLoading.loadRunning(false);
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

    private Observable<Void> getImageDownloaderObservable(final List<String> satelliteImageNames) {
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

    private void download(final SatelliteImage satelliteImage) {
        Timber.d("Calling for:" + satelliteImage.getImageName());
        Response response = null;
        Request request = new Request.Builder()
                .url(SATELLITE_URL + satelliteImage.getImageName())
                .build();
        try {
            response = client.newCall(request).execute();
            InputStream inputStream = response.body().byteStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null || !response.header("Content-Type").startsWith("image")){
                satelliteImage.setErrorOnLoad(true);
                Timber.d( satelliteImage.getImageName() + " null bitmap");
            } else {
                Timber.d( satelliteImage.getImageName() + "  good bitmap");
                satelliteImage.setBitmap(bitmap);
                satelliteImage.setErrorOnLoad(false);
            }
        } catch (IOException e) {
            satelliteImage.setErrorOnLoad(true);
            Timber.d("IOException getting" + satelliteImage.getImageName());
            Timber.e(e.toString());
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }


    SatelliteImageInfo getSatelliteImageInfo() {
        return satelliteImageInfo;
    }
}
