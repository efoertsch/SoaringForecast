package org.soaringforecast.rasp.test;


import org.soaringforecast.rasp.dagger.OkHttpClientModule;
import org.soaringforecast.rasp.data.taf.TafResponse;
import org.soaringforecast.rasp.retrofit.AviationWeatherApi;
import org.soaringforecast.rasp.retrofit.AviationWeatherGovRetrofit;
import org.soaringforecast.rasp.retrofit.LoggingInterceptor;

import org.junit.Test;

import java.io.IOException;

import retrofit2.Call;

public class TafParseTest {

    AviationWeatherApi client = new AviationWeatherGovRetrofit(new OkHttpClientModule().getOkHttpClient(new LoggingInterceptor())).getRetrofit().create(AviationWeatherApi.class);
    String airportList = "KORH KBOS";

    //Set up this test to help make sure POJO simplexml annotations correct
    // >>>Synchronous call<<<
    @Test
    public void testTafApiCall() throws IOException {
        Call<TafResponse> call = client.mostRecentTafForEachAirport(airportList, AviationWeatherApi.TAF_HOURS_BEFORE_NOW);
        TafResponse tafResponse = call.execute().body();

    }

}


