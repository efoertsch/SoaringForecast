package com.fisincorporated.soaringforecast.test;

import com.fisincorporated.soaringforecast.cache.BitmapCache;
import com.fisincorporated.soaringforecast.dagger.OkHttpClientModule;
import com.fisincorporated.soaringforecast.retrofit.LoggingInterceptor;
import com.fisincorporated.soaringforecast.retrofit.SoaringForecastApi;
import com.fisincorporated.soaringforecast.retrofit.SoaringForecastRetrofit;
import com.fisincorporated.soaringforecast.soaring.forecast.SoaringForecastDownloader;
import com.fisincorporated.soaringforecast.soaring.json.GpsLocationAndTimes;
import com.fisincorporated.soaringforecast.soaring.json.ModelLocationAndTimes;
import com.fisincorporated.soaringforecast.soaring.json.RegionForecastDate;
import com.fisincorporated.soaringforecast.soaring.json.RegionForecastDates;
import com.fisincorporated.soaringforecast.utils.BitmapImageUtils;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import io.reactivex.Single;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import timber.log.Timber;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SoaringForecastTest {

    Retrofit retrofit;
    SoaringForecastApi client;
    RegionForecastDates regionForecastDates;
    OkHttpClient okHttpClient = new OkHttpClient();
    BitmapCache bitmapCache = new BitmapCache();
    BitmapImageUtils bitmapImageUtils = new BitmapImageUtils(bitmapCache, new OkHttpClient());

    @Before
    public void createRetrofit() {
        retrofit = new SoaringForecastRetrofit(new OkHttpClientModule().getOkHttpClient(new LoggingInterceptor())).getRetrofit();
        client = retrofit.create(SoaringForecastApi.class);
    }

    /**
     * Call to get current forecast dates for region
     */
    @Test
    public void shouldGetForecastDatesJson() {
        Single<RegionForecastDates> single = client.getForecastDates("current.json?" + (new Date()).getTime());
        single.subscribe(regionForecastDates -> {
            System.out.println(" Got RegionForecastDates successfully");
            SoaringForecastTest.this.regionForecastDates = regionForecastDates;
        });

    }

    private void checkForecastDatesResponse(RegionForecastDates regionForecastDates) {
        Timber.d("doing asserts");
        assertNotNull(regionForecastDates);
        assertTrue(regionForecastDates.getStringDateList().size() > 3);
        assertTrue(regionForecastDates.getRegionForecastDateList().size() > 3);
    }


    //http://www.soargbsc.com/rasp/NewEngland/2018-03-30/status.json - Run for each date in above json list
    @Test
    public void shouldParseForecastDatesTest() {
        Call<ModelLocationAndTimes> call;
        shouldGetForecastDatesJson();
        regionForecastDates.parseForecastDates();
        assertTrue(regionForecastDates.getRegionForecastDateList().size() > 3);
        for (RegionForecastDate regionForecastDate : regionForecastDates.getForecastDates()) {
            Single<ModelLocationAndTimes> single = client.getTypeLocationAndTimes("NewEngland" + "/" + regionForecastDate.getYyyymmddDate() + "/status.json");
            single.subscribe(this::checkTypeLocationAndTimes);
        }

    }

    private void checkTypeLocationAndTimes(ModelLocationAndTimes modelLocationAndTimes) {
        GpsLocationAndTimes gpsLocationAndTimes;
        if (modelLocationAndTimes.getGfs() != null) {
            System.out.println("Gfs available");
            gpsLocationAndTimes = modelLocationAndTimes.getGfs();
            testGpsLocationAndTimes(gpsLocationAndTimes);
        }

        if (modelLocationAndTimes.getNam() != null) {
            System.out.println("Nam available");
            gpsLocationAndTimes = modelLocationAndTimes.getGfs();
            testGpsLocationAndTimes(gpsLocationAndTimes);
        }

        if (modelLocationAndTimes.getRap() != null) {
            System.out.println("Rap available");
            gpsLocationAndTimes = modelLocationAndTimes.getGfs();
            testGpsLocationAndTimes(gpsLocationAndTimes);
        }

    }


    private void testGpsLocationAndTimes(GpsLocationAndTimes gpsLocationAndTimes) {
        assertNotNull(gpsLocationAndTimes.getCenter());
        assertNotNull(gpsLocationAndTimes.getCorners());
        assertNotNull(gpsLocationAndTimes.getTimes());
    }

    @Test
    public void callLoadForecastsForDayTest() {

        SoaringForecastDownloader soaringForecastDownloader = new SoaringForecastDownloader(client
        , new BitmapImageUtils(bitmapCache, new OkHttpClient.Builder().build()));

        Single<RegionForecastDates> singleRegionForecastDates = soaringForecastDownloader.getRegionForecastDates();
        singleRegionForecastDates.subscribe(regionForecastDates -> {
            assertNotNull("regionForecastDates is null",regionForecastDates);
            System.out.println(" Got RegionForecastDates successfully");
            regionForecastDates.parseForecastDates();
            SoaringForecastTest.this.regionForecastDates = regionForecastDates;

        });


        for (RegionForecastDate regionForecastDate : regionForecastDates.getForecastDates()) {
            assertNotNull(regionForecastDate.getFormattedDate());
            System.out.println(String.format("%s  %s", regionForecastDate.getFormattedDate(), regionForecastDate.getYyyymmddDate()));
            Single<ModelLocationAndTimes> singleTypeLocationAndTimes =soaringForecastDownloader.callTypeLocationAndTimes("NewEngland", regionForecastDate);
            singleTypeLocationAndTimes.subscribe(modelLocationAndTimes1 -> {
            regionForecastDate.setModelLocationAndTimes(modelLocationAndTimes1);
            assertNotNull("typeLocationAndTimes is null", modelLocationAndTimes1);
            checkTypeLocationAndTimes(modelLocationAndTimes1);
            });

        }
    }

}
