package org.soaringforecast.rasp.retrofit;

import org.soaringforecast.rasp.one800wxbrief.routebriefing.RouteBriefing;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface One800WxBriefApi {

    // Get a METAR (mainly for testing 1800wxbrief api. App get METARS from other source
    @GET("retrieveMETAR")
    Call<ResponseBody> getMETAR(@Query("location") String airport);

    @POST("FP/routeBriefing")
    Call<RouteBriefing> getRouteBriefing(@Body RequestBody completeQueryString);

}
