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
    public void shouldGetForecastDatesJson() throws IOException {
        final Call<RegionForecastDates> call;

        call = client.getForecastDates("current.json?" + (new Date()).getTime());
        regionForecastDates = call.execute().body();

        checkForecastDatesResponse(regionForecastDates);

    }

    static private void checkForecastDatesResponse(RegionForecastDates regionForecastDates) {
        Timber.d("doing asserts");
        assertNotNull(regionForecastDates);
        assertTrue(regionForecastDates.getStringDateList().size() > 3);
        assertTrue(regionForecastDates.getRegionForecastDateList().size() > 3);
    }


    //http://www.soargbsc.com/rasp/NewEngland/2018-03-30/status.json - Run for each date in above json list
    @Test
    public void shouldParseForecastDatesTest() throws IOException {
        Call<TypeLocationAndTimes> call;
        shouldGetForecastDatesJson();
        regionForecastDates.parseForecastDates();
        assertTrue(regionForecastDates.getRegionForecastDateList().size() > 3);
        for (RegionForecastDate regionForecastDate : regionForecastDates.getForecastDates()) {
            call = client.getTypeLocationAndTimes("NewEngland" + "/" + regionForecastDate.getYyyymmddDate() + "/status.json");
            TypeLocationAndTimes typeLocationAndTimes = call.execute().body();
            checkTypeLocationAndTimes(typeLocationAndTimes);
        }

    }

    private void checkTypeLocationAndTimes(TypeLocationAndTimes typeLocationAndTimes) {
        GpsLocationAndTimes gpsLocationAndTimes;
        if (typeLocationAndTimes.getGfs() != null) {

            gpsLocationAndTimes = typeLocationAndTimes.getGfs();
            testGpsLocationAndTimes(gpsLocationAndTimes);
        }

        if (typeLocationAndTimes.getNam() != null) {
            gpsLocationAndTimes = typeLocationAndTimes.getGfs();
            testGpsLocationAndTimes(gpsLocationAndTimes);
        }

        if (typeLocationAndTimes.getRap() != null) {
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
        TypeLocationAndTimes typeLocationAndTimes;
        SoaringForecastDownloader soaringForecastDownloader = new SoaringForecastDownloader(client);
        soaringForecastDownloader.callRegionForecastDates();
        assertNotNull(soaringForecastDownloader.getRegionForecastDates());
        for (RegionForecastDate regionForecastDate : soaringForecastDownloader.getRegionForecastDates().getForecastDates()) {
            assertNotNull(regionForecastDate.getFormattedDate());
            System.out.println(String.format("%s  %s", regionForecastDate.getFormattedDate(), regionForecastDate.getYyyymmddDate()));
            soaringForecastDownloader.callTypeLocationAndTimes("NewEngland", regionForecastDate);
            typeLocationAndTimes = regionForecastDate.getTypeLocationAndTimes();
            assertNotNull(typeLocationAndTimes);
            if (typeLocationAndTimes.getGfs() != null) {
                System.out.println("Gfs available");
                testGpsLocationAndTimes(typeLocationAndTimes.getGfs());

            }
            if (typeLocationAndTimes.getNam() != null) {
                System.out.println("Nam available");
                testGpsLocationAndTimes(typeLocationAndTimes.getNam());
            }
            if (typeLocationAndTimes.getRap() != null) {
                System.out.println("Rap available");
                testGpsLocationAndTimes(typeLocationAndTimes.getRap());
            }

        }
    }

}
