package com.fisincorporated.soaringforecast.test;


import com.fisincorporated.soaringforecast.airport.airportweather.AirportMetarTafAdapter;
import com.fisincorporated.soaringforecast.airport.airportweather.AirportMetarTafViewModelOld;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.data.metars.Metar;
import com.fisincorporated.soaringforecast.data.taf.TAF;
import com.fisincorporated.soaringforecast.retrofit.AviationWeatherApi;
import com.fisincorporated.soaringforecast.retrofit.AviationWeatherGovRetrofit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.TimeUnit;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AirportMetarTafViewModelOldTest {

    public OkHttpClient getOkHttpClientWithMockingInterceptor(){
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(4);
        httpClient.dispatcher(dispatcher);
        httpClient.connectTimeout(30, TimeUnit.SECONDS);
        httpClient.readTimeout(30, TimeUnit.SECONDS);
        httpClient.addInterceptor(new retrofit.MockInterceptor());
        return httpClient.build();
    }

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    public AppPreferences appPreferences;

    @Mock
    public AirportMetarTafAdapter airportMetarTafAdapter;


    // Note to self. Make sure method public else test not found.
    @Test
    public void getAirportCodesFromSharedPreferences() {
        when(appPreferences.getAirportList()).thenReturn("KORH KORD");
        AirportMetarTafViewModelOld airportMetarTafViewModelOld = new AirportMetarTafViewModelOld();
        airportMetarTafViewModelOld.appPreferences = appPreferences;
        airportMetarTafViewModelOld.airportMetarTafAdapter = airportMetarTafAdapter;
        // This MockInterceptor always returns same canned Metar and Taf info
        Retrofit retrofit = new AviationWeatherGovRetrofit(getOkHttpClientWithMockingInterceptor()).getRetrofit();
        retrofit.callbackExecutor();
        airportMetarTafViewModelOld.aviationWeatherApi = retrofit.create(AviationWeatherApi.class);

        airportMetarTafViewModelOld.refresh();

        // TODO not working due to async nature of retrofit calls. Find way to test.
        verify(airportMetarTafAdapter).updateMetarList(Matchers.anyListOf(Metar.class));
        verify(airportMetarTafAdapter).updateTafList(Matchers.anyListOf(TAF.class));


    }

}
