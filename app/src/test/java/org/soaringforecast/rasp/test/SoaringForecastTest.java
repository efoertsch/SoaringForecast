package org.soaringforecast.rasp.test;

import org.junit.Before;
import org.junit.Test;
import org.soaringforecast.rasp.cache.BitmapCache;
import org.soaringforecast.rasp.dagger.OkHttpClientModule;
import org.soaringforecast.rasp.retrofit.LoggingInterceptor;
import org.soaringforecast.rasp.retrofit.SoaringForecastApi;
import org.soaringforecast.rasp.retrofit.ForecastServerRetrofit;
import org.soaringforecast.rasp.soaring.json.ForecastModels;
import org.soaringforecast.rasp.soaring.json.Model;
import org.soaringforecast.rasp.soaring.json.Region;
import org.soaringforecast.rasp.soaring.json.Regions;
import org.soaringforecast.rasp.soaring.json.Sounding;
import org.soaringforecast.rasp.utils.BitmapImageUtils;

import java.io.IOException;
import java.util.List;

import io.reactivex.Single;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import timber.log.Timber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SoaringForecastTest {

   // String raspUrl = "http://thepaavolas.net/";
    String raspUrl = "http://soargbsc.com/";
    Retrofit retrofit;
    SoaringForecastApi client;
    Regions regions;
    OkHttpClient okHttpClient = new OkHttpClient();
    BitmapCache bitmapCache = new BitmapCache();
    BitmapImageUtils bitmapImageUtils = new BitmapImageUtils(bitmapCache, new OkHttpClient());

    @Before
    public void createRetrofit() {
        retrofit = new ForecastServerRetrofit(new OkHttpClientModule(). getOkHttpClient(new LoggingInterceptor()), raspUrl).getRetrofit();
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
            Single<ForecastModels> single = client.getForecastModels(region.getName() , date);
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
            Single<ForecastModels> single = client.getForecastModels(region.getName() , date);
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

    @Test
    //region=NewEngland&date=2019-03-20&model=gfs&time=1000&lat=43.132009&lon=-72.157325&param=wstar bsratio
    //    @POST("cgi/get_rasp_blipspot.cgi")
    public void shouldGetALatLongPointForecastSeparateHeaderParmsTest() throws IOException {
        Response<ResponseBody> response = client.getLatLongPointForecast(
                "NewEngland", "2019-05-16", "rap", "1100", "43.132009", "-72.157325",
                "wstar bsratio zsfclcldif zsfclcl zblcldif zblcl sfcwind0spd sfcwind0dir sfcwindspd sfcwinddir blwindspd blwinddir bltopwindspd bltopwinddir").blockingGet();
        assertEquals("OK", response.message());
        System.out.println(response.body().string());
        assertNotNull(response);

    }

    @Test
    //region=NewEngland&date=2019-03-20&model=gfs&time=1000&lat=43.132009&lon=-72.157325&param=wstar bsratio
    //    @POST("cgi/get_rasp_blipspot.cgi")
    public void shouldGetALatLongWaveForecastSeparateHeaderParmsTest() throws IOException {
        Response<ResponseBody> response = client.getLatLongPointForecast(
                "NewEngland", "2019-05-17", "rap", "1100", "43.132009", "-72.157325",
                "press1000 press1000wspd press1000wdir press950 press950wspd press950wdir press850 press850wspd press850wdir" +
                        " press700 press700wspd press700wdir press500 press500wspd press500wdir").blockingGet();
        assertEquals("OK", response.message());
        System.out.println(response.body().string());
        assertNotNull(response);

    }



}
