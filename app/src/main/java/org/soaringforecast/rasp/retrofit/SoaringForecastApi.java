package org.soaringforecast.rasp.retrofit;



import org.soaringforecast.rasp.soaring.json.ForecastModels;
import org.soaringforecast.rasp.soaring.json.Regions;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * Calls to www.soargbsc.com/rasp
 */
public interface SoaringForecastApi {

    // Construct URL as BASE_URL + "current.json
    @GET()
    Single<Regions> getForecastDates(@Url String url);


    // Construct URL as BASE_URL + "NewEngland/2018-03-30/status.json"
    @GET
    Single<ForecastModels> getForecastModels(@Url String url);


    @POST("cgi/get_rasp_blipspot.cgi")
    @FormUrlEncoded
    Single<Response<ResponseBody>> getLatLongPointForecast(
            @Field(value = "region", encoded = true) String region,
            @Field(value = "date", encoded = true) String date,
            @Field(value = "model", encoded = true) String model,
            @Field(value = "time", encoded = true) String time,
            @Field(value = "lat", encoded = true) String lat,
            @Field(value = "lon", encoded = true)String lon,
            @Field(value = "param", encoded = true) String forecastType
    );

}
