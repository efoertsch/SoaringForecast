package org.soaringforecast.rasp.test;

import org.junit.Before;
import org.junit.Test;
import org.soaringforecast.rasp.dagger.OkHttpClientModule;
import org.soaringforecast.rasp.retrofit.JSONServerApi;
import org.soaringforecast.rasp.retrofit.LoggingInterceptor;
import org.soaringforecast.rasp.retrofit.ForecastServerRetrofit;
import org.soaringforecast.rasp.soaring.json.SUARegionFiles;
import org.soaringforecast.rasp.task.json.TurnpointRegions;

import io.reactivex.Single;
import retrofit2.Retrofit;

import static junit.framework.TestCase.assertNotNull;

public class GBSCJsonTest {

    String gbscJsonUrl = "http://soargbsc.com/soaringforecast/";
    Retrofit retrofit;
    JSONServerApi client;
    TurnpointRegions turnpointRegions;
    SUARegionFiles suaRegionFiles;


    @Before
    public void createRetrofit() {
        retrofit = new ForecastServerRetrofit(new OkHttpClientModule(). getOkHttpClient(new LoggingInterceptor()), gbscJsonUrl).getRetrofit();
        client = retrofit.create(JSONServerApi.class);
    }


    @Test
    public void shouldGetTurnpointFilesFromGBSC(){
        Single<TurnpointRegions> single = client.getTurnpointRegions();
        single.test().assertNoErrors();
        single.subscribe(turnpointRegions -> {
            assertNotNull(turnpointRegions);
            System.out.println(" Got Turnpoint files");
            GBSCJsonTest.this.turnpointRegions = turnpointRegions;
        });
    }


    @Test
    public void shouldGetSUAJsonFilesFromGBSC(){
        Single<SUARegionFiles> single = client.getSUARegions();
         single.test().assertNoErrors();
        single.subscribe(suaRegions -> {
            assertNotNull(suaRegions);
            System.out.println(" Got SUA file ");
            GBSCJsonTest.this.suaRegionFiles = suaRegions;
        });
    }





}
