package com.fisincorporated.aviationweather.test;

import com.fisincorporated.aviationweather.dagger.AppModule;
import com.fisincorporated.aviationweather.retrofit.LoggingInterceptor;
import com.fisincorporated.aviationweather.retrofit.SoaringForecastApi;
import com.fisincorporated.aviationweather.retrofit.SoaringForecastRetrofit;
import com.fisincorporated.aviationweather.soaring.forecast.SoaringForecastDownloader;
import com.fisincorporated.aviationweather.soaring.json.GpsLocationAndTimes;
import com.fisincorporated.aviationweather.soaring.json.RegionForecastDate;
import com.fisincorporated.aviationweather.soaring.json.RegionForecastDates;
import com.fisincorporated.aviationweather.soaring.json.TypeLocationAndTimes;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.Retrofit;
import timber.log.Timber;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SoaringForecastTest {

    Retrofit retrofit;
    SoaringForecastApi client;
    RegionForecastDates regionForecastDates;

    @Before
    public void createRetrofit() {
        retrofit = new SoaringForecastRetrofit(new AppModule().getOkHttpClient(new LoggingInterceptor())).getRetrofit();
        client = retrofit.create(SoaringForecastApi.class);
    }

    /**
     * Call to get current forecast dates for region
     *
     * @throws IOException
     */
    @Test
    public void shouldGetForecastDatesJson() throws Exception {
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
    public void shouldParseForecastDatesTest() throws Exception {
        Call<TypeLocationAndTimes> call;
        shouldGetForecastDatesJson();
        regionForecastDates.parseForecastDates();
        assertTrue(regionForecastDates.getRegionForecastDateList().size() > 3);
        for (RegionForecastDate regionForecastDate : regionForecastDates.getForecastDates()) {
            Single<TypeLocationAndTimes> single = client.getTypeLocationAndTimes("NewEngland" + "/" + regionForecastDate.getYyyymmddDate() + "/status.json");
            single.subscribe(this::checkTypeLocationAndTimes);
        }

    }

    private void checkTypeLocationAndTimes(TypeLocationAndTimes typeLocationAndTimes) {
        GpsLocationAndTimes gpsLocationAndTimes;
        if (typeLocationAndTimes.getGfs() != null) {
            System.out.println("Gfs available");
            gpsLocationAndTimes = typeLocationAndTimes.getGfs();
            testGpsLocationAndTimes(gpsLocationAndTimes);
        }

        if (typeLocationAndTimes.getNam() != null) {
            System.out.println("Nam available");
            gpsLocationAndTimes = typeLocationAndTimes.getGfs();
            testGpsLocationAndTimes(gpsLocationAndTimes);
        }

        if (typeLocationAndTimes.getRap() != null) {
            System.out.println("Rap available");
            gpsLocationAndTimes = typeLocationAndTimes.getGfs();
            testGpsLocationAndTimes(gpsLocationAndTimes);
        }

    }


    private void testGpsLocationAndTimes(GpsLocationAndTimes gpsLocationAndTimes) {
        assertNotNull(gpsLocationAndTimes.getCenter());
        assertNotNull(gpsLocationAndTimes.getCorners());
        assertNotNull(gpsLocationAndTimes.getTimes());
    }

    @Test
    public void callLoadForecastsForDayTest() throws IOException {

        SoaringForecastDownloader soaringForecastDownloader = new SoaringForecastDownloader(client);

        Single<RegionForecastDates> singleRegionForecastDates = soaringForecastDownloader.callRegionForecastDates();
        singleRegionForecastDates.subscribe(regionForecastDates -> {
            assertNotNull("regionForecastDates is null",regionForecastDates);
            System.out.println(" Got RegionForecastDates successfully");
            regionForecastDates.parseForecastDates();
            SoaringForecastTest.this.regionForecastDates = regionForecastDates;

        });


        for (RegionForecastDate regionForecastDate : regionForecastDates.getForecastDates()) {
            assertNotNull(regionForecastDate.getFormattedDate());
            System.out.println(String.format("%s  %s", regionForecastDate.getFormattedDate(), regionForecastDate.getYyyymmddDate()));
            Single<TypeLocationAndTimes> singleTypeLocationAndTimes =soaringForecastDownloader.callTypeLocationAndTimes("NewEngland", regionForecastDate);
            singleTypeLocationAndTimes.subscribe(typeLocationAndTimes1 -> {
            regionForecastDate.setTypeLocationAndTimes(typeLocationAndTimes1);
            assertNotNull("typeLocationAndTimes is null", typeLocationAndTimes1);
            checkTypeLocationAndTimes(typeLocationAndTimes1);
            });

        }
    }

}
