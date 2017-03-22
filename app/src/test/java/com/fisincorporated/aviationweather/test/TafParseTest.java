package com.fisincorporated.aviationweather.test;


import com.fisincorporated.aviationweather.data.taf.TafResponse;
import com.fisincorporated.aviationweather.retrofit.AirportTafService;
import com.fisincorporated.aviationweather.retrofit.AppRetrofit;
import com.fisincorporated.aviationweather.retrofit.LoggingInterceptor;

import org.junit.Test;

import java.io.IOException;

import retrofit2.Call;

public class TafParseTest {

    AirportTafService client = new AppRetrofit(new LoggingInterceptor()).getRetrofit().create(AirportTafService.class);
    String airportList = new String("KORH KBOS");

    //Set up this test to help make sure POJO simplexml annotations correct
    // >>>Synchronous call<<<
    @Test
    public void testTafApiCall() throws IOException {
        Call<TafResponse> call = client.mostRecentTafForEachAirport(airportList, AirportTafService.HOURS_BEFORE_NOW);
        TafResponse tafResponse = call.execute().body();

    }

}


