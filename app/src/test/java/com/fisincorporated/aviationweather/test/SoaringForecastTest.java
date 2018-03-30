package com.fisincorporated.aviationweather.test;

import com.fisincorporated.aviationweather.dagger.AppModule;
import com.fisincorporated.aviationweather.retrofit.AviationWeatherGovRetrofit;
import com.fisincorporated.aviationweather.retrofit.LoggingInterceptor;
import com.fisincorporated.aviationweather.retrofit.SoaringForecastApi;
import com.fisincorporated.aviationweather.soaring.json.ForecastDates;

import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Retrofit;
import timber.log.Timber;

public class SoaringForecastTest {

    /**
     * Call to get current forecast dates for region
     * @throws IOException
     */
    @Test
    public void shouldGetForecastDatesJson() throws IOException {
        Retrofit retrofit = new AviationWeatherGovRetrofit(new AppModule().getOkHttpClient(new LoggingInterceptor())).getRetrofit();
        SoaringForecastApi client = retrofit.create(SoaringForecastApi.class);

        final Call<ForecastDates> call;

        call = client.getForecastDates(SoaringForecastApi.BASE_URL + "current.json?" + (new Date()).getTime());
        ForecastDates response = call.execute().body();

        checkForecastDatesResponse(response);

    }

    static private void checkForecastDatesResponse(ForecastDates response){
        Timber.d("doing asserts");
        assert(response.getForecastDates() != null);
        assert(response.getForecastDates().size() > 3);

    }
}
