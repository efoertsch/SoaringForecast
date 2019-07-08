package org.soaringforecast.rasp.test;

import org.junit.Before;
import org.junit.Test;
import org.soaringforecast.rasp.dagger.OkHttpClientModule;
import org.soaringforecast.rasp.retrofit.GBSCJsonApi;
import org.soaringforecast.rasp.retrofit.LoggingInterceptor;
import org.soaringforecast.rasp.retrofit.SoaringForecastRetrofit;
import org.soaringforecast.rasp.task.json.TurnpointRegions;

import io.reactivex.Single;
import retrofit2.Retrofit;

public class GBSCJsonTest {

    String gbscJsonUrl = "http://soargbsc.com/soaringforecast/";
    Retrofit retrofit;
    GBSCJsonApi client;
    TurnpointRegions turnpointRegions;


    @Before
    public void createRetrofit() {
        retrofit = new SoaringForecastRetrofit(new OkHttpClientModule(). getOkHttpClient(new LoggingInterceptor()), gbscJsonUrl).getRetrofit();
        client = retrofit.create(GBSCJsonApi.class);
    }


    @Test
    public void shouldGetTurnpointFilesFromGBSC(){
        Single<TurnpointRegions> single = client.getTurnpointRegions();
        single.test().assertNoErrors();
        single.subscribe(turnpointRegions -> {
            System.out.println(" Got Turnpoint files successfully downloaded");
            GBSCJsonTest.this.turnpointRegions = turnpointRegions;
        });
    }






}
