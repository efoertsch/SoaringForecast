package com.fisincorporated.aviationweather.retrofit;

import com.fisincorporated.aviationweather.data.metars.MetarResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface AirportMetarService {
    /**
     * Get most recent TAFs issued within last x.y hours
     *
     * @param icaoIdentifiers whitespace or comma delimited list of airport icao codes
     * @param hoursBeforeNow
     * @return
     */
    @GET("httpparam?dataSource=metars&requestType=retrieve&format=xml&mostRecentForEachStation=constraint")
    Call<MetarResponse> mostRecentMetarForEachAirport(@Query("stationString") String icaoIdentifiers
            , @Query("hoursBeforeNow") int hoursBeforeNow);

}
