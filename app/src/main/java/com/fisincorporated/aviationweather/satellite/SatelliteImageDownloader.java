package com.fisincorporated.aviationweather.satellite;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.fisincorporated.aviationweather.cache.BitmapCache;
import com.fisincorporated.aviationweather.messages.DataLoadCompleteEvent;
import com.fisincorporated.aviationweather.messages.DataLoadingEvent;
import com.fisincorporated.aviationweather.satellite.data.SatelliteImage;
import com.fisincorporated.aviationweather.satellite.data.SatelliteImageInfo;
import com.fisincorporated.aviationweather.utils.TimeUtils;

import org.cache2k.Cache;
import org.greenrobot.eventbus.EventBus;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
    public BitmapCache bitmapCache;

    private OkHttpClient client;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    SatelliteImageDownloader() {
    }

    @SuppressLint("CheckResult")
    public void loadSatelliteImages(String area, String type) {
        client = new OkHttpClient();
        cancelOutstandingLoads();
        satelliteImageInfo = createSatelliteImageInfo(TimeUtils.getUtcRightNow(), area, type);
        fireLoadStarted();
        getImageDownloaderObservable(satelliteImageInfo.getSatelliteImageNames())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new Observer<Void>() {
                    @Override
                    public void onSubscribe(Disposable d) {

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
        compositeDisposable.dispose();
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

    private Observable<Void>  getImageDownloaderObservable(final List<String> satelliteImageNames) {
        return Observable.fromIterable(satelliteImageNames)
                .flatMap(new Function<String, Observable<Void>>() {
                    @Override
                    public Observable<Void> apply(String satelliteImageName) {
                        if (satelliteImageCache.get(satelliteImageName) == null) {
                            SatelliteImage satelliteImage = new SatelliteImage(satelliteImageName);
                            getSatelliteBitmap(satelliteImage);
                            satelliteImageCache.put(satelliteImageName, satelliteImage);
                            Timber.d(" %s %s", satelliteImage.getImageName()
                                    , satelliteImageCache.containsKey(satelliteImageName) ?
                                            String.format(" was cached.%s", (satelliteImage.isImageLoaded() ? " w/good bitmap" : " no bitmap")) : " not cached.");
                        }
                        return Observable.empty();
                    }
                });
    }

    private void getSatelliteBitmap(final SatelliteImage satelliteImage) {
        String url = SATELLITE_URL + satelliteImage.getImageName();
        Bitmap bitmap = bitmapCache.get(url);
        if (bitmap != null) {
            satelliteImage.setBitmap(bitmap);
            return;
        }

        bitmap = download(url);
        if (bitmap != null) {
            satelliteImage.setBitmap(bitmap);
            bitmapCache.put(url, bitmap);
            return;
        }
        satelliteImage.setErrorOnLoad(true);

    }

    private Bitmap download(final String url) {
        Bitmap bitmap = null;
        //Timber.d("Calling to get: %s", url);
        Response response = null;
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            response = client.newCall(request).execute();
            Timber.d("Content-Type for download: %s", response.header("Content-Type"));
            if (response.header("Content-Type").startsWith("image")) {
                InputStream inputStream = response.body().byteStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap != null) {
                    Timber.d("good bitmap ");
                    return bitmap;
                }
            }
        } catch (IOException e) {
            Timber.d("%s  IOException ", url);
            Timber.e(e.toString());
        } catch (NullPointerException npe) {
            Timber.d("%s  Null pointer exception on getting response byteStream", url);
            Timber.e(npe.toString());
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return bitmap;
    }


    SatelliteImageInfo getSatelliteImageInfo() {
        return satelliteImageInfo;
    }
}
