package com.fisincorporated.soaringforecast.test;


import com.fisincorporated.soaringforecast.dagger.OkHttpClientModule;
import com.fisincorporated.soaringforecast.data.taf.TafResponse;
import com.fisincorporated.soaringforecast.retrofit.AviationWeatherApi;
import com.fisincorporated.soaringforecast.retrofit.AviationWeatherGovRetrofit;
import com.fisincorporated.soaringforecast.retrofit.LoggingInterceptor;

import org.junit.Test;

import java.io.IOException;

import retrofit2.Call;

public class TafParseTest {

    AviationWeatherApi client = new AviationWeatherGovRetrofit(new OkHttpClientModule().getOkHttpClient(new LoggingInterceptor())).getRetrofit().create(AviationWeatherApi.class);
    String airportList = new String("KORH KBOS");

    //Set up this test to help make sure POJO simplexml annotations correct
    // >>>Synchronous call<<<
    @Test
    public void testTafApiCall() throws IOException {
        Call<TafResponse> call = client.mostRecentTafForEachAirport(airportList, AviationWeatherApi.TAF_HOURS_BEFORE_NOW);
        TafResponse tafResponse = call.execute().body();

    }

}


