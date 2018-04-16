package com.fisincorporated.aviationweather.test;


import com.fisincorporated.aviationweather.data.metars.MetarResponse;
import com.fisincorporated.aviationweather.retrofit.AviationWeatherApi;
import com.fisincorporated.aviationweather.retrofit.AviationWeatherGovRetrofit;

import org.junit.Test;

import java.io.IOException;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import timber.log.Timber;

public class MetarParseTest {


    /**
     * Call the real METAR web service and check for response
     * @throws IOException
     */
    @Test
    public void shouldGetMetar() throws IOException {
        Retrofit retrofit = new AviationWeatherGovRetrofit(new OkHttpClient()).getRetrofit();
        AviationWeatherApi client = retrofit.create(AviationWeatherApi.class);

        final Call<MetarResponse>  call;

        call = client.mostRecentMetarForEachAirport("KORH", AviationWeatherApi.METAR_HOURS_BEFORE_NOW);
        MetarResponse response = call.execute().body();

        checkMetarResponse(response);

    }

    static private void checkMetarResponse(MetarResponse metarResponse){
        Timber.d("doing asserts");
        assert(metarResponse.getErrors().getError().isEmpty());
        assert(metarResponse.getData().getNumResults().intValue() >= 1);

    }
}
