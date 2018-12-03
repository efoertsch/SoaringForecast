package com.fisincorporated.soaringforecast.test;

import com.fisincorporated.soaringforecast.cache.BitmapCache;
import com.fisincorporated.soaringforecast.dagger.OkHttpClientModule;
import com.fisincorporated.soaringforecast.retrofit.LoggingInterceptor;
import com.fisincorporated.soaringforecast.retrofit.SoaringForecastApi;
import com.fisincorporated.soaringforecast.retrofit.SoaringForecastRetrofit;
import com.fisincorporated.soaringforecast.soaring.json.ForecastModels;
import com.fisincorporated.soaringforecast.soaring.json.Model;
import com.fisincorporated.soaringforecast.soaring.json.Region;
import com.fisincorporated.soaringforecast.soaring.json.Regions;
import com.fisincorporated.soaringforecast.soaring.json.Sounding;
import com.fisincorporated.soaringforecast.utils.BitmapImageUtils;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import io.reactivex.Single;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import timber.log.Timber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SoaringForecastTest {

    String raspUrl = "http://thepaavolas.net/rasp/";
    Retrofit retrofit;
    SoaringForecastApi client;
    Regions regions;
    OkHttpClient okHttpClient = new OkHttpClient();
    BitmapCache bitmapCache = new BitmapCache();
    BitmapImageUtils bitmapImageUtils = new BitmapImageUtils(bitmapCache, new OkHttpClient());

    @Before
    public void createRetrofit() {
        retrofit = new SoaringForecastRetrofit(new OkHttpClientModule().getOkHttpClient(new LoggingInterceptor()), raspUrl).getRetrofit();
        client = retrofit.create(SoaringForecastApi.class);
    }

    /**
     * Call to get current forecast dates for region
     */
    @Test
    public void shouldGetRegionsCurrentJsonText() {
        Single<Regions> single = client.getForecastDates("current.json");
        single.test().assertNoErrors();
    }

    private void getRegions(){
        Single<Regions> single = client.getForecastDates("current.json");
        single.subscribe(regions -> {
            System.out.println(" Got Regions successfully");
            SoaringForecastTest.this.regions = regions;
        });
    }

    @Test
    public void checkRegionForecastDatesResponse() {
       getRegions();
        Timber.d("doing asserts");
        assertNotNull(regions);
        assertNotNull(regions.getRegions());
        assertTrue(regions.getRegions().size() >= 1);
    }


    //http://www.soargbsc.com/rasp/NewEngland/2018-03-30/status.json - Run for each date in above json list
    @Test
    public void shouldGetStatusJsonForEachDateTest() {
        getRegions();
        Region region  = regions.getRegion("NewEngland");
        assertNotNull("NewEngland Region missing from status.json", region);
        System.out.println(region.toString());
        for (String date : region.getDates()) {
            Single<ForecastModels> single = client.getForecastModels(region.getName() + "/" + date + "/status.json");
            single.subscribe(this::forecastModelShouldHaveNameCenterCornersAndTimes);
        }
    }

    @Test
    public void soundingsShouldHaveValues() {
        getRegions();
        Region region = regions.getRegion("NewEngland");
        List<Sounding> soundings = region.getSoundings();
        assertNotNull(soundings);
        for (int i = 0; i < soundings.size(); ++i) {
            assertEquals((int) soundings.get(i).getPosition(), i + 1);
            assertTrue(soundings.get(i).getLat() != 0);
            assertTrue(soundings.get(i).getLng() != 0);

        }
    }
    @Test
    public void eachRegionDateShouldHaveAtValidModelValues() {
        getRegions();
        Region region = regions.getRegion("NewEngland");
        System.out.println(region.toString());
        for (String date : region.getDates()) {
            Single<ForecastModels> single = client.getForecastModels(region.getName() + "/" + date + "/status.json");
            single.subscribe(this::forecastModelShouldHaveNameCenterCornersAndTimes);
        }
    }

    private void forecastModelShouldHaveNameCenterCornersAndTimes(ForecastModels forecastModels) {
        for (Model model: forecastModels.getModels()) {
            System.out.println(model.getName() + " available");
            testModel(model);
        }
    }

    private void testModel(Model model) {
        assertNotNull(model.getCenter());
        assertNotNull(model.getCorners());
        assertNotNull(model.getTimes());
    }

}
