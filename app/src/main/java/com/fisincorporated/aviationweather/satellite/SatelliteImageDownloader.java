package com.fisincorporated.aviationweather.satellite;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.fisincorporated.aviationweather.messages.DataLoadCompleteEvent;
import com.fisincorporated.aviationweather.messages.DataLoadingEvent;
import com.fisincorporated.aviationweather.utils.TimeUtils;

import org.cache2k.Cache;
import org.greenrobot.eventbus.EventBus;

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
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class SatelliteImageDownloader {

    // TODO - inject url
    // Remaining URL string similar to .../20180326/16/20180326_1600_sat_irbw_alb.jpg
    public static final String SATELLITE_URL = "https://aviationweather.gov/data/obs/sat/us/";

    private Subscription subscription;
    private SatelliteImageInfo satelliteImageInfo;

    @Inject
    public Cache<String, SatelliteImage> satelliteImageCache;

    private OkHttpClient client;

    @Inject
    SatelliteImageDownloader() {
    }

    public void loadSatelliteImages(String area, String type) {
        client = new OkHttpClient();
        cancelOutstandingLoads();
        satelliteImageInfo = createSatelliteImageInfo(TimeUtils.getUtcRightNow(), area, type);
        fireLoadStarted();
        subscription = getImageDownloaderObservable(satelliteImageInfo.getSatelliteImageNames())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Void>() {
            @Override
            public void onCompleted() {
                confirmLoad();
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

    private void confirmLoad() {
        Timber.d("Confirming load in cache");
        SatelliteImage satelliteImage;
        for (String satelliteImageName : satelliteImageInfo.getSatelliteImageNames()){
            satelliteImage = satelliteImageCache.get(satelliteImageName);
            Timber.d("Satellite image: %s -  %s", satelliteImageName , satelliteImage != null ? " cached" : " not cached");
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
        if (subscription != null) {
            subscription.unsubscribe();
        }
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
        return Observable.from(satelliteImageNames)
                .flatMap(new Func1<String, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(String satelliteImageName) {
                        if (satelliteImageCache.get(satelliteImageName) == null) {
                            SatelliteImage satelliteImage = new SatelliteImage(satelliteImageName);
                            download(satelliteImage);
                            satelliteImageCache.put(satelliteImageName, satelliteImage);
                            Timber.d(" %s %s", satelliteImage.getImageName()
                                    , satelliteImageCache.containsKey(satelliteImageName) ? " was cached." + (satelliteImage.isImageLoaded() ? " w/good bitmap" : " no bitmap") : " not cached.");
                        }
                        return Observable.empty();
                    }
                });
    }

    private void download(final SatelliteImage satelliteImage) {
        //Timber.d("Calling to get: %s", satelliteImage.getImageName());
        Response response = null;
        Request request = new Request.Builder()
                .url(SATELLITE_URL + satelliteImage.getImageName())
                .build();
        try {
            response = client.newCall(request).execute();
            InputStream inputStream = response.body().byteStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Timber.d("Content-Type for download: %s", response.header("Content-Type"));
            if (response.header("Content-Type").startsWith("image") && bitmap != null ) {
                Timber.d("%s good bitmap ", satelliteImage.getImageName());
                satelliteImage.setBitmap(bitmap);
            } else {
                satelliteImage.setErrorOnLoad(true);
                Timber.d("%s null bitmap ", satelliteImage.getImageName());
            }
        } catch (IOException e) {
            satelliteImage.setErrorOnLoad(true);
            Timber.d("%s  IOException ", satelliteImage.getImageName());
            Timber.e(e.toString());
        } catch (NullPointerException npe) {
            satelliteImage.setErrorOnLoad(true);
            Timber.d("%s  Null pointer exception on getting response byteStream"
                    , satelliteImage.getImageName());
            Timber.e(npe.toString());
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
