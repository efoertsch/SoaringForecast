package com.fisincorporated.soaringforecast.test;

import com.fisincorporated.soaringforecast.dagger.AppModule;
import com.fisincorporated.soaringforecast.data.metars.MetarResponse;
import com.fisincorporated.soaringforecast.retrofit.AviationWeatherGovRetrofit;
import com.fisincorporated.soaringforecast.retrofit.AviationWeatherApi;

import org.junit.Test;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Retrofit;



public class TestMockResponse {

    String airportList = "KORH KBOS";

    @Test
    public void testMockRetrofitResponse() throws IOException {

        // This MockInterceptor always returns same canned Metar and Taf info
        Retrofit retrofit = new AviationWeatherGovRetrofit(new AppModule().getOkHttpClient(new retrofit.MockInterceptor())).getRetrofit();

        AviationWeatherApi service = retrofit.create(AviationWeatherApi.class);
        Call<MetarResponse> call = service.mostRecentMetarForEachAirport(airportList, AviationWeatherApi.METAR_HOURS_BEFORE_NOW);
        MetarResponse tafResponse = call.execute().body();
        // Can loop through response to check data
    }

}
