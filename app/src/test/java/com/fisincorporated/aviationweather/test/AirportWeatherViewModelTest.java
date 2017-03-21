package com.fisincorporated.aviationweather.test;


import com.fisincorporated.aviationweather.airportweather.AirportWeatherAdapter;
import com.fisincorporated.aviationweather.airportweather.AirportWeatherViewModel;
import com.fisincorporated.aviationweather.app.AppPreferences;
import com.fisincorporated.aviationweather.data.metars.Metar;
import com.fisincorporated.aviationweather.data.taf.TAF;
import com.fisincorporated.aviationweather.retrofit.AirportMetarService;
import com.fisincorporated.aviationweather.retrofit.AirportTafService;
import com.fisincorporated.aviationweather.retrofit.AppRetrofit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;

import retrofit2.Retrofit;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AirportWeatherViewModelTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    public AppPreferences appPreferences;

    @Mock
    public AirportWeatherAdapter airportWeatherAdapter;


    // Note to self. Make sure method public else test not found.
    @Test
    public void getAirportCodesFromSharedPreferences() {
        when(appPreferences.getAirportList()).thenReturn("KORH KORD");
        AirportWeatherViewModel airportWeatherViewModel = new AirportWeatherViewModel();
        airportWeatherViewModel.appPreferences = appPreferences;
        airportWeatherViewModel.airportWeatherAdapter = airportWeatherAdapter;
        // This MockInterceptor always returns same canned Metar and Taf info
        Retrofit retrofit = new AppRetrofit(new retrofit.MockInterceptor()).getRetrofit();
        airportWeatherViewModel.airportMetarService = retrofit.create(AirportMetarService.class);
        airportWeatherViewModel.airportTafService = retrofit.create(AirportTafService.class);

        airportWeatherViewModel.refresh();

        // TODO failing due to async nature of retrofit calls. Find way to test.
        verify(airportWeatherAdapter).updateMetarList(Matchers.anyListOf(Metar.class));
        verify(airportWeatherAdapter).updateTafList(Matchers.anyListOf(TAF.class));


    }

}
