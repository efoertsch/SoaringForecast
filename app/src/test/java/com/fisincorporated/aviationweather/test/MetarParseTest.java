package com.fisincorporated.aviationweather.test;


import com.fisincorporated.aviationweather.data.metars.MetarResponse;
import com.fisincorporated.aviationweather.retrofit.AirportMetarService;
import com.fisincorporated.aviationweather.retrofit.AppRetrofit;
import com.fisincorporated.aviationweather.retrofit.LoggingInterceptor;

import org.junit.Test;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Retrofit;

public class MetarParseTest {


    /**
     * Call the real METAR web service and check for response
     * @throws IOException
     */
    @Test
    public void shouldGetMetar() throws IOException {
        Retrofit retrofit = new AppRetrofit(new LoggingInterceptor()).getRetrofit();
        AirportMetarService client =retrofit.create(AirportMetarService.class);

        final Call<MetarResponse>  call;

        call = client.mostRecentMetarForEachAirport("KORH", AirportMetarService.HOURS_BEFORE_NOW);
        MetarResponse response = call.execute().body();

        checkMetarResponse(response);

    }

    static private void checkMetarResponse(MetarResponse metarResponse){
        System.out.println("doing asserts");
        assert(metarResponse.getErrors().getError().isEmpty());
        assert(metarResponse.getData().getNumResults().intValue() >= 1);

    }
}
