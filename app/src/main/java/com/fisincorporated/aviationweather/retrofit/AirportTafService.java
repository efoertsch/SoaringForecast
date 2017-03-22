package com.fisincorporated.aviationweather.retrofit;

import com.fisincorporated.aviationweather.data.taf.TafResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AirportTafService {

    int HOURS_BEFORE_NOW = 7;

    /**
     * Get most recent Metars issued within last x.y hours
     *
     * @param icaoIdentifiers whitespace or comma delimited list of airport icao codes
     * @param hoursBeforeNow
     * @return
     */
    @GET("httpparam?dataSource=tafs&requestType=retrieve&format=xml&mostRecentForEachStation=constraint")
    Call<TafResponse> mostRecentTafForEachAirport(@Query("stationString") String icaoIdentifiers
            , @Query("hoursBeforeNow") int hoursBeforeNow);
}
