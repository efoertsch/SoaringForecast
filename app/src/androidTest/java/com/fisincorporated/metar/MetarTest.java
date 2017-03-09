package com.fisincorporated.metar;


import com.fisincorporated.aviationweather.data.metars.MetarResponse;
import com.fisincorporated.aviationweather.retrofit.AppRetrofit;
import com.fisincorporated.aviationweather.retrofit.AviationWeatherApi;

import org.junit.Test;

import java.io.IOException;

import retrofit2.Call;

public class MetarTest {

    @Test
    public void shouldGetMetar() throws IOException {
        AviationWeatherApi client = AppRetrofit.get().create(AviationWeatherApi.class);

        final Call<MetarResponse>  call;

        call = client.mostRecentMetarForEachAirport("KORH", 1);
        MetarResponse response = call.execute().body();

        checkMetarResponse(response);

    }

    static private void checkMetarResponse(MetarResponse metarResponse){
        System.out.println("doing asserts");
        assert(metarResponse.getErrors().getError().isEmpty());
        assert(metarResponse.getData().getNumResults().intValue() >= 1);

    }
}
