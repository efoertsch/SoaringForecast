package org.soaringforecast.rasp.test;

import org.soaringforecast.rasp.dagger.OkHttpClientModule;
import org.soaringforecast.rasp.data.metars.MetarResponse;
import org.soaringforecast.rasp.retrofit.AviationWeatherGovApi;
import org.soaringforecast.rasp.retrofit.AviationWeatherGovRetrofit;

import org.junit.Test;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Retrofit;



public class TestMockResponse {

    private String airportList = "KORH KBOS";

    @Test
    public void testMockRetrofitResponse() throws IOException {

        // This MockInterceptor always returns same canned Metar and Taf info
        Retrofit retrofit = new AviationWeatherGovRetrofit(new OkHttpClientModule().getOkHttpClient
                (new retrofit.MockInterceptor()),"https://aviationweather.gov/adds/dataserver_current").getRetrofit();

        AviationWeatherGovApi service = retrofit.create(AviationWeatherGovApi.class);
        Call<MetarResponse> call = service.getMostRecentMetarForEachAirport(airportList, 3);
        MetarResponse tafResponse = call.execute().body();
        // Can loop through response to check data
    }

}
