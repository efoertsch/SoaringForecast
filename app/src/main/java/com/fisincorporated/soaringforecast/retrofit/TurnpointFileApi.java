package com.fisincorporated.soaringforecast.retrofit;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface TurnpointFileApi {

    @Streaming
    @GET()
    Single<ResponseBody> getCupFile(@Url String url);
}

