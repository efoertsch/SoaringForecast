package org.soaringforecast.satellite;

import android.graphics.Bitmap;
import android.support.test.runner.AndroidJUnit4;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.soaringforecast.rasp.app.BaseTest;
import org.soaringforecast.rasp.cache.BitmapCache;
import org.soaringforecast.rasp.satellite.SatelliteImageDownloader;
import org.soaringforecast.rasp.satellite.data.SatelliteImage;
import org.soaringforecast.rasp.satellite.data.SatelliteImageInfo;
import org.soaringforecast.rasp.utils.BitmapImageUtils;
import org.soaringforecast.rasp.utils.TimeUtils;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import timber.log.Timber;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class SatelliteImageDownloadTest extends BaseTest {

    private SatelliteImageInfo satelliteImageInfo;
    private SatelliteImageDownloader satelliteImageDownloader = new SatelliteImageDownloader();
    private Retrofit retrofit;

    public Cache<String, SatelliteImage> satelliteImageCache = new Cache2kBuilder<String, SatelliteImage>() {
    }
            .name("Satellite Images Cache")
            .eternal(false)
            .expireAfterWrite(15, TimeUnit.MINUTES)    // expire/refresh after 15 minutes
            .entryCapacity(20)
            .build();

    BitmapCache bitmapCache = new BitmapCache();
    BitmapImageUtils bitmapImageUtils = new BitmapImageUtils(bitmapCache, new OkHttpClient());

    @Before
    public void setup(){
        super.setup();
        bitmapCache = BitmapCache.init(getContext());
        satelliteImageDownloader.bitmapImageUtils = bitmapImageUtils;
        satelliteImageDownloader.satelliteImageCache = satelliteImageCache;
    }

    @Test
    public void createSatelliteImageInfo() {
        Calendar rightNow = TimeUtils.getUtcRightNow();
        SatelliteImageInfo satelliteImageInfo = SatelliteImageDownloader.createSatelliteImageInfo(rightNow
                , "alb", "vis", -1, 30);
        assertEquals(satelliteImageInfo.getSatelliteImageLocalTimes().size(), 31);

    }

    @Test
    public void loadSatelliteImages() {
        int mostCurrentBitmapIndex = 0 ;
        Bitmap bitmap;
        SatelliteImage satelliteImage;
        Calendar rightNow = TimeUtils.getUtcRightNow();
        SatelliteImageInfo satelliteImageInfo = SatelliteImageDownloader.createSatelliteImageInfo(rightNow
                , "alb", "vis", -1, 30);
        // This loop finds the most current satellite image. Need the time of image to then go back in 15 min icreaments to get older images
        for (int i = satelliteImageInfo.getSatelliteImageNames().size() -1 ; i >= 0; --i) {
            satelliteImage = new SatelliteImage(satelliteImageInfo.getSatelliteImageName(i));
            bitmap = getBitmapImage(satelliteImage);
            if (bitmap != null) {
                satelliteImage.setBitmap(bitmap);
                satelliteImageCache.put(satelliteImage.getImageName(), satelliteImage);
                mostCurrentBitmapIndex = i;
                Timber.d(" bitmap for %s was found", satelliteImage.getImageName());
                break;
            }
        }

        // Now that we know time of most recent image, step back in time in 15 min increments for older images.
        satelliteImageInfo = SatelliteImageDownloader.createSatelliteImageInfo(satelliteImageInfo.getSatelliteImageCalendar(mostCurrentBitmapIndex)
                , "alb", "vis", -15, 14);
        List<String> imageNames = satelliteImageInfo.getSatelliteImageNames();
        for (int i = imageNames.size() -1 ; i >= 0; --i) {
            if (!satelliteImageCache.containsKey(imageNames.get(i))) {
                satelliteImage = new SatelliteImage(imageNames.get(i));
                bitmap = getBitmapImage(satelliteImage);
                if (bitmap != null) {
                    satelliteImage.setBitmap(bitmap);
                    satelliteImageCache.put(satelliteImage.getImageName(), satelliteImage);
                    Timber.d(" bitmap for %s was found", satelliteImage.getImageName());
                }
            }
        }

    }

    public Bitmap getBitmapImage(final SatelliteImage satelliteImage) {
       return  bitmapImageUtils.download(satelliteImageDownloader.getSatelliteUrl() + satelliteImage.getImageName());
    }

}
