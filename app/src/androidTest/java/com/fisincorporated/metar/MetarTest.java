package com.fisincorporated.metar;


import com.fisincorporated.aviationweather.data.metars.MetarResponse;
import com.fisincorporated.aviationweather.retrofit.AppRetrofit;
import com.fisincorporated.aviationweather.retrofit.AviationWeatherApi;

import org.junit.Test;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MetarTest {

    public static final CallComplete callComplete = new CallComplete();

    @Test
    public void shouldGetMetar(){
        AviationWeatherApi client = AppRetrofit.get().create(AviationWeatherApi.class);

        final Call<MetarResponse>  metarCall;

        callComplete.setCallComplete(false);

        metarCall = client.mostRecentMetarForEachAirport("KORH", 0);

        // Execute the call asynchronously. Get a positive or negative callback.
        metarCall.enqueue(new Callback<MetarResponse>() {
            @Override
            public void onResponse(Call<MetarResponse> call, Response<MetarResponse> response) {
                checkMetarResponse(response.body());


            }

            @Override
            public void onFailure(Call<MetarResponse> call, Throwable t) {
                callComplete.setCallComplete(true);

            }
        });

        while (!callComplete.isCallComplete()){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    static private void checkMetarResponse(MetarResponse metarResponse){
        System.out.println("doing asserts");
        assert(metarResponse.getErrors().isEmpty());
        assert(metarResponse.getData().getNumResults().intValue() >= 1);
        callComplete.setCallComplete(true);

    }
}
