package org.soaringforecast.rasp;


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.soaringforecast.rasp.dagger.OkHttpClientModule;
import org.soaringforecast.rasp.retrofit.ForecastServerRetrofit;
import org.soaringforecast.rasp.retrofit.JSONServerApi;
import org.soaringforecast.rasp.retrofit.LoggingInterceptor;
import org.soaringforecast.rasp.soaring.forecast.SUAHandler;
import org.soaringforecast.rasp.soaring.json.SUARegion;
import org.soaringforecast.rasp.soaring.json.SUARegionFiles;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class SUADownloadTest {

    private String gbscJsonUrl = "http://soargbsc.com/soaringforecast/";
    private static String newEnglandRegion = "NewEngland";

    private Context context;
    private Retrofit retrofit;
    private JSONServerApi jsonServerApi;
    private SUAHandler suaHandler;
    SUARegionFiles suaRegionFiles;
    private OkHttpClient okHttpClient = new OkHttpClient();

    @Before
    public void setup() {
        context = InstrumentationRegistry.getTargetContext();
        retrofit = new ForecastServerRetrofit(new OkHttpClientModule().getOkHttpClient(new LoggingInterceptor()), gbscJsonUrl).getRetrofit();
        jsonServerApi = retrofit.create(JSONServerApi.class);
        suaHandler = SUAHandler.getInstance(context, jsonServerApi);
    }


    @Test
    public void thereShouldBeNoStoredSuaFileTest() {
        String suaRegionFile = suaHandler.seeIfRegionSUAFileExists(newEnglandRegion);
        assertNull(suaRegionFile);
    }

    @Test
    public void thereShouldBeStoredSuaFileTest() {
        String suaRegionFile = suaHandler.seeIfRegionSUAFileExists(newEnglandRegion);
        assertNotNull("There should be a SUA file stored for:" + newEnglandRegion, suaRegionFile);
    }

    @Test
    public void suaJsonFileShouldBeDownloadedTest() {
        Single<SUARegionFiles> single = jsonServerApi.getSUARegions();
        suaRegionFiles = single.blockingGet();
        assertNotNull(suaRegionFiles);
        System.out.println(" Got SUA file ");
    }

    @Test
    public void shouldDownloadNewEnglandSUAFileTest() {
        suaJsonFileShouldBeDownloadedTest();
        List<SUARegion> suaRegionList = suaRegionFiles.getSuaRegionList();
        for (SUARegion suaRegion : suaRegionList) {
            if (suaRegion.getRegion().equalsIgnoreCase(newEnglandRegion)) {
                Completable completable = suaHandler.getDownloadSUACompleteable(suaRegion.getRegion(), suaRegion.getSuaFileName());
                completable.blockingGet();
                thereShouldBeStoredSuaFileTest();
                return;
            }
        }
        assertNotNull(suaRegionList);
        assert (suaRegionList.size() > 0);
    }

    @Test
    public void shouldReturnSuaGeoJsonTest(){
       Observable<JSONObject> suaGeoJsonObservable =  suaHandler.displaySuaForRegion(newEnglandRegion);
       suaGeoJsonObservable.blockingForEach(jsonObject -> {
           assertNotNull(jsonObject);
       });
    }



}
