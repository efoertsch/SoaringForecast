package com.fisincorporated.aviationweather.test;


import com.fisincorporated.aviationweather.dagger.AppModule;
import com.fisincorporated.aviationweather.data.taf.TafResponse;
import com.fisincorporated.aviationweather.retrofit.AviationWeatherGovRetrofit;
import com.fisincorporated.aviationweather.retrofit.AviationWeatherApi;
import com.fisincorporated.aviationweather.retrofit.LoggingInterceptor;

import org.junit.Test;

import java.io.IOException;

import retrofit2.Call;

public class TafParseTest {

    AviationWeatherApi client = new AviationWeatherGovRetrofit(new AppModule().getOkHttpClient(new LoggingInterceptor())).getRetrofit().create(AviationWeatherApi.class);
    String airportList = new String("KORH KBOS");

    //Set up this test to help make sure POJO simplexml annotations correct
    // >>>Synchronous call<<<
    @Test
    public void testTafApiCall() throws IOException {
        Call<TafResponse> call = client.mostRecentTafForEachAirport(airportList, AviationWeatherApi.TAF_HOURS_BEFORE_NOW);
        TafResponse tafResponse = call.execute().body();

    }

}


