package org.soaringforecast.rasp.test;


import org.soaringforecast.rasp.data.metars.MetarResponse;
import org.soaringforecast.rasp.retrofit.AviationWeatherGovApi;
import org.soaringforecast.rasp.retrofit.AviationWeatherGovRetrofit;

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
        Retrofit retrofit = new AviationWeatherGovRetrofit(new OkHttpClient(), "https://aviationweather.gov/adds/dataserver_current").getRetrofit();
        AviationWeatherGovApi client = retrofit.create(AviationWeatherGovApi.class);

        final Call<MetarResponse>  call;

        call = client.getMostRecentMetarForEachAirport("KORH", 3);
        MetarResponse response = call.execute().body();

        checkMetarResponse(response);

    }

    static private void checkMetarResponse(MetarResponse metarResponse){
        Timber.d("doing asserts");
        assert(metarResponse.getErrors().getError().isEmpty());
        assert(metarResponse.getData().getNumResults().intValue() >= 1);

    }
}
